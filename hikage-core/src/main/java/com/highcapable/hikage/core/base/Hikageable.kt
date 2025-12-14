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
 * This file is created by fankes on 2025/2/25.
 */
@file:Suppress("unused", "FunctionName")
@file:JvmName("HikageableUtils")

package com.highcapable.hikage.core.base

import android.content.Context
import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage

/**
 * The [Hikage.Performer] type.
 */
typealias HikagePerformer<LP> = Hikage.Performer<LP>.() -> Unit

/**
 * The [Hikage] view scope type.
 */
typealias HikageView<V> = V.() -> Unit

/** The [Hikage] layout params body type. */
internal typealias LayoutParamsBody<LP> = LP.() -> Unit

/**
 * Start performing a [Hikage] layout [LP].
 * @param context the context to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Hikage]
 */
@JvmName("HikageableTyped")
inline fun <reified LP : ViewGroup.LayoutParams> Hikageable(
    context: Context,
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    noinline factory: HikageFactoryBuilder.() -> Unit = {},
    noinline performer: HikagePerformer<LP>
) = Hikage.create(context, parent, attachToParent, factory, performer)

/**
 * Start performing a [Hikage] layout.
 * @param context the context to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Hikage]
 */
@JvmSynthetic
fun Hikageable(
    context: Context,
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) = Hikage.create(context, parent, attachToParent, factory, performer)

/**
 * Start performing a [Hikage] layout [LP].
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Hikage.Delegate]<[LP]>
 */
@JvmName("HikageableTyped")
inline fun <reified LP : ViewGroup.LayoutParams> Hikageable(
    noinline factory: HikageFactoryBuilder.() -> Unit = {},
    noinline performer: HikagePerformer<LP>
) = Hikage.build<LP>(factory, performer)

/**
 * Start performing a [Hikage] layout.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Hikage.Delegate]<[ViewGroup.LayoutParams]>
 */
@JvmSynthetic
fun Hikageable(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) = Hikage.build(factory, performer)