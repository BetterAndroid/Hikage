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
 * This file is created by fankes on 2025/2/25.
 */
@file:Suppress("unused")

package com.highcapable.hikage.core

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.highcapable.betterandroid.ui.extension.component.base.DisplayDensity
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.core.base.HikageFactory
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.layout.exception.PerformerException
import com.highcapable.hikage.core.layout.extension.ResourcesScope
import com.highcapable.hikage.core.layout.session.LayoutSession
import kotlin.reflect.KClass

/**
 * The Hikage core layout builder.
 *
 * A [Hikage] can have multiple levels of [Hikage.Performer].
 * @param session the current [LayoutSession].
 */
class Hikage private constructor(internal val session: LayoutSession) {

    companion object {

        /** The Android widget class prefix. */
        internal const val ANDROID_WIDGET_CLASS_PREFIX = "android.widget."

        /**
         * Automatically add [HikageFactory] to handle [LayoutInflater.Factory2].
         *
         * This [LayoutInflater] will be retrieved from the [Context] you passed in.
         *
         * This option is enabled by default and will add this feature when creating any [Hikage].
         * It can support the [View] autoboxing feature that supports libraries such as `androidx.appcompat`,
         * which can be disabled if you don't need it.
         */
        var isAutoProcessWithFactory2 = true

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        @JvmName("createTyped")
        inline fun <reified LP : ViewGroup.LayoutParams> create(
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            noinline factory: HikageFactoryBuilder.() -> Unit = {},
            noinline performer: HikagePerformer<LP>
        ) = create(LP::class, context, parent, attachToParent, factory, performer)

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        fun create(
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<ViewGroup.LayoutParams>
        ) = create(ViewGroup.LayoutParams::class, context, parent, attachToParent, factory, performer)

        /**
         * Create a new [Hikage].
         * @param lpClass the layout params type.
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        fun <LP : ViewGroup.LayoutParams> create(
            lpClass: KClass<LP>,
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<LP>
        ) = Hikage(newSession(context, factory)).apply {
            // If the parent view is specified and mark as attach to parent,
            // add it directly to the first position.
            if (parent != null && attachToParent) session.provideParent(parent)

            session.newPerformer(lpClass, parent, attachToParent, context).apply(performer)
        }

        /**
         * Create a new [Hikage.Delegate].
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[LP]>
         */
        @JvmName("buildTyped")
        inline fun <reified LP : ViewGroup.LayoutParams> build(
            noinline factory: HikageFactoryBuilder.() -> Unit = {},
            noinline performer: HikagePerformer<LP>
        ) = build(LP::class, factory, performer)

        /**
         * Create a new [Hikage.Delegate].
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[ViewGroup.LayoutParams]>
         */
        fun build(
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<ViewGroup.LayoutParams>
        ) = build(ViewGroup.LayoutParams::class, factory, performer)

        /**
         * Create a new [Hikage.Delegate].
         * @param lpClass the layout params type.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[LP]>
         */
        fun <LP : ViewGroup.LayoutParams> build(
            lpClass: KClass<LP>,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<LP>
        ) = Delegate(lpClass, factory, performer)

        /**
         * Create a new [LayoutSession].
         * @param context the context to create the layout.
         * @param factory the [HikageFactory] builder.
         * @return [LayoutSession]
         */
        private fun newSession(
            context: Context,
            factory: HikageFactoryBuilder.() -> Unit
        ) = LayoutSession.create(HikageFactoryBuilder.create {
            if (isAutoProcessWithFactory2) add(HikageFactory(context.layoutInflater))
            factory()
        }.build())
    }

    /**
     * Get the root view.
     * @return [View]
     */
    val root get() = session.rootView

    /**
     * Get the root view [V].
     * @return [V]
     */
    inline fun <reified V : View> root() = root as? V?
        ?: throw PerformerException("Root view is not a type of ${V::class.qualifiedName}.")

    /**
     * Get the view by [id].
     * @param id the view id.
     * @return [View]
     */
    operator fun get(id: String) = getOrNull(id)
        ?: throw PerformerException("View with id \"$id\" not found.")

    /**
     * Get the view by [id].
     * @param id the view id.
     * @return [View] or null.
     */
    fun getOrNull(id: String) = session.findView(id)

    /**
     * Get the view by [id] via [V].
     * @param id the view id.
     * @return [V]
     */
    @JvmName("getTyped")
    inline fun <reified V : View> get(id: String) = get(id) as? V
        ?: throw PerformerException("View with id \"$id\" is not a ${V::class.qualifiedName}.")

    /**
     * Get the view by [id] via [V].
     * @param id the view id.
     * @return [V] or null.
     */
    @JvmName("getOrNullTyped")
    inline fun <reified V : View> getOrNull(id: String) = getOrNull(id) as? V?

    /**
     * Get the actual view id by [id].
     * @param id the view id.
     * @return [Int] or -1.
     */
    fun getActualViewId(id: String) = session.getActualViewId(id)

    /**
     * The [Hikage] attribute scope.
     */
    interface Attribute

    /**
     * The [Hikage] performer scope interface.
     */
    interface Performer<LP : ViewGroup.LayoutParams> : DisplayDensity, ResourcesScope {

        /**
         * Get the actual view id by [id].
         * @param id the view id.
         * @return [Int] or -1.
         */
        fun actualViewId(id: String): Int
    }

    /**
     * The delegate for [Hikage].
     * @param lpClass the layout params type.
     * @param factory the [HikageFactory] builder.
     * @param performer the performer body.
     */
    class Delegate<LP : ViewGroup.LayoutParams> internal constructor(
        private val lpClass: KClass<LP>,
        private val factory: HikageFactoryBuilder.() -> Unit = {},
        private val performer: HikagePerformer<LP>
    ) {

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @return [Hikage]
         */
        fun create(context: Context, parent: ViewGroup? = null, attachToParent: Boolean = parent != null) =
            create(lpClass, context, parent, attachToParent, factory, performer)
    }
}