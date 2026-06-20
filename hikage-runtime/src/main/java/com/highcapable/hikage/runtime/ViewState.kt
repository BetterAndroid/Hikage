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
 * This file is created by fankes on 2026/6/21.
 */
@file:Suppress("unused")
@file:JvmName("ViewStateUtils")

package com.highcapable.hikage.runtime

import android.view.View
import com.highcapable.hikage.core.Hikage

/**
 * Set the [Hikage] state value.
 *
 * Usage:
 *
 * ```kotlin
 * val textState = mutableStateOf("Hello World!")
 * var text by textState
 * TextView {
 *     setState(textState) {
 *         text = it
 *     }
 * }
 * // Modify the state.
 * text = "Hello Hikage!"
 * ```
 *
 * This function binds the state observer to the [View] lifecycle.
 *
 * The observer will be subscribed when the [View] is attached to window and automatically canceled when it is detached.
 * @param state the state to be set.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
inline fun <T, R : View> R.setState(state: NonNullState<T>, crossinline apply: R.(T) -> Unit): StateSubscription {
    var subscription: StateSubscription? = null
    var isCanceled = false

    val observer = StateObserver<T> {
        this.apply(it)
    }
    val listener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(view: View) {
            if (subscription != null || isCanceled) return
            val next = state.observe(observer)
            if (isAttachedToWindow && !isCanceled) subscription = next
            else next.cancel()
        }

        override fun onViewDetachedFromWindow(view: View) {
            subscription?.cancel()
            subscription = null
        }
    }

    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) {
        val next = state.observe(observer)
        if (isAttachedToWindow && !isCanceled) subscription = next
        else next.cancel()
    } else apply(state.value)

    return StateSubscription {
        if (isCanceled) return@StateSubscription
        isCanceled = true
        subscription?.cancel()
        subscription = null

        removeOnAttachStateChangeListener(listener)
    }
}

/**
 * Set the [Hikage] state value.
 * @see setState
 * @param state the state to be set.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
inline fun <T, R : View> R.setState(state: NullableState<T>, crossinline apply: R.(T?) -> Unit): StateSubscription {
    var subscription: StateSubscription? = null
    var isCanceled = false

    val observer = StateObserver<T?> {
        this.apply(it)
    }
    val listener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(view: View) {
            if (subscription != null || isCanceled) return
            val next = state.observe(observer)
            if (isAttachedToWindow && !isCanceled) subscription = next
            else next.cancel()
        }

        override fun onViewDetachedFromWindow(view: View) {
            subscription?.cancel()
            subscription = null
        }
    }

    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) {
        val next = state.observe(observer)
        if (isAttachedToWindow && !isCanceled) subscription = next
        else next.cancel()
    } else apply(state.value)

    return StateSubscription {
        if (isCanceled) return@StateSubscription
        isCanceled = true
        subscription?.cancel()
        subscription = null

        removeOnAttachStateChangeListener(listener)
    }
}

/**
 * Set the [Hikage] state value.
 *
 * This function creates a long lifecycle observer and will not be automatically canceled.
 *
 * If you use it with short lifecycle objects, keep the returned [StateSubscription] and call [StateSubscription.cancel] manually.
 * @param state the state to be set.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
inline fun <T, R> R.setState(state: NonNullState<T>, crossinline apply: R.(T) -> Unit) =
    state.observe(StateObserver { this.apply(it) })

/**
 * Set the [Hikage] state value.
 *
 * This function creates a long lifecycle observer and will not be automatically canceled.
 *
 * If you use it with short lifecycle objects, keep the returned [StateSubscription] and call [StateSubscription.cancel] manually.
 * @see setState
 * @param state the state to be set.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
inline fun <T, R> R.setState(state: NullableState<T>, crossinline apply: R.(T?) -> Unit) =
    state.observe(StateObserver { this.apply(it) })