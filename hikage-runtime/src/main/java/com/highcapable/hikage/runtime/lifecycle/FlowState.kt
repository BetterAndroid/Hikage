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
@file:JvmName("FlowStateUtils")

package com.highcapable.hikage.runtime.lifecycle

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.highcapable.hikage.runtime.StateSubscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

/**
 * Set the [StateFlow] state value and bind collection to the [View] lifecycle tree.
 * @param state the state flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use, or `null` to find it from the view tree.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R : View> R.setState(
    state: StateFlow<T>,
    lifecycleOwner: LifecycleOwner? = null,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
): StateSubscription {
    apply(state.value)
    return bindLifecycleState(lifecycleOwner, minActiveState) {
        state.collectLatest { apply(it) }
    }
}

/**
 * Set the [StateFlow] state value and bind collection to the given [LifecycleOwner].
 * @param state the state flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R> R.setState(
    state: StateFlow<T>,
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
): StateSubscription {
    apply(state.value)
    return lifecycleOwner.bindLifecycleState(minActiveState) {
        state.collectLatest { apply(it) }
    }
}

/**
 * Set the [Flow] state value with an initial value and bind collection to the [View] lifecycle tree.
 * @param flow the flow to be collected.
 * @param initialValue the initial value to apply before the first flow emission.
 * @param lifecycleOwner the lifecycle owner to use, or `null` to find it from the view tree.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R : View> R.setState(
    flow: Flow<T>,
    initialValue: T,
    lifecycleOwner: LifecycleOwner? = null,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
): StateSubscription {
    apply(initialValue)
    return collectState(flow, lifecycleOwner, minActiveState, apply)
}

/**
 * Set the [Flow] state value with an initial value and bind collection to the given [LifecycleOwner].
 * @param flow the flow to be collected.
 * @param initialValue the initial value to apply before the first flow emission.
 * @param lifecycleOwner the lifecycle owner to use.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R> R.setState(
    flow: Flow<T>,
    initialValue: T,
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
): StateSubscription {
    apply(initialValue)
    return collectState(flow, lifecycleOwner, minActiveState, apply)
}

/**
 * Collect the [Flow] as state and bind collection to the [View] lifecycle tree.
 * @param flow the flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use, or `null` to find it from the view tree.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R : View> R.collectState(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner? = null,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
) = bindLifecycleState(lifecycleOwner, minActiveState) {
    flow.collectLatest { apply(it) }
}

/**
 * Collect the [Flow] as state and bind collection to the given [LifecycleOwner].
 * @param flow the flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R> R.collectState(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    apply: R.(T) -> Unit
) = lifecycleOwner.bindLifecycleState(minActiveState) {
    flow.collectLatest { apply(it) }
}

/**
 * Collect the [Flow] as a lifecycle-aware effect and bind collection to the [View] lifecycle tree.
 * @param flow the effect flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use, or `null` to find it from the view tree.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param collect the collect body.
 * @return [StateSubscription]
 */
fun <T, R : View> R.collectEffect(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner? = null,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collect: R.(T) -> Unit
) = bindLifecycleState(lifecycleOwner, minActiveState) {
    flow.collect { collect(it) }
}

/**
 * Collect the [Flow] as a lifecycle-aware effect and bind collection to the given [LifecycleOwner].
 * @param flow the effect flow to be collected.
 * @param lifecycleOwner the lifecycle owner to use.
 * @param minActiveState the minimum active lifecycle state to collect.
 * @param collect the collect body.
 * @return [StateSubscription]
 */
fun <T, R> R.collectEffect(
    flow: Flow<T>,
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collect: R.(T) -> Unit
) = lifecycleOwner.bindLifecycleState(minActiveState) {
    flow.collect { collect(it) }
}