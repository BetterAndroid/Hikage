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
 * This file is created by fankes on 2025/2/27.
 */
@file:Suppress("unused")
@file:JvmName("ViewGroupUtils")

package com.highcapable.hikage.extension

import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable

/**
 * @see ViewGroup.addView
 * @see Hikageable
 * @return [Hikage]
 */
@JvmName("addViewTyped")
inline fun <reified LP : ViewGroup.LayoutParams> ViewGroup.addView(
    index: Int = -1,
    noinline factory: HikageFactoryBuilder.() -> Unit = {},
    noinline performer: HikagePerformer<LP>
) = Hikageable<LP>(context = context, factory = factory, performer = performer).apply { addView(root, index) }

/**
 * @see ViewGroup.addView
 * @see Hikageable
 * @return [Hikage]
 */
fun ViewGroup.addView(
    index: Int = -1,
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) = addView<ViewGroup.LayoutParams>(index, factory, performer)

/**
 * @see ViewGroup.addView
 * @see Hikage
 */
@JvmOverloads
fun ViewGroup.addView(index: Int = -1, hikage: Hikage) = addView(hikage.root, index)

/**
 * @see ViewGroup.addView
 * @see Hikage.Delegate
 * @return [Hikage]
 */
@JvmOverloads
fun ViewGroup.addView(index: Int = -1, delegate: Hikage.Delegate<*>) =
    delegate.create(context).apply { addView(root, index) }