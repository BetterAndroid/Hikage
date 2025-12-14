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
@file:JvmName("DialogUtils")

package com.highcapable.hikage.extension

import android.app.AlertDialog
import android.app.Dialog
import android.widget.FrameLayout
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable
import androidx.appcompat.app.AlertDialog as AndroidXAlertDialog

/**
 * @see AlertDialog.setView
 * @see Hikageable
 * @return [Hikage]
 */
fun AlertDialog.setView(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
) = Hikageable(context = context, factory = factory, performer = performer).apply { setView(root) }

/**
 * @see AlertDialog.setView
 * @see Hikage
 */
fun AlertDialog.setView(hikage: Hikage) = setView(hikage.root)

/**
 * @see AlertDialog.setView
 * @see Hikage.Delegate
 * @return [Hikage]
 */
fun AlertDialog.setView(delegate: Hikage.Delegate<*>) =
    delegate.create(context).apply { setView(root) }

/**
 * @see AlertDialog.Builder.setView
 * @see Hikageable
 * @return [AlertDialog.Builder]
 */
fun AlertDialog.Builder.setView(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
): AlertDialog.Builder = setView(Hikageable(context = context, factory = factory, performer = performer).root)

/**
 * @see AlertDialog.Builder.setView
 * @see Hikage
 * @return [AlertDialog.Builder]
 */
fun AlertDialog.Builder.setView(hikage: Hikage): AlertDialog.Builder = setView(hikage.root)

/**
 * @see AlertDialog.Builder.setView
 * @see Hikage.Delegate
 * @return [AlertDialog.Builder]
 */
fun AlertDialog.Builder.setView(delegate: Hikage.Delegate<*>): AlertDialog.Builder =
    setView(delegate.create(context).root)

/**
 * @see AndroidXAlertDialog.setView
 * @see Hikageable
 * @return [Hikage]
 */
fun AndroidXAlertDialog.setView(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
) = Hikageable(context = context, factory = factory, performer = performer).apply { setView(root) }

/**
 * @see AndroidXAlertDialog.setView
 * @see Hikage
 */
fun AndroidXAlertDialog.setView(hikage: Hikage) = setView(hikage.root)

/**
 * @see AndroidXAlertDialog.setView
 * @see Hikage.Delegate
 * @return [Hikage]
 */
fun AndroidXAlertDialog.setView(delegate: Hikage.Delegate<*>) =
    delegate.create(context).apply { setView(root) }

/**
 * @see AndroidXAlertDialog.Builder.setView
 * @see Hikageable
 * @return [AndroidXAlertDialog.Builder]
 */
fun AndroidXAlertDialog.Builder.setView(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
): AndroidXAlertDialog.Builder = setView(Hikageable(context = context, factory = factory, performer = performer).root)

/**
 * @see AndroidXAlertDialog.Builder.setView
 * @see Hikage
 * @return [AndroidXAlertDialog.Builder]
 */
fun AndroidXAlertDialog.Builder.setView(hikage: Hikage): AndroidXAlertDialog.Builder = setView(hikage.root)

/**
 * @see AndroidXAlertDialog.Builder.setView
 * @see Hikage.Delegate
 * @return [AndroidXAlertDialog.Builder]
 */
fun AndroidXAlertDialog.Builder.setView(delegate: Hikage.Delegate<*>): AndroidXAlertDialog.Builder =
    setView(delegate.create(context).root)

/**
 * @see Dialog.setContentView
 * @see Hikageable
 * @return [Hikage]
 */
fun Dialog.setContentView(
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<FrameLayout.LayoutParams>
) = Hikageable(context = context, factory = factory, performer = performer).apply { setContentView(root) }

/**
 * @see Dialog.setContentView
 * @see Hikage
 */
fun Dialog.setContentView(hikage: Hikage) = setContentView(hikage.root)

/**
 * @see Dialog.setContentView
 * @see Hikage.Delegate
 * @return [Hikage]
 */
fun Dialog.setContentView(delegate: Hikage.Delegate<*>) =
    delegate.create(context).apply { setContentView(root) }