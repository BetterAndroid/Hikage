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

package com.highcapable.hikage.runtime.attribute.encoder

import android.content.Context
import android.util.TypedValue
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem
import com.highcapable.hikage.runtime.attribute.entity.AttributeResolverParams
import com.highcapable.hikage.runtime.attribute.entity.EncodedAttributeValue
import com.highcapable.hikage.runtime.attribute.exception.XmlParserException
import com.highcapable.hikage.runtime.attribute.resolver.AttributeBagResolver
import com.highcapable.hikage.runtime.attribute.resolver.EnumFlagResolver
import java.lang.Float.floatToRawIntBits
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Encodes a [AttributeItem] value into a typed binary [EncodedAttributeValue], mirroring (a subset of)
 * what `aapt2` does at compile time.
 *
 * Supported string forms:
 * - `@[pkg:]type/name` / `@null` -> reference
 * - `?[pkg:]attr/name` -> attribute reference
 * - framework / enum / flag symbols (via [EnumFlagResolver]) -> integer
 * - `#RGB` / `#ARGB` / `#RRGGBB` / `#AARRGGBB` -> color
 * - `true` / `false` -> boolean
 * - `16dp` / `16dip` / `16sp` / `16px` / `16pt` / `16in` / `16mm` -> dimension
 * - `50%` / `50%p` -> fraction
 * - integers (incl. `0x`) / floats -> int / float
 * - anything else -> string
 */
internal object AttributeValueEncoder {

    /** The known dimension unit suffixes mapped to [TypedValue].COMPLEX_UNIT_* values. */
    private val DIMENSION_UNITS = linkedMapOf(
        "dp" to TypedValue.COMPLEX_UNIT_DIP,
        "dip" to TypedValue.COMPLEX_UNIT_DIP,
        "sp" to TypedValue.COMPLEX_UNIT_SP,
        "px" to TypedValue.COMPLEX_UNIT_PX,
        "pt" to TypedValue.COMPLEX_UNIT_PT,
        "in" to TypedValue.COMPLEX_UNIT_IN,
        "mm" to TypedValue.COMPLEX_UNIT_MM
    )

    /** Cache of resource references to resource ids. */
    private val resourceIdCache = ConcurrentHashMap<ResourceIdCacheKey, Int>()

    /** Cache of resource id existence checks. */
    private val resourceExistsCache = ConcurrentHashMap<ResourceExistsCacheKey, Boolean>()

    /**
     * Encode the given [attr].
     * @param context the context (for resolving resource ids).
     * @param attr the attribute.
     * @param resolver the framework symbol resolver.
     * @param params the parameters.
     * @param intern the string pool interning function (string -> pool index).
     * @return [EncodedAttributeValue]
     * @throws XmlParserException if the value cannot be encoded.
     */
    fun encode(
        context: Context,
        attr: AttributeItem,
        resolver: EnumFlagResolver,
        params: AttributeResolverParams,
        intern: (String) -> Int
    ) = when (val value = attr.value) {
        is AttributeItem.Value.Raw -> encodeRawInt(context, attr, value.value, params)
        is AttributeItem.Value.Bool ->
            EncodedAttributeValue(TypedValue.TYPE_INT_BOOLEAN, if (value.value) -1 else 0, -1)
        is AttributeItem.Value.Real -> EncodedAttributeValue(TypedValue.TYPE_FLOAT, floatToRawIntBits(value.value), -1)
        is AttributeItem.Value.Str -> encodeString(context, attr, value.value, resolver, params, intern)
    }

