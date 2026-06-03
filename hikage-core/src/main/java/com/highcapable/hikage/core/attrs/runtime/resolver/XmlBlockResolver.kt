/*
 * Hikage - An Android responsive UI building tool.
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
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.hikage.core.attrs.runtime.resolver

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.XmlResourceParser
import android.content.res.loader.AssetsProvider
import android.content.res.loader.ResourcesProvider
import androidx.annotation.StyleRes
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.hikage.bypass.HiddenApiResolver
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.runtime.builder.BinaryXmlBuilder
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.lazyClass
import org.xmlpull.v1.XmlPullParser
import android.R as Android_R

/**
 * The resolver for creating [XmlResourceParser] from `XmlBlock`.
 */
internal object XmlBlockResolver : AttributeSetResolver {

    /** The path used to load the apk assets represents an APK file. */
    private const val FORMAT_APK = 0

    /** The path used to load the apk assets represents an idmap file. */
    private const val FORMAT_IDMAP = 1

    /** The path used to load the apk assets represents a resources.arsc file. */
    private const val FORMAT_ARSC = 2

    /** The path used to load the apk assets represents a directory. */
    private const val FORMAT_DIR = 3

    /**
     * The apk assets contains framework resource values specified by the system.
     * This allows some functions to filter out this package when computing what
     * configurations/resources are available.
     */
    private const val PROPERTY_SYSTEM = 1 shl 0

    /**
     * The apk assets is a shared library or was loaded as a shared library by force.
     * The package ids of dynamic apk assets are assigned at runtime instead of compile time.
     */
    private const val PROPERTY_DYNAMIC = 1 shl 1

    /**
     * The apk assets has been loaded dynamically using a [ResourcesProvider].
     * Loader apk assets overlay resources like RROs except they are not backed by an idmap.
     */
    private const val PROPERTY_LOADER = 1 shl 2

    /**
     * The apk assets is a RRO.
     * An RRO overlays resource values of its target package.
     */
    private const val PROPERTY_OVERLAY = 1 shl 3

    /**
     * The apk assets is owned by the application running in this process and incremental crash
     * protections for this APK must be disabled.
     */
    private const val PROPERTY_DISABLE_INCREMENTAL_HARDENING = 1 shl 4

    /**
     * The apk assets only contain the overlayable declarations information.
     */
    private const val PROPERTY_ONLY_OVERLAYABLES = 1 shl 5

    /** The apk assets class.  */
    private val ApkAssetsClass by lazyClass("android.content.res.ApkAssets")

    /** The `XmlBlock` class. */
    private val XmlBlockClass by lazyClass("android.content.res.XmlBlock")

    /** Global pointer references object. */
    private var xmlBlock: Long? = null

    /** Global pointer references object. */
    private var blockParser: Any? = null

    /** Whether the initialization is done once. */
    private var isInitOnce = false

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

    /**
     * Initialize the global pointer references for creating [XmlResourceParser] from `XmlBlock`.
     * @param info the application info.
     */
    private fun initParser(info: ApplicationInfo) {
        if (isInitOnce) return

        val sourceDir = info.sourceDir
        xmlBlock = when {
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
                    }?.invokeQuietly(FORMAT_APK, sourceDir, PROPERTY_SYSTEM, null)
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
                    }?.invokeQuietly(sourceDir, false, false, false)
            else -> error("Unsupported Android version.")
        } as? Long? ?: error("Failed to create ApkAssets.")

        blockParser = createXmlBlockFromApkAssets(xmlBlock ?: error("Failed to create ApkAssets."))
            ?: error("Failed to create XmlBlock.")

        isInitOnce = true
    }

    /**
     * Create a new empty parser.
     * @param context the context.
     * @param resId the style resource id, default is [Android_R.style.Widget].
     * @return [XmlResourceParser]
     */
    fun newParser(context: Context, @StyleRes resId: Int = Android_R.style.Widget): XmlResourceParser {
        initParser(context.applicationContext.applicationInfo)

        return if (AndroidVersion.isAtLeast(AndroidVersion.P)) {
            if (!isInitOnce) return createEmptyParser(context)

            val parser = blockParser?.let {
                newParser?.copy()?.of(it)?.invokeQuietly<XmlResourceParser>(resId)
            }
            parser ?: createEmptyParser(context)
        } else createEmptyParser(context)
    }

    override fun newParser(context: Context, attrs: List<AttributeItem>): XmlResourceParser {
        if (attrs.isEmpty()) return newParser(context)

        initParser(context.applicationContext.applicationInfo)
        val data = BinaryXmlBuilder.build(context, attrs)
        val parser = createParserFrom(data) ?: error(
            "Failed to create parser from the synthesized XmlBlock on Android ${AndroidVersion.code}."
        )

        advanceToStartTag(parser)
        return parser
    }

    /**
     * Create an empty parser from a synthesized binary XML.
     * @param context the context.
     * @return [XmlResourceParser]
     */
    private fun createEmptyParser(context: Context): XmlResourceParser {
        val data = BinaryXmlBuilder.build(context, emptyList())
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
     * @return [XmlResourceParser] or null.
     */
    private fun createParserOf(block: Any): XmlResourceParser? =
        newParser?.copy()?.of(block)?.invokeQuietly<XmlResourceParser>(0)
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
}