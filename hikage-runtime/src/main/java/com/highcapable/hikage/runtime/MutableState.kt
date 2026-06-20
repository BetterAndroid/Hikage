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
 * This file is created by fankes on 2026/6/21.
 */
@file:Suppress("unused")
@file:JvmName("MutableStateUtils")

package com.highcapable.hikage.runtime

import com.highcapable.hikage.core.Hikage
import kotlin.reflect.KProperty

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