    private fun encodeRawInt(
        context: Context,
        attr: AttributeItem,
        value: Int,
        params: AttributeResolverParams
    ): EncodedAttributeValue {
        val format = AttributeBagResolver.formatOf(context, attr, params)
        if (value.isResourceId(context, params) && format?.acceptsRawInt() != true)
            return EncodedAttributeValue(TypedValue.TYPE_REFERENCE, value, -1)
        if (format == null) return EncodedAttributeValue(TypedValue.TYPE_INT_DEC, value, -1)

        return when {
            format and (AttributeBagResolver.TYPE_ENUM or AttributeBagResolver.TYPE_FLAGS) != 0 ->
                EncodedAttributeValue(TypedValue.TYPE_INT_DEC, value, -1)
            format and AttributeBagResolver.TYPE_REFERENCE != 0 && value.isResourceId(context, params) ->
                EncodedAttributeValue(TypedValue.TYPE_REFERENCE, value, -1)
            format and AttributeBagResolver.TYPE_COLOR != 0 ->
                EncodedAttributeValue(TypedValue.TYPE_INT_COLOR_ARGB8, value, -1)
            format and AttributeBagResolver.TYPE_DIMENSION != 0 ->
                EncodedAttributeValue(TypedValue.TYPE_DIMENSION, floatToComplex(value.toFloat(), TypedValue.COMPLEX_UNIT_PX), -1)
            format and AttributeBagResolver.TYPE_INTEGER != 0 ->
                EncodedAttributeValue(TypedValue.TYPE_INT_DEC, value, -1)
            else -> EncodedAttributeValue(TypedValue.TYPE_INT_DEC, value, -1)
        }
    }

    private fun Int.acceptsRawInt() = this and (
        AttributeBagResolver.TYPE_ENUM or
            AttributeBagResolver.TYPE_FLAGS or
            AttributeBagResolver.TYPE_COLOR or
            AttributeBagResolver.TYPE_DIMENSION or
            AttributeBagResolver.TYPE_INTEGER
    ) != 0

    private fun encodeString(
        context: Context,
        attr: AttributeItem,
        raw: String,
        resolver: EnumFlagResolver,
        params: AttributeResolverParams,
        intern: (String) -> Int
    ): EncodedAttributeValue {
        val value = raw.trim()
        return when {
            value.startsWith("@") -> encodeReference(context, attr, value, params)
            value.startsWith("?") -> encodeAttribute(context, attr, value, params)
            else -> resolveEnumFlag(context, attr, value, resolver, params)?.let {
                EncodedAttributeValue(TypedValue.TYPE_INT_DEC, it, -1)
            } ?: when {
                value.startsWith("#") -> encodeColor(attr, value)
                value == "true" -> EncodedAttributeValue(TypedValue.TYPE_INT_BOOLEAN, -1, -1)
                value == "false" -> EncodedAttributeValue(TypedValue.TYPE_INT_BOOLEAN, 0, -1)
                else -> encodeDimension(value)
                    ?: encodeFraction(value)
                    ?: value.parseIntOrNull()?.let { (data, type) -> EncodedAttributeValue(type, data, -1) }
                    ?: value.parseFloatOrNull()?.let { EncodedAttributeValue(TypedValue.TYPE_FLOAT, it, -1) }
                    ?: intern(raw).let { EncodedAttributeValue(TypedValue.TYPE_STRING, it, it) }
            }
        }
    }

    /**
     * Resolve a symbol value, or null if [attr] is not a symbol attribute (fall through to
     * generic parsing).
     *
     * Resolution is layered: the built-in (T1) [resolver] is tried first for framework attributes, then
     * the runtime resource-bag based [AttributeBagResolver] (T2) for library / custom attributes.
     * A raw integer literal is accepted for any enum/flag attribute (T0).
     * @return [Int] or null.
     */
    private fun resolveEnumFlag(
        context: Context,
        attr: AttributeItem,
        value: String,
        resolver: EnumFlagResolver,
        params: AttributeResolverParams
    ): Int? {
        // T1 only applies to framework attributes, otherwise custom attributes with the same name would
        // be incorrectly treated as Android framework symbols.
        val isFramework = namespaceToPackage(context, attr.namespace, params) == "android"
        val isT1 = isFramework && resolver.isEnumFlag(attr.name)
        val isT2 = !isFramework && AttributeBagResolver.isEnumFlag(context, attr, params)
        if (!isT1 && !isT2) return null

        // Allow a raw integer for enum/flag attributes too (T0).
        value.parseIntOrNull()?.let { (data, _) -> return data }
        return if (isT1) resolver.resolveOrNull(attr.name, value) else AttributeBagResolver.resolve(context, attr, value, params)
    }

    private fun encodeReference(
        context: Context,
        attr: AttributeItem,
        value: String,
        params: AttributeResolverParams
    ): EncodedAttributeValue {
        if (value.startsWith("@+")) throw XmlParserException(
            "Cannot create resource \"$value\" for attribute \"${attr.name}\" at runtime."
        )

        // @null
        if (value == "@null") return EncodedAttributeValue(TypedValue.TYPE_REFERENCE, 0, -1)

        // @[pkg:]type/name
        val body = value.removePrefix("@")
        val id = resolveResourceId(context, attr, body, defType = null, params)

        return EncodedAttributeValue(TypedValue.TYPE_REFERENCE, id, -1)
    }

    private fun encodeAttribute(
        context: Context,
        attr: AttributeItem,
        value: String,
        params: AttributeResolverParams
    ): EncodedAttributeValue {
        // ?[pkg:][attr/]name
        val body = value.removePrefix("?")
        val id = resolveResourceId(context, attr, body, defType = "attr", params)

        return EncodedAttributeValue(TypedValue.TYPE_ATTRIBUTE, id, -1)
    }

    /**
     * Resolve a resource id from a body like `pkg:type/name`, `type/name` or `name`.
     * @param defType the default resource type if not present in [body].
     * @return [Int]
     */
    private fun resolveResourceId(
        context: Context,
        attr: AttributeItem,
        body: String,
        defType: String?,
        params: AttributeResolverParams
    ): Int {
        var pkg: String? = null
        var rest = body
        val colon = rest.indexOf(':')
        if (colon >= 0) {
            pkg = rest.substring(0, colon)
            rest = rest.substring(colon + 1)
        }

        val slash = rest.indexOf('/')
        val type: String?
        val name: String
        if (slash >= 0) {
            type = rest.substring(0, slash)
            name = rest.substring(slash + 1)
        } else {
            type = defType
            name = rest
        }

        if (type.isNullOrEmpty()) throw XmlParserException(
            "Cannot resolve resource reference \"$body\" for attribute \"${attr.name}\": missing resource type."
        )

        val pkgName = pkg ?: params.resourcePackageName(context)
        val id = resolveResourceId(context, pkgName, type, name)
        if (id == 0) throw XmlParserException(
            "Cannot resolve resource \"$pkgName:$type/$name\" for attribute \"${attr.name}\"."
        )
        return id
    }

    private fun resolveResourceId(context: Context, packageName: String, type: String, name: String): Int {
        val cacheKey = ResourceIdCacheKey(packageName, type, name)
        resourceIdCache[cacheKey]?.let { return it }

        val id = context.resources.getIdentifier(name, type, packageName)
        if (id == 0) return 0
        return resourceIdCache.putIfAbsent(cacheKey, id) ?: id
    }

    private fun encodeColor(attr: AttributeItem, value: String): EncodedAttributeValue {
        val hex = value.removePrefix("#")
        val (argb, type) = when (hex.length) {
            3 -> expandNibbles(hex, hasAlpha = false) to TypedValue.TYPE_INT_COLOR_RGB4
            4 -> expandNibbles(hex, hasAlpha = true) to TypedValue.TYPE_INT_COLOR_ARGB4
            6 -> (0xFF000000.toInt() or hex.toLong(16).toInt()) to TypedValue.TYPE_INT_COLOR_RGB8
            8 -> hex.toLong(16).toInt() to TypedValue.TYPE_INT_COLOR_ARGB8
            else -> throw XmlParserException(
                "Invalid color \"$value\" for attribute \"${attr.name}\". Expected #RGB, #ARGB, #RRGGBB or #AARRGGBB."
            )
        }

        return EncodedAttributeValue(type, argb, -1)
    }

    private fun expandNibbles(hex: String, hasAlpha: Boolean): Int {
        fun dup(c: Char): Int {
            val v = c.digitToInt(16)
            return v shl 4 or v
        }

        val a = if (hasAlpha) dup(hex[0]) else 0xFF
        val off = if (hasAlpha) 1 else 0
        val r = dup(hex[off])
        val g = dup(hex[off + 1])
        val b = dup(hex[off + 2])

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    private fun encodeDimension(value: String): EncodedAttributeValue? {
        for ((suffix, unit) in DIMENSION_UNITS) {
            if (value.endsWith(suffix, ignoreCase = true) && value.length > suffix.length) {
                val number = value.dropLast(suffix.length).toFloatOrNull() ?: return null
                return EncodedAttributeValue(TypedValue.TYPE_DIMENSION, floatToComplex(number, unit), -1)
            }
        }

        return null
    }

    private fun encodeFraction(value: String): EncodedAttributeValue? {
        val unit = when {
            value.endsWith("%p") -> TypedValue.COMPLEX_UNIT_FRACTION_PARENT
            value.endsWith("%") -> TypedValue.COMPLEX_UNIT_FRACTION
            else -> return null
        }
        val numberStr = if (unit == TypedValue.COMPLEX_UNIT_FRACTION_PARENT) value.dropLast(2) else value.dropLast(1)
        val number = numberStr.toFloatOrNull() ?: return null

        return EncodedAttributeValue(TypedValue.TYPE_FRACTION, floatToComplex(number / 100f, unit), -1)
    }

    /**
     * Parse an integer literal, returning (data, dataType) or null.
     * @return [Pair]<[Int], [Int]> or null.
     */
    private fun String.parseIntOrNull(): Pair<Int, Int>? {
        if (this.startsWith("0x", ignoreCase = true) || this.startsWith("0X")) {
            val hex = this.substring(2)
            val parsed = hex.toLongOrNull(16) ?: return null
            return parsed.toInt() to TypedValue.TYPE_INT_HEX
        }

        val parsed = this.toLongOrNull(10) ?: return null
        return parsed.toInt() to TypedValue.TYPE_INT_DEC
    }

    private fun String.parseFloatOrNull() = this.toFloatOrNull()?.let { floatToRawIntBits(it) }

    private fun Int.isResourceId(context: Context, params: AttributeResolverParams): Boolean {
        if (this == 0) return false
        val cacheKey = ResourceExistsCacheKey(params.resourcePackageName(context), this)
        resourceExistsCache[cacheKey]?.let { return it }

        val exists = runCatching { context.resources.getResourceTypeName(this) }.isSuccess
        if (!exists) return false

        return resourceExistsCache.putIfAbsent(cacheKey, true) ?: true
    }

    /**
     * Pack a float into an Android complex value (dimension/fraction), mirroring the inverse of
     * [TypedValue.complexToFloat].
     *
     * The 24-bit signed mantissa occupies bits 8..31; the radix selects the fixed-point scale so the
     * value fits in the mantissa with maximum precision.
     * @return [Int]
     */
    private fun floatToComplex(value: Float, unit: Int): Int {
        val absV = abs(value)
        // factor = mantissa / value, chosen so |mantissa| < 2^23.
        val (radix, factor) = when {
            absV < 1f -> TypedValue.COMPLEX_RADIX_0p23 to (1 shl 23).toFloat()
            absV < 256f -> TypedValue.COMPLEX_RADIX_8p15 to (1 shl 15).toFloat()
            absV < 65536f -> TypedValue.COMPLEX_RADIX_16p7 to (1 shl 7).toFloat()
            else -> TypedValue.COMPLEX_RADIX_23p0 to 1f
        }
        var mantissa = (value * factor).roundToInt()
        if (mantissa < -0x800000) mantissa = -0x800000
        if (mantissa > 0x7fffff) mantissa = 0x7fffff

        return (mantissa shl TypedValue.COMPLEX_MANTISSA_SHIFT) or
            (radix shl TypedValue.COMPLEX_RADIX_SHIFT) or
            (unit and TypedValue.COMPLEX_UNIT_MASK)
    }

    /**
     * Resolve a namespace name to a package name for [Context.getResources] `getIdentifier`.
     * @return [String]
     */
    fun namespaceToPackage(
        context: Context,
        namespace: String,
        params: AttributeResolverParams
    ): String = when {
        namespace == "android" -> "android"
        namespace == "http://schemas.android.com/apk/res/android" -> "android"
        namespace == "http://schemas.android.com/apk/res-auto" -> params.resourcePackageName(context)
        namespace.startsWith("http://schemas.android.com/apk/res/") ->
            namespace.removePrefix("http://schemas.android.com/apk/res/")
        namespace.contains('.') -> namespace
        else -> params.resourcePackageName(context)
    }

    /**
     * Resolve the namespace URI used in the binary XML for a namespace name.
     * @return [String]
     */
    fun namespaceToUri(namespace: String) = when {
        namespace == "android" -> "http://schemas.android.com/apk/res/android"
        namespace.startsWith("http://") || namespace.startsWith("https://") -> namespace
        else -> "http://schemas.android.com/apk/res-auto"
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
     * The resource id existence cache key.
     */
    private data class ResourceExistsCacheKey(
        val resourcePackageName: String,
        val resId: Int
    )
}