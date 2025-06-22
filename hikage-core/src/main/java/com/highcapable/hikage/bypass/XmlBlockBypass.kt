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

package com.highcapable.hikage.bypass

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.XmlResourceParser
import android.content.res.loader.AssetsProvider
import android.content.res.loader.ResourcesProvider
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.highcapable.betterandroid.system.extension.tool.SystemVersion
import com.highcapable.betterandroid.ui.extension.view.inflateOrNull
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.core.R
import com.highcapable.yukireflection.factory.classOf
import com.highcapable.yukireflection.factory.lazyClass
import com.highcapable.yukireflection.type.android.AssetManagerClass
import com.highcapable.yukireflection.type.java.BooleanType
import com.highcapable.yukireflection.type.java.IntType
import com.highcapable.yukireflection.type.java.LongType
import com.highcapable.yukireflection.type.java.StringClass
import org.lsposed.hiddenapibypass.HiddenApiBypass
import android.R as Android_R

/**
 * Create a new XmlBlock with system api bypass reflection magic.
 */
internal object XmlBlockBypass {

    /** The path used to load the apk assets represents an APK file. */
    private const val FORMAT_APK = 0

    /** The path used to load the apk assets represents an idmap file. */
    private const val FORMAT_IDMAP = 1

    /** The path used to load the apk assets represents an resources.arsc file. */
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

    /** The xml block class. */
    private val XmlBlockClass by lazyClass("android.content.res.XmlBlock")

    /** Global pointer references object. */
    private var xmlBlock: Long? = null

    /** Global pointer references object. */
    private var blockParser: AutoCloseable? = null

    /** Whether the initialization is done once. */
    private var isInitOnce = false

    /**
     * Initialize.
     * @param context the context.
     */
    fun init(context: Context) {
        // Context may be loaded from the preview and other non-Android platforms, ignoring this.
        if (context.javaClass.name.endsWith("BridgeContext")) return
        init(context.applicationContext.applicationInfo)
    }

    /**
     * Initialize.
     * @param info the application info.
     */
    private fun init(info: ApplicationInfo) {
        if (SystemVersion.isLowOrEqualsTo(SystemVersion.P)) return
        if (isInitOnce) return
        val sourceDir = info.sourceDir
        xmlBlock = when {
            SystemVersion.isHighOrEqualsTo(SystemVersion.R) ->
                // private static native long nativeLoad(@FormatType int format, @NonNull String path,
                //            @PropertyFlags int flags, @Nullable AssetsProvider asset) throws IOException;
                HiddenApiBypass.getDeclaredMethod(
                    ApkAssetsClass, "nativeLoad",
                    IntType, StringClass, IntType, classOf<AssetsProvider>()
                ).apply { isAccessible = true }.invoke(null, FORMAT_APK, sourceDir, PROPERTY_SYSTEM, null)
            SystemVersion.isHighOrEqualsTo(SystemVersion.P) ->
                // private static native long nativeLoad(
                //            @NonNull String path, boolean system, boolean forceSharedLib, boolean overlay)
                //            throws IOException;
                HiddenApiBypass.getDeclaredMethod(
                    ApkAssetsClass, "nativeLoad",
                    StringClass, BooleanType, BooleanType, BooleanType
                ).apply { isAccessible = true }.invoke(null, sourceDir, false, false, false)
            else -> error("Unsupported Android version.")
        } as? Long? ?: error("Failed to create ApkAssets.")
        blockParser = when {
            SystemVersion.isHighOrEqualsTo(36) ->
                // XmlBlock(@Nullable AssetManager assets, long xmlBlock, boolean usesFeatureFlags)
                HiddenApiBypass.getDeclaredConstructor(XmlBlockClass, AssetManagerClass, LongType, BooleanType)
                    .apply { isAccessible = true }
                    .newInstance(null, xmlBlock, false)
            else ->
                // XmlBlock(@Nullable AssetManager assets, long xmlBlock)
                HiddenApiBypass.getDeclaredConstructor(XmlBlockClass, AssetManagerClass, LongType)
                    .apply { isAccessible = true }
                    .newInstance(null, xmlBlock)
        } as? AutoCloseable? ?: error("Failed to create XmlBlock\$Parser.")
        isInitOnce = true
    }

    /**
     * Create a new parser.
     * @param context the context.
     * @param resId the style resource id, default is [Android_R.style.Widget].
     * @return [XmlResourceParser]
     */
    fun newParser(context: Context, @StyleRes resId: Int = Android_R.style.Widget): XmlResourceParser {
        /**
         * Create a view [AttributeSet].
         * @return [XmlResourceParser]
         */
        fun createViewAttrs() = context.layoutInflater.inflateOrNull<HikageAttrsView>(R.layout.layout_hikage_attrs_view)?.attrs
          as? XmlResourceParser? ?: error("Failed to create AttributeSet.")
        return if (SystemVersion.isHighOrEqualsTo(SystemVersion.P)) {
            if (!isInitOnce) return createViewAttrs()
            require(blockParser != null) { "Hikage initialization failed." }
            HiddenApiBypass.getDeclaredMethod(XmlBlockClass, "newParser", IntType)
                .apply { isAccessible = true }
                .invoke(blockParser, resId) as? XmlResourceParser? ?: error("Failed to create parser.")
        } else createViewAttrs()
    }
}