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
 * This file is created by fankes on 2025/3/4.
 */
@file:Suppress("unused", "FunctionName")
@file:JvmName("HikageFactory")

package com.highcapable.hikage.core.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage

/**
 * The [Hikage] factory type.
 *
 * - `parent` the parent view group.
 * - `base` the base view (from previous [HikageFactory] processed, if not will null).
 * - `context` the view context.
 * - `params` the parameters.
 */
typealias HikageFactory = (parent: ViewGroup?, base: View?, context: Context, params: Hikage.PerformerParams) -> View?

/**
 * Create a [Hikage] factory.
 * @param createView the view creator.
 * @return [HikageFactory]
 */
@JvmSynthetic
fun HikageFactory(createView: HikageFactory) = createView

/**
 * Create a [Hikage] factory from [LayoutInflater].
 *
 * This will proxy the function of [LayoutInflater.Factory2] to [Hikage].
 * @param inflater the layout inflater.
 * @return [HikageFactory]
 */
@JvmSynthetic
fun HikageFactory(inflater: LayoutInflater) = HikageFactory { parent, base, context, params ->
    base ?: inflater.factory2?.onCreateView(parent, params.viewClass.name.let {
        if (it.startsWith(Hikage.ANDROID_WIDGET_CLASS_PREFIX))
            it.replace(Hikage.ANDROID_WIDGET_CLASS_PREFIX, "")
        else it
    }, context, params.attrs)
}

/**
 * The [HikageFactory] builder.
 */
class HikageFactoryBuilder private constructor() {

    internal companion object {

        /**
         * Create a [HikageFactoryBuilder].
         * @param builder the builder.
         * @return [HikageFactoryBuilder]
         */
        inline fun create(builder: HikageFactoryBuilder.() -> Unit) = HikageFactoryBuilder().apply(builder)
    }

    /** Caches factories. */
    private val factories = mutableListOf<HikageFactory>()

    /**
     * Add a factory.
     * @param factory the factory.
     */
    fun add(factory: HikageFactory) {
        factories.add(factory)
    }

    /**
     * Add factories.
     * @param factories the factories.
     */
    fun addAll(factories: List<HikageFactory>) {
        this.factories.addAll(factories)
    }

    /**
     * Build the factory.
     * @return <[List]>[HikageFactory]
     */
    @JvmSynthetic
    internal fun build() = factories.toList()
}