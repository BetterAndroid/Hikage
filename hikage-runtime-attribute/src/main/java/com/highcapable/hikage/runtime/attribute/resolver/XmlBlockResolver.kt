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
 * This file is created by fankes on 2025/3/5.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate", "PrivatePropertyName")

package com.highcapable.hikage.runtime.attribute.resolver

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.XmlResourceParser
import android.content.res.loader.AssetsProvider
import android.content.res.loader.ResourcesProvider
import androidx.annotation.LayoutRes
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.hikage.runtime.attribute.R
import com.highcapable.hikage.runtime.attribute.builder.BinaryXmlBuilder
import com.highcapable.hikage.runtime.attribute.bypass.HiddenApiResolver
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem
import com.highcapable.hikage.runtime.attribute.entity.AttributeResolverParams
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.lazyClass
import org.xmlpull.v1.XmlPullParser
import java.util.concurrent.ConcurrentHashMap

/**
 * The resolver for creating [XmlResourceParser] from `XmlBlock`.
 * @param context the context to resolve the `XmlBlock` from.
 */
internal class XmlBlockResolver(private val context: Context) : XmlResourceParserResolver {

    private companion object {

        /** The path used to load the apk assets represents an APK file. */
        const val FORMAT_APK = 0

        /** The path used to load the apk assets represents an idmap file. */
        const val FORMAT_IDMAP = 1

        /** The path used to load the apk assets represents a resources.arsc file. */
        const val FORMAT_ARSC = 2

        /** The path used to load the apk assets represents a directory. */
        const val FORMAT_DIR = 3

        /**
         * The apk assets contains framework resource values specified by the system.
         * This allows some functions to filter out this package when computing what
         * configurations/resources are available.
         */
        const val PROPERTY_SYSTEM = 1 shl 0

        /**
         * The apk assets is a shared library or was loaded as a shared library by force.
         * The package ids of dynamic apk assets are assigned at runtime instead of compile time.
         */
        const val PROPERTY_DYNAMIC = 1 shl 1

        /**
         * The apk assets has been loaded dynamically using a [ResourcesProvider].
         * Loader apk assets overlay resources like RROs except they are not backed by an idmap.
         */
        const val PROPERTY_LOADER = 1 shl 2

        /**
         * The apk assets is a RRO.
         * An RRO overlays resource values of its target package.
         */
        const val PROPERTY_OVERLAY = 1 shl 3

        /**
         * The apk assets is owned by the application running in this process and incremental crash
         * protections for this APK must be disabled.
         */
        const val PROPERTY_DISABLE_INCREMENTAL_HARDENING = 1 shl 4

        /**
         * The apk assets only contain the overlayable declarations information.
         */
        const val PROPERTY_ONLY_OVERLAYABLES = 1 shl 5

        /**
         * Shared source XML block holders by application source directory.
         *
         * This is a lifetime holder rather than a performance cache. The source parser path creates
         * `XmlBlock` from a raw `ApkAssets.nativeLoad` pointer, and `XmlBlock(AssetManager, long)`
         * requires that native object to outlive the Java wrapper. If each resolver instance owned
         * and closed this block independently, another resolver could later create a parser from a
         * destroyed native pointer and crash in native code.
         */
        val sourceXmlBlocks = ConcurrentHashMap<String, SourceXmlBlock>()
    }

    /** The apk assets class.  */
    private val ApkAssetsClass by lazyClass("android.content.res.ApkAssets")

    /** The `XmlBlock` class. */
    private val XmlBlockClass by lazyClass("android.content.res.XmlBlock")

    /** Cache of package name + attribute items to synthesized `XmlBlock` instances. */
    private val xmlBlockCache = ConcurrentHashMap<CacheKey, Any>()

    /** The resolver for member processor. */
    private val processor by HiddenApiResolver::processor

    private val newParser by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstMethodOrNull {
                name = "newParser"
                parameters(Int::class)
            }
    }

    private val newParserEmptyArg by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstMethodOrNull {
                name = "newParser"
                emptyParameters()
            }
    }

    private val xmlBlockByteArrayConstructor by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstConstructorOrNull {
                parameters(ByteArray::class)
            }
    }

    private val xmlBlockAssetManagerLongConstructor by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstConstructorOrNull {
                parameters(AssetManager::class, Long::class)
            }
    }

    private val xmlBlockAssetManagerLongBooleanConstructor by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstConstructorOrNull {
                parameters(AssetManager::class, Long::class, Boolean::class)
            }
    }

    private val closeXmlBlock by lazy {
        XmlBlockClass.resolve()
            .processor(processor)
            .optional(silent = true)
            .firstMethodOrNull {
                name = "close"
                emptyParameters()
            }
    }

    override fun newParser(
        attrs: List<AttributeItem>,
        params: AttributeResolverParams
    ) = if (attrs.isEmpty())
        if (AndroidVersion.isAtLeast(AndroidVersion.P)) 
            getOrCreateSourceXmlBlock()?.block?.let { createParserOf(it, params.sourceResId) } ?: createEmptyParser(params)
        else createEmptyParser(params)
    else createParser(attrs, params)

    override fun close() {
        xmlBlockCache.values.forEach { closeXmlBlock(it) }
        xmlBlockCache.clear()

        BinaryXmlBuilder.clear(context.packageName)
    }

    /**
     * Get or create a shared source `XmlBlock` for creating empty parsers.
     *
     * The `XmlBlock(AssetManager, long)` constructor documents that its native object must live
     * longer than the Java `XmlBlock`. It is backed by a raw `ApkAssets.nativeLoad` pointer here,
     * so binding it to a short-lived resolver instance can make another resolver use a destroyed
     * or tagged native pointer. Keep this holder process-scoped and let synthesized byte-array
     * blocks remain instance-owned.
     * @return the source `XmlBlock` holder or null.
     */
    private fun getOrCreateSourceXmlBlock(): SourceXmlBlock? {
        val info = context.applicationContext.applicationInfo
        val sourceDir = info.sourceDir

        sourceXmlBlocks[sourceDir]?.let { return it }

        val nativePtr = loadApkAssetsNativePtr(info) ?: return null
        val block = createXmlBlockFromApkAssets(nativePtr) ?: return null
        val holder = SourceXmlBlock(nativePtr, block)
        val exists = sourceXmlBlocks.putIfAbsent(sourceDir, holder)
        if (exists != null) {
            closeXmlBlock(block)
            return exists
        }

        return holder
    }

    /**
     * Load the `ApkAssets` native pointer from [info].
     * @param info the application info.
     * @return [Long] native pointer or null.
     */
    private fun loadApkAssetsNativePtr(info: ApplicationInfo) = when {
        AndroidVersion.isAtLeast(AndroidVersion.R) ->
            // private static native long nativeLoad(@FormatType int format, @NonNull String path,
            //            @PropertyFlags int flags, @Nullable AssetsProvider asset) throws IOException;
            ApkAssetsClass.resolve()
                .processor(processor)
                .optional()
                .firstMethodOrNull {
                    name = "nativeLoad"
                    parameters(Int::class, String::class, Int::class, AssetsProvider::class)
                    modifiers(Modifiers.NATIVE)
                }?.invokeQuietly(FORMAT_APK, info.sourceDir, PROPERTY_SYSTEM, null)
        AndroidVersion.isAtLeast(AndroidVersion.P) ->
            // private static native long nativeLoad(
            //            @NonNull String path, boolean system, boolean forceSharedLib, boolean overlay)
            //            throws IOException;
            ApkAssetsClass.resolve()
                .processor(processor)
                .optional()
                .firstMethodOrNull {
                    name = "nativeLoad"
                    parameters(String::class, Boolean::class, Boolean::class, Boolean::class)
                    modifiers(Modifiers.NATIVE)
                }?.invokeQuietly(info.sourceDir, false, false, false)
        else -> null
    } as? Long

    /**
     * Create a new parser from a synthesized binary XML with [attrs].
     * @param attrs the resolved attribute item.
     * @param params the parameters.
     * @return [XmlResourceParser]
     */
    private fun createParser(attrs: List<AttributeItem>, params: AttributeResolverParams): XmlResourceParser {
        if (attrs.isEmpty()) return createEmptyParser(params)

        val block = getOrCreateXmlBlock(attrs, params) ?: error(
            "Failed to create XmlBlock from the synthesized XML on Android ${AndroidVersion.code}."
        )
        val parser = createParserOf(block) ?: error(
            "Failed to create parser from the synthesized XmlBlock on Android ${AndroidVersion.code}."
        )

        advanceToStartTag(parser)
        return parser
    }

    /**
     * Create an empty parser from a synthesized binary XML.
     * @param params the parameters.
     * @return [XmlResourceParser]
     */
    private fun createEmptyParser(params: AttributeResolverParams): XmlResourceParser {
        val data = BinaryXmlBuilder.build(context, emptyList(), params)
        val parser = createParserFrom(data) ?: error(
            "Failed to create an empty parser from the synthesized XmlBlock on Android ${AndroidVersion.code}."
        )
        advanceToStartTag(parser)

        return parser
    }

    /**
     * Create an [XmlResourceParser] from the given binary XML [data].
     * @param data the binary XML bytes.
     * @return [XmlResourceParser] or null.
     */
    private fun createParserFrom(data: ByteArray): XmlResourceParser? {
        val block = xmlBlockByteArrayConstructor?.createQuietly(data) ?: return null
        return try {
            createParserOf(block)
        } finally {
            closeXmlBlock(block)
        }
    }

    /**
     * Get or create a cached synthesized `XmlBlock` for [attrs].
     *
     * `XmlResourceParser` instances are stateful and must be recreated, but the backing `XmlBlock`
     * can serve multiple fresh parsers for identical attributes.
     * @param attrs the injected attributes.
     * @param params the parameters.
     * @return `XmlBlock` instance or null.
     */
    private fun getOrCreateXmlBlock(attrs: List<AttributeItem>, params: AttributeResolverParams): Any? {
        val cacheKey = CacheKey(context.packageName, params.resourcePackageName(context), attrs.toList())
        xmlBlockCache[cacheKey]?.let { return it }

        val data = BinaryXmlBuilder.build(context, attrs, params)
        val block = xmlBlockByteArrayConstructor?.createQuietly(data) ?: return null
        val exists = xmlBlockCache.putIfAbsent(cacheKey, block)
        if (exists != null) {
            closeXmlBlock(block)
            return exists
        }

        return block
    }

    /**
     * Create an `XmlBlock` from `ApkAssets` native pointer.
     *
     * The constructor gained a `usesFeatureFlags` boolean on newer platform releases and is still present
     * on API 37, so prefer it when available instead of relying on a narrow SDK window.
     * @param nativePtr the native `ApkAssets` pointer.
     * @return `XmlBlock` instance or null.
     */
    private fun createXmlBlockFromApkAssets(nativePtr: Long): Any? =
        xmlBlockAssetManagerLongBooleanConstructor?.createQuietly(null, nativePtr, false)
            ?: xmlBlockAssetManagerLongConstructor?.createQuietly(null, nativePtr)

    /**
     * Create an [XmlResourceParser] from the given `XmlBlock` [block] instance.
     * @param block the `XmlBlock` instance.
     * @param sourceResId the source layout resource id.
     * @return [XmlResourceParser] or null.
     */
    private fun createParserOf(block: Any, @LayoutRes sourceResId: Int? = null): XmlResourceParser? =
        newParser?.copy()?.of(block)?.invokeQuietly<XmlResourceParser>(sourceResId ?: R.layout.layout_hikage_view_tree_node)
            ?: newParserEmptyArg?.copy()?.of(block)?.invokeQuietly<XmlResourceParser>()

    /**
     * Close the `XmlBlock` [block] after a parser has been created.
     *
     * AOSP does the same in `AssetManager.openXmlResourceParser`: the parser increments the block's
     * open count, so closing the block owner does not invalidate the returned parser.
     * @param block the `XmlBlock` instance.
     */
    private fun closeXmlBlock(block: Any) {
        closeXmlBlock?.copy()?.of(block)?.invokeQuietly()
    }

    /**
     * Advance the [parser] to its start element.
     * @param parser the parser.
     */
    private fun advanceToStartTag(parser: XmlResourceParser) {
        runCatching {
            var event = parser.eventType
            while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT)
                event = parser.next()
        }
    }

    /**
     * The synthesized `XmlBlock` cache key.
     */
    private data class CacheKey(
        val packageName: String,
        val resourcePackageName: String,
        val attrs: List<AttributeItem>
    )

    /**
     * Shared source XML block holder.
     */
    private data class SourceXmlBlock(
        val nativePtr: Long,
        val block: Any
    )
}