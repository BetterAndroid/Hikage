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
 * This file is created by fankes on 2025/3/4.
 */
@file:Suppress("unused", "FunctionName")
@file:JvmName("HikageFactoryUtils")

package com.highcapable.hikage.core.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.extension.createViewFromBridgeInflaterOrNull
import com.highcapable.hikage.core.extension.createViewFromInflaterOrNull
import kotlin.reflect.KClass

/**
 * The [Hikage] factory interface.
 */
fun interface HikageFactory {

    /**
     * Create factory view.
     * @param parent the parent view group.
     * @param base the base view (from previous [HikageFactory] processed, if not will null).
     * @param context the view context.
     * @param params the parameters.
     * @return [View] or null.
     */
    fun createView(parent: ViewGroup?, base: View?, context: Context, params: Params): View?

    /**
     * The parameters of the [HikageFactory].
     * @param id the view ID.
     * @param attrs the attributes set.
     * @param viewClass the view class.
     * @param factory the view factory, used to create the view instance.
     */
    @ConsistentCopyVisibility
    data class Params internal constructor(
        val id: String?,
        val attrs: AttributeSet,
        val viewClass: KClass<out View>,
        val factory: ViewConstructor<View>? = null
    )

    /**
     * The configuration of the [HikageFactory].
     * @param privateFactory whether to process the private factory of [LayoutInflater],
     * enabling this may cause a performance decrease.
     * @param privateFactoryViews the list of [View] classes that need to process the private factory of [LayoutInflater].
     * leaving it empty will process all [View] classes.
     */
    data class Config(
        val privateFactory: Boolean = defaultProcessPrivateFactory,
        val privateFactoryViews: List<String> = defaultPrivateFactoryViews
    ) {

        companion object {

            /**
             * Whether to process the private factory of [LayoutInflater] by default.
             * default is false, enabling this may cause a performance decrease.
             */
            var defaultProcessPrivateFactory = false

            /**
             * The list of [View] classes that need to process the private factory of [LayoutInflater] by default.
             * leaving it empty will process all [View] classes.
             */
            var defaultPrivateFactoryViews = emptyList<String>()
        }
    }
}

/**
 * Create a [Hikage] factory from [LayoutInflater].
 *
 * This will proxy the factory pipeline of [LayoutInflater] to [Hikage].
 * @param inflater the layout inflater.
 * @param config the configuration.
 * @return [HikageFactory]
 */
@JvmSynthetic
fun HikageFactory(
    inflater: LayoutInflater,
    config: HikageFactory.Config = HikageFactory.Config()
) = HikageFactory { parent, base, context, params ->
    val fullName = params.viewClass.java.name
    val name = fullName.let {
        if (it.startsWith(Hikage.ANDROID_WIDGET_CLASS_PREFIX))
            it.replace(Hikage.ANDROID_WIDGET_CLASS_PREFIX, "")
        else it
    }
    val processPrivateFactory = config.privateFactory
    val processViews = config.privateFactoryViews

    base ?: inflater.createViewFromInflaterOrNull(parent, name, fullName, context, params.attrs, processPrivateFactory, processViews)
        ?: inflater.createViewFromBridgeInflaterOrNull(name, params.attrs)
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
        this.factories.add(factory)
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
    internal fun build(): List<HikageFactory> = factories
}