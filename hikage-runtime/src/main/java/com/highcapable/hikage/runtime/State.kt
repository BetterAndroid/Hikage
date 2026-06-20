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
 * This file is created by fankes on 2025/5/2.
 */
@file:Suppress("unused")
@file:JvmName("StateUtils")

package com.highcapable.hikage.runtime

import com.highcapable.hikage.core.Hikage
import kotlin.properties.ReadWriteProperty

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