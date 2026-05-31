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
 * This file is created by fankes on 2025/5/2.
 */
@file:Suppress("unused")
@file:JvmName("StateUtils")

package com.highcapable.hikage.core.runtime

import android.view.View
import com.highcapable.hikage.core.Hikage
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Definition a [Hikage] runtime state interface.
 */
interface State<T> : ReadWriteProperty<Any?, T>

/**
 * Definition a [Hikage] runtime state observer.
 */
fun interface StateObserver<in T> {

    /**
     * Called when the state value changed.
     * @param value the changed value.
     */
    fun onChanged(value: T)
}

/**
 * Definition a [Hikage] runtime state subscription.
 */
fun interface StateSubscription {

    /** Cancel the state observer subscription. */
    fun cancel()
}

/**
 * Definition a [Hikage] runtime state interface for non-nullable type.
 */
interface NonNullState<T> : State<T> {

    /** The current value of the state. */
    var value: T

    /**
     * Observe the state changes.
     * @param observer the observer to be notified when the state changes.
     * @return [StateSubscription]
     */
    fun observe(observer: StateObserver<T>): StateSubscription

    /**
     * Observe the state changes.
     * @param observer the observer to be notified when the state changes.
     * @return [StateSubscription]
     */
    fun observe(observer: (T) -> Unit) = observe(StateObserver { observer(it) })
}

/**
 * Definition a [Hikage] runtime state interface for nullable type.
 */
interface NullableState<T> : State<T?> {

    /** The current value of the state. */
    var value: T?

    /**
     * Observe the state changes.
     * @param observer the observer to be notified when the state changes.
     * @return [StateSubscription]
     */
    fun observe(observer: StateObserver<T?>): StateSubscription

    /**
     * Observe the state changes.
     * @param observer the observer to be notified when the state changes.
     * @return [StateSubscription]
     */
    fun observe(observer: (T?) -> Unit) = observe(StateObserver { observer(it) })
}

/**
 * Implementing the [State] interface mutable state of [Hikage].
 */
class MutableState<T> private constructor() {

    /**
     * The non-nullable state of [Hikage].
     */
    class NonNull<T> internal constructor(private var holder: T) : NonNullState<T> {

        private val observers = mutableSetOf<StateObserver<T>>()

        override var value get() = holder
            set(value) {
                if (holder == value) return
                holder = value
                observers.toList().forEach { it.onChanged(value) }
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>) = value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
        }

        override fun observe(observer: StateObserver<T>): StateSubscription {
            observers += observer
            observer.onChanged(value)

            return StateSubscription {
                observers -= observer
            }
        }
    }

    /**
     * The nullable state of [Hikage].
     */
    class Nullable<T> internal constructor(private var holder: T?) : NullableState<T?> {

        private val observers = mutableSetOf<StateObserver<T?>>()

        override var value get() = holder
            set(value) {
                if (holder == value) return
                holder = value
                observers.toList().forEach { it.onChanged(value) }
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>) = value

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            this.value = value
        }

        override fun observe(observer: StateObserver<T?>): StateSubscription {
            observers += observer
            observer.onChanged(value)

            return StateSubscription {
                observers -= observer
            }
        }
    }
}

/**
 * Create a mutable state of [Hikage] with the specified value.
 * @param value the initial value of the state.
 * @return [MutableState.NonNull]
 */
fun <T> mutableStateOf(value: T) = MutableState.NonNull(value)

/**
 * Create a mutable state of [Hikage] with the specified value.
 * @param value the initial value of the state.
 * @return [MutableState.Nullable]
 */
fun <T> mutableStateOfNull(value: T? = null) = MutableState.Nullable(value)

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