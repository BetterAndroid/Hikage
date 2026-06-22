/*
 * Hikage - A real-time Android View runtime powered by Kotlin DSL.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2026/6/2.
 */
@file:Suppress("DiscouragedApi")

package com.highcapable.hikage.runtime.attribute.resolver

import android.content.Context
import android.content.res.AssetManager
import com.highcapable.hikage.runtime.attribute.bypass.HiddenApiResolver
import com.highcapable.hikage.runtime.attribute.encoder.AttributeValueEncoder
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem
import com.highcapable.hikage.runtime.attribute.exception.XmlParserException
import com.highcapable.kavaref.KavaRef.Companion.resolve
import java.util.concurrent.ConcurrentHashMap

/**
 * The runtime resource-bag based (T2) `enum` / `flag` resolver.
 *
 * Unlike [BuiltInEnumFlagResolver] (T1, a curated static table of framework attributes), this resolver
 * reads the attribute's compiled bag (`ResTable_map`) from the resource table at runtime, so it can
 * resolve **library / application custom** `enum` / `flag` attributes.
 *
 * Mechanism (see `AssetManager#getResourceBagText`):
 * - An `<attr>` is compiled into a complex/bag entry. Its `ATTR_TYPE` entry holds the format bitmask
 *   (which includes [TYPE_ENUM] / [TYPE_FLAGS]); each symbol entry maps the symbol's resource id to its
 *   integer value.
 * - `getResourceBagText(attrResId, bagEntryId)` returns the (coerced) value of one bag entry. We look up
 *   `ATTR_TYPE` to detect enum/flag, and each symbol's resource id (resolved as an `id` resource) to read
 *   its integer value.
 *
 * If the reflective access is unavailable on the current platform, this resolver degrades gracefully
 * ([isEnumFlag] returns false) and the value falls back to being treated as a plain string.
 */
internal object AttributeBagResolver {

    /** `Res_table_map::ATTR_TYPE`, the bag key for the attribute's format bitmask. */
    private const val ATTR_TYPE = 0x01000000

    /** `ResTable_map::TYPE_REFERENCE`. */
    const val TYPE_REFERENCE = 0x00000001

    /** `ResTable_map::TYPE_INTEGER`. */
    const val TYPE_INTEGER = 0x00000004

    /** `ResTable_map::TYPE_COLOR`. */
    const val TYPE_COLOR = 0x00000010

    /** `ResTable_map::TYPE_DIMENSION`. */
    const val TYPE_DIMENSION = 0x00000040

    /** `ResTable_map::TYPE_ENUM`. */
    const val TYPE_ENUM = 0x00010000

    /** `ResTable_map::TYPE_FLAGS`. */
    const val TYPE_FLAGS = 0x00020000

    /** The sentinel for an unresolved format. */
    private const val INVALID_FORMAT = Int.MIN_VALUE

    /** The resolver for member processor. */
    private val processor by HiddenApiResolver::processor

    /** The `AssetManager.getResourceBagText(int resId, int bagEntryId)` method. */
    private val getResourceBagText by lazy {
        AssetManager::class.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstMethodOrNull {
                name = "getResourceBagText"
                parameters(Int::class, Int::class)
            }
    }

    /** Cache of attribute resource id -> format bitmask (or [INVALID_FORMAT]). */
    private val formatCache = ConcurrentHashMap<Int, Int>()

    /** Cache of package name + attribute name to resource id. */
    private val attrResIdCache = ConcurrentHashMap<ResourceIdCacheKey, Int>()

    /** Cache of package name + enum/flag symbol name to resource id. */
    private val symbolResIdCache = ConcurrentHashMap<ResourceIdCacheKey, Int>()

    /** Cache of resource bag values. */
    private val bagTextCache = hashMapOf<BagTextCacheKey, String?>()

    /**
     * Whether the [attr] is a (custom) enum/flag attribute resolvable from the resource bag.
     * @param context the context.
     * @param attr the attribute.
     * @return [Boolean]
     */
    fun isEnumFlag(context: Context, attr: AttributeItem): Boolean {
        if (getResourceBagText == null) return false

        val attrResId = resolveAttrResId(context, attr)
        if (attrResId == 0) return false

        val format = formatOf(context, attrResId)
        return format != INVALID_FORMAT && (format and (TYPE_ENUM or TYPE_FLAGS)) != 0
    }

    /**
     * Resolve the format bitmask of the [attr] from the resource bag.
     * @param context the context.
     * @param attr the attribute.
     * @return [Int] or null.
     */
    fun formatOf(context: Context, attr: AttributeItem): Int? {
        if (getResourceBagText == null) return null

        val attrResId = resolveAttrResId(context, attr)
        if (attrResId == 0) return null

        return formatOf(context, attrResId).takeIf { it != INVALID_FORMAT }
    }

    /**
     * Resolve the enum/flag [value] of the [attr] into an integer by reading the resource bag.
     * @param context the context.
     * @param attr the attribute.
     * @param value the symbol value, e.g. `rounded` or `labelBold|labelItalic`.
     * @return [Int]
     * @throws XmlParserException if a symbol cannot be resolved.
     */
    fun resolve(context: Context, attr: AttributeItem, value: String): Int {
        val pkg = AttributeValueEncoder.namespaceToPackage(context, attr.namespace)

        val attrResId = resolveResourceId(context, pkg, "attr", attr.name, attrResIdCache)
        if (attrResId == 0) throw XmlParserException(
            "Cannot resolve attribute \"$pkg:attr/${attr.name}\"."
        )

        var result = 0
        val tokens = value.split('|').map { it.trim() }.filter { it.isNotEmpty() }
        if (tokens.isEmpty()) throw XmlParserException("Empty enum/flag value for attribute \"${attr.name}\".")
        tokens.forEach { token ->
            val symbolResId = resolveResourceId(context, pkg, "id", token, symbolResIdCache)
            if (symbolResId == 0) throw XmlParserException(
                "Unknown enum/flag symbol \"$token\" for attribute \"${attr.name}\". " +
                    "Alternatively pass a raw Int value, e.g. set(\"${attr.name}\", <int>)."
            )
            val text = bagText(context, attrResId, symbolResId) ?: throw XmlParserException(
                "Cannot resolve enum/flag symbol \"$token\" for attribute \"${attr.name}\" from the resource table."
            )
            result = result or parseIntValue(text, attr, token)
        }

        return result
    }

    private fun resolveAttrResId(context: Context, attr: AttributeItem): Int {
        val pkg = AttributeValueEncoder.namespaceToPackage(context, attr.namespace)
        return resolveResourceId(context, pkg, "attr", attr.name, attrResIdCache)
    }

    private fun formatOf(context: Context, attrResId: Int): Int {
        formatCache[attrResId]?.let { return it }

        val format = bagText(context, attrResId, ATTR_TYPE)?.let(::parseIntOrNull) ?: INVALID_FORMAT
        return formatCache.putIfAbsent(attrResId, format) ?: format
    }

    private fun bagText(context: Context, resId: Int, bagEntryId: Int): String? {
        val cacheKey = BagTextCacheKey(resId, bagEntryId)
        synchronized(bagTextCache) {
            if (bagTextCache.containsKey(cacheKey)) return bagTextCache[cacheKey]
        }

        val text = getResourceBagText?.copy()?.of(context.assets)?.invokeQuietly<CharSequence>(resId, bagEntryId)?.toString()
        synchronized(bagTextCache) {
            bagTextCache[cacheKey] = text
        }
        return text
    }

    private fun resolveResourceId(
        context: Context,
        packageName: String,
        type: String,
        name: String,
        cache: ConcurrentHashMap<ResourceIdCacheKey, Int>
    ): Int {
        val cacheKey = ResourceIdCacheKey(packageName, type, name)
        cache[cacheKey]?.let { return it }

        val id = context.resources.getIdentifier(name, type, packageName)
        return cache.putIfAbsent(cacheKey, id) ?: id
    }

    private fun parseIntValue(text: String, attr: AttributeItem, token: String) = parseIntOrNull(text) ?: throw XmlParserException(
        "Resolved value \"$text\" of enum/flag symbol \"$token\" for attribute \"${attr.name}\" is not an integer."
    )

    private fun parseIntOrNull(text: String): Int? {
        val value = text.trim()
        return when {
            value.startsWith("0x", ignoreCase = true) -> value.substring(2).toLongOrNull(16)?.toInt()
            else -> value.toLongOrNull(10)?.toInt()
        }
    }

    /**
     * The resource id cache key.
     */
    private data class ResourceIdCacheKey(
        val packageName: String,
        val type: String,
        val name: String
    )

    /**
     * The resource bag value cache key.
     */
    private data class BagTextCacheKey(
        val resId: Int,
        val bagEntryId: Int
    )
}