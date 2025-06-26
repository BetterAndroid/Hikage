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
@file:Suppress("unused", "NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
@file:JvmName("PopupWindowUtils")

package com.highcapable.hikage.extension

import android.content.Context
import android.widget.FrameLayout
import android.widget.PopupWindow
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.kavaref.KavaRef.Companion.resolve

/**
 * @see PopupWindow.setContentView
 * @see Hikageable
 * @return [Hikage]
 */
inline fun PopupWindow.setContentView(
    context: Context = requireContext(),
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
) = Hikageable(context = context, factory = factory, performer = performer).apply { setContentView(root) }

/**
 * @see PopupWindow.setContentView
 * @see Hikage
 */
fun PopupWindow.setContentView(hikage: Hikage) {
    contentView = hikage.root
}

/**
 * @see PopupWindow.setContentView
 * @see Hikage.Delegate
 * @return [Hikage]
 */
fun PopupWindow.setContentView(
    context: Context = requireContext(),
    delegate: Hikage.Delegate<*>
) = delegate.create(context).apply { setContentView(root) }

/**
 * Require a context from [PopupWindow].
 * @return [Context]
 */
private fun PopupWindow.requireContext() =
    resolve().optional(silent = true).firstFieldOrNull { name = "mContext" }?.getQuietly<Context>()
        ?: error("Hikage need a Context to create PopupWindow content view.")