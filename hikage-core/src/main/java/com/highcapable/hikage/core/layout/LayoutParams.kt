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
 * This file is created by fankes on 2026/5/31.
 */
@file:Suppress("UNCHECKED_CAST")

package com.highcapable.hikage.core.layout

import android.view.ViewGroup
import com.highcapable.betterandroid.ui.extension.view.LayoutParamsWrapContent
import com.highcapable.betterandroid.ui.extension.view.ViewLayoutParams
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.LayoutParamsBody
import com.highcapable.hikage.core.base.PerformerException
import com.highcapable.hikage.core.layout.session.LayoutSession
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.extension.createInstanceOrNull
import com.highcapable.kavaref.resolver.MethodResolver
import kotlin.reflect.KClass

/**
 * The [Hikage.Performer] layout params.
 * @see ViewLayoutParams
 * @param session the current [LayoutSession].
 * @param lpClass the layout params type.
 * @param parent the parent view group.
 */
class LayoutParams private constructor(
    private val session: LayoutSession,
    private val lpClass: KClass<ViewGroup.LayoutParams>,
    private val parent: ViewGroup?
) {

    /**
     * Builder params of body.
     */
    private class BodyBuilder(
        val width: Int?,
        val height: Int?,
        val matchParent: Boolean,
        val widthMatchParent: Boolean,
        val heightMatchParent: Boolean,
        val body: LayoutParamsBody<ViewGroup.LayoutParams>
    )

    /**
     * Builder params of wrapper.
     */
    private class WrapperBuilder(
        val delegate: LayoutParams?,
        val lparams: ViewGroup.LayoutParams?
    )

    /** The layout params body. */
    private var bodyBuilder: BodyBuilder? = null

    /** The layout params wrapper. */
    private var wrapperBuilder: WrapperBuilder? = null

    internal companion object {

        /** The [ViewGroup.generateLayoutParams] method resolver map. */
        private val generateLayoutParamsResolvers = mutableMapOf<KClass<out ViewGroup>, MethodResolver<ViewGroup>?>()

        /**
         * Create a new [LayoutParams].
         * @see ViewLayoutParams
         * @param session the current [LayoutSession].
         * @param parent the parent view group.
         * @return [LayoutParams]
         */
        fun <LP : ViewGroup.LayoutParams> from(
            session: LayoutSession,
            lpClass: KClass<LP>,
            parent: ViewGroup?,
            width: Int?,
            height: Int?,
            matchParent: Boolean,
            widthMatchParent: Boolean,
            heightMatchParent: Boolean,
            body: LayoutParamsBody<LP>
        ) = LayoutParams(session, lpClass as KClass<ViewGroup.LayoutParams>, parent).apply {
            bodyBuilder = BodyBuilder(
                width, height, matchParent, widthMatchParent, heightMatchParent,
                body as LayoutParamsBody<ViewGroup.LayoutParams>
            )
        }

        /**
         * Create a new [LayoutParams].
         * @param session the current [LayoutSession].
         * @param parent the parent view group.
         * @param delegate the delegate.
         * @param lparams the another layout params.
         * @return [LayoutParams]
         */
        fun <LP : ViewGroup.LayoutParams> from(
            session: LayoutSession,
            lpClass: KClass<LP>,
            parent: ViewGroup?,
            delegate: LayoutParams?,
            lparams: ViewGroup.LayoutParams? = null
        ) = LayoutParams(session, lpClass as KClass<ViewGroup.LayoutParams>, parent).apply {
            wrapperBuilder = WrapperBuilder(delegate, lparams)
        }

        /**
         * Get [ViewGroup.generateLayoutParams] method resolver.
         * @receiver the parent view group.
         * @return [MethodResolver] or null.
         */
        private fun ViewGroup.generateLayoutParamsResolver(): MethodResolver<ViewGroup>? {
            val viewClass = this::class
            if (generateLayoutParamsResolvers.containsKey(viewClass))
                return generateLayoutParamsResolvers[viewClass]

            val resolver = asResolver().optional(silent = true).firstMethodOrNull {
                name = "generateLayoutParams"
                parameters(ViewGroup.LayoutParams::class)
                superclass()
            }
            generateLayoutParamsResolvers[viewClass] = resolver

            return resolver
        }
    }

    /**
     * Create a default layout params.
     * @return [ViewGroup.LayoutParams]
     */
    private fun createDefaultLayoutParams(lparams: ViewGroup.LayoutParams? = null): ViewGroup.LayoutParams {
        val wrapped = lparams?.let {
            parent?.generateLayoutParamsResolver()
                ?.copy()?.of(parent)
                ?.invokeQuietly<ViewGroup.LayoutParams>(it)
        } ?: lparams

        return wrapped
            // Build a default.
            ?: lpClass.createInstanceOrNull(LayoutParamsWrapContent, LayoutParamsWrapContent)
            ?: throw PerformerException("Create default layout params failed.")
    }

    /**
     * Create the layout params.
     * @return [ViewGroup.LayoutParams]
     */
    fun create(): ViewGroup.LayoutParams {
        if (bodyBuilder == null && wrapperBuilder == null) throw PerformerException("No layout params builder found.")

        return bodyBuilder?.let {
            val lparams = ViewLayoutParams(lpClass, it.width, it.height, it.matchParent, it.widthMatchParent, it.heightMatchParent)
            session.requireNoPerformers(lparams::class.qualifiedName) { it.body(lparams) }

            lparams
        } ?: wrapperBuilder?.let {
            val lparams = it.delegate?.create() ?: it.lparams

            createDefaultLayoutParams(lparams)
        } ?: throw PerformerException("Internal error of build layout params.")
    }
}