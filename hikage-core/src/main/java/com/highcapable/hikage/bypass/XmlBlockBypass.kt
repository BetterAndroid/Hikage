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
import android.content.res.AssetManager
import android.content.res.XmlResourceParser
import android.content.res.loader.AssetsProvider
import android.content.res.loader.ResourcesProvider
import android.util.AttributeSet
import androidx.annotation.StyleRes
import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.betterandroid.ui.extension.view.inflateOrNull
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.core.R
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.lazyClass
import com.highcapable.kavaref.resolver.processor.MemberProcessor
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Constructor
import java.lang.reflect.Method
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
    private val XmlBlockClass by lazyClass<AutoCloseable>("android.content.res.XmlBlock")

    /** Global pointer references object. */
    private var xmlBlock: Long? = null

    /** Global pointer references object. */
    private var blockParser: AutoCloseable? = null

    /** Whether the initialization is done once. */
    private var isInitOnce = false

    /** The resolver for member processor. */
    private val resolver = object : MemberProcessor.Resolver() {

        override fun <T : Any> getDeclaredConstructors(declaringClass: Class<T>): List<Constructor<T>> =
            AndroidVersion.require(AndroidVersion.P, super.getDeclaredConstructors(declaringClass)) {
                HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Constructor<T>>().toList()
            }

        override fun <T : Any> getDeclaredMethods(declaringClass: Class<T>): List<Method> =
            AndroidVersion.require(AndroidVersion.P, super.getDeclaredMethods(declaringClass)) {
                HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Method>().toList()
            }
    }

    private val newParser by lazy {
        XmlBlockClass.resolve()
            .processor(resolver)
            .optional()
            .firstMethodOrNull {
                name = "newParser"
                parameters(Int::class)
            }
    }

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
        if (AndroidVersion.isAtMost(AndroidVersion.P)) return
        if (isInitOnce) return

        val sourceDir = info.sourceDir
        xmlBlock = when {
            AndroidVersion.isAtLeast(AndroidVersion.R) ->
                // private static native long nativeLoad(@FormatType int format, @NonNull String path,
                //            @PropertyFlags int flags, @Nullable AssetsProvider asset) throws IOException;
                ApkAssetsClass.resolve()
                    .processor(resolver)
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
                    .processor(resolver)
                    .optional()
                    .firstMethodOrNull {
                        name = "nativeLoad"
                        parameters(String::class, Boolean::class, Boolean::class, Boolean::class)
                        modifiers(Modifiers.NATIVE)
                    }?.invokeQuietly(sourceDir, false, false, false)
            else -> error("Unsupported Android version.")
        } as? Long? ?: error("Failed to create ApkAssets.")

        blockParser = XmlBlockClass.resolve()
            .processor(resolver)
            .optional()
            .firstConstructorOrNull {
                if (AndroidVersion.isAtLeast(AndroidVersion.BAKLAVA))
                    parameters(AssetManager::class, Long::class, Boolean::class)
                else parameters(AssetManager::class, Long::class)
            }?.let {
                if (AndroidVersion.isAtLeast(AndroidVersion.BAKLAVA))
                    it.createQuietly(null, xmlBlock, false)
                else it.createQuietly(null, xmlBlock)
            } ?: error($$"Failed to create XmlBlock$Parser.")

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

        return if (AndroidVersion.isAtLeast(AndroidVersion.P)) {
            if (!isInitOnce) return createViewAttrs()

            require(blockParser != null) { "Hikage initialization failed." }
            newParser?.copy()?.of(blockParser)
                ?.invokeQuietly<XmlResourceParser>(resId)
                ?: error("Failed to create parser.")
        } else createViewAttrs()
    }
}