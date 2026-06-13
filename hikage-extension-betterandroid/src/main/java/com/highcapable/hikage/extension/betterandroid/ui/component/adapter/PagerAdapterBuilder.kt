/*
 * Hikage - A Kotlin DSL-based Android real-time UI building framework.
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
 * This file is created by fankes on 2025/3/10.
 */
@file:Suppress("LocalVariableName")
@file:JvmName("PagerAdapterBuilderUtils")

package com.highcapable.hikage.extension.betterandroid.ui.component.adapter

import android.view.ViewGroup
import com.highcapable.betterandroid.ui.component.adapter.PagerAdapterBuilder
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.extension.betterandroid.ui.component.adapter.viewholder.HikageHolderDelegate

/**
 * Create and add view holder from [Hikage.Delegate].
 *
 * Usage:
 *
 * ```kotlin
 * onBindPageView(
 *     Hikageable = {
 *         LinearLayout(
 *             id = "container",
 *             lparams = LayoutParams(matchParent = true),
 *             init = {
 *                 orientation = LinearLayout.VERTICAL
 *                 gravity = Gravity.CENTER
 *             }
 *         ) {
 *             TextView(
 *                 id = "text",
 *                 lparams = LayoutParams {
 *                     gravity = Gravity.CENTER
 *                 }
 *             ) {
 *                 text = "Page"
 *                 textSize = 20f
 *             }
 *         }
 *    }
 * ) { hikage, entity, position ->
 *     hikage.get<TextView>("text").text = "Page ${entity.name} of ${position + 1}"
 * }
 * ```
 * @see PagerAdapterBuilder.onBindPageView
 * @receiver [PagerAdapterBuilder]<[E]>
 * @param Hikageable the performer body.
 * @return [PagerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> PagerAdapterBuilder<E>.onBindPageView(
    Hikageable: HikagePerformer<ViewGroup.LayoutParams>,
    viewHolder: (hikage: Hikage, entity: E, position: Int) -> Unit = { _, _, _ -> }
) = onBindPageView(Hikageable(performer = Hikageable), viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate].
 * @see PagerAdapterBuilder.onBindPageView
 * @receiver [PagerAdapterBuilder]<[E]>
 * @param delegate the delegate.
 * @return [PagerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> PagerAdapterBuilder<E>.onBindPageView(
    delegate: Hikage.Delegate<*>,
    viewHolder: (hikage: Hikage, entity: E, position: Int) -> Unit = { _, _, _ -> }
) = onBindPageView(HikageHolderDelegate(delegate), viewHolder)