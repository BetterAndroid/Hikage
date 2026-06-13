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
@file:JvmName("RecyclerAdapterBuilderUtils")

package com.highcapable.hikage.extension.betterandroid.ui.component.adapter

import android.view.ViewGroup
import com.highcapable.betterandroid.ui.component.adapter.RecyclerAdapterBuilder
import com.highcapable.betterandroid.ui.component.adapter.entity.AdapterPosition
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.extension.betterandroid.ui.component.adapter.viewholder.HikageHolderDelegate

/**
 * Create and add view holder from [Hikage.Delegate] for a header view.
 *
 * Usage:
 *
 * ```kotlin
 * onBindHeaderView(
 *     Hikageable = {
 *         TextView(
 *             id = "text",
 *             lparams = LayoutParams(widthMatchParent = true)
 *         ) {
 *             text = "Header"
 *             textSize = 20f
 *         }
 *    }
 * ) { hikage ->
 *     hikage.get<TextView>("text").text = "Header with redefined text"
 * }
 * ```
 * @see RecyclerAdapterBuilder.onBindHeaderView
 * @param Hikageable the performer body.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindHeaderView(
    Hikageable: HikagePerformer<ViewGroup.LayoutParams>,
    viewHolder: (hikage: Hikage) -> Unit = {}
) = onBindHeaderView(Hikageable(performer = Hikageable), viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate] for a header view.
 * @see RecyclerAdapterBuilder.onBindHeaderView
 * @param delegate the delegate.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindHeaderView(
    delegate: Hikage.Delegate<*>,
    viewHolder: (hikage: Hikage) -> Unit = {}
) = onBindHeaderView(HikageHolderDelegate(delegate), viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate] for a footer view.
 *
 * Usage:
 *
 * ```kotlin
 * onBindFooterView(
 *     Hikageable = {
 *         TextView(
 *             id = "text",
 *             lparams = LayoutParams(widthMatchParent = true)
 *         ) {
 *             text = "Footer"
 *             textSize = 20f
 *         }
 *    }
 * ) { hikage ->
 *     hikage.get<TextView>("text").text = "Footer with redefined text"
 * }
 * ```
 * @see RecyclerAdapterBuilder.onBindFooterView
 * @param Hikageable the performer body.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindFooterView(
    Hikageable: HikagePerformer<ViewGroup.LayoutParams>,
    viewHolder: (hikage: Hikage) -> Unit = {}
) = onBindFooterView(Hikageable(performer = Hikageable), viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate] for a footer view.
 * @see RecyclerAdapterBuilder.onBindFooterView
 * @param delegate the delegate.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindFooterView(
    delegate: Hikage.Delegate<*>,
    viewHolder: (hikage: Hikage) -> Unit = {}
) = onBindFooterView(HikageHolderDelegate(delegate), viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate].
 *
 * Usage:
 *
 * ```kotlin
 * onBindItemView(
 *     Hikageable = {
 *         TextView(
 *             id = "text",
 *             lparams = LayoutParams(widthMatchParent = true)
 *         ) {
 *             text = "Item"
 *             textSize = 20f
 *         }
 *    }
 * ) { hikage, entity, position ->
 *     hikage.get<TextView>("text").text = "Item ${entity.name} of ${position.value + 1}"
 * }
 * ```
 * @see RecyclerAdapterBuilder.onBindItemView
 * @receiver [RecyclerAdapterBuilder]<[E]>
 * @param Hikageable the performer body.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindItemView(
    Hikageable: HikagePerformer<ViewGroup.LayoutParams>,
    viewType: Int = RecyclerAdapterBuilder.DEFAULT_VIEW_TYPE,
    viewHolder: (hikage: Hikage, entity: E, position: AdapterPosition) -> Unit = { _, _, _ -> }
) = onBindItemView(Hikageable(performer = Hikageable), viewType, viewHolder)

/**
 * Create and add view holder from [Hikage.Delegate].
 * @see RecyclerAdapterBuilder.onBindItemView
 * @receiver [RecyclerAdapterBuilder]<[E]>
 * @param delegate the delegate.
 * @return [RecyclerAdapterBuilder]<[E]>
 */
@JvmOverloads
fun <E> RecyclerAdapterBuilder<E>.onBindItemView(
    delegate: Hikage.Delegate<*>,
    viewType: Int = RecyclerAdapterBuilder.DEFAULT_VIEW_TYPE,
    viewHolder: (hikage: Hikage, entity: E, position: AdapterPosition) -> Unit = { _, _, _ -> }
) = onBindItemView(HikageHolderDelegate(delegate), viewType, viewHolder)