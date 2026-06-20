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
@file:JvmName("LiveDataStateUtils")

package com.highcapable.hikage.runtime.lifecycle

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.highcapable.betterandroid.ui.extension.component.lifecycleOwner
import com.highcapable.hikage.runtime.StateSubscription

/**
 * Set the [LiveData] state value and bind observation to the [View] lifecycle tree.
 * @param liveData the live data to be observed.
 * @param lifecycleOwner the lifecycle owner to use, or `null` to find it from the view tree.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R : View> R.setState(
    liveData: LiveData<T>,
    lifecycleOwner: LifecycleOwner? = null,
    apply: R.(T) -> Unit
): StateSubscription {
    var observer: Observer<T>? = null
    var isCanceled = false

    liveData.applyInitializedValue {
        apply(it)
    }

    fun resolveOwner() = lifecycleOwner ?: this.lifecycleOwner
        ?: error("LifecycleOwner was not found from View tree. Pass lifecycleOwner explicitly.")

    fun start() {
        if (observer != null || isCanceled) return
        val nextObserver = Observer<T> { apply(it) }
        observer = nextObserver
        liveData.observe(resolveOwner(), nextObserver)
    }

    val listener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(view: View) {
            start()
        }

        override fun onViewDetachedFromWindow(view: View) {
            observer?.let(liveData::removeObserver)
            observer = null
        }
    }

    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) start()

    return StateSubscription {
        if (isCanceled) return@StateSubscription
        isCanceled = true
        observer?.let(liveData::removeObserver)
        observer = null

        removeOnAttachStateChangeListener(listener)
    }
}

/**
 * Set the [LiveData] state value and bind observation to the given [LifecycleOwner].
 * @param liveData the live data to be observed.
 * @param lifecycleOwner the lifecycle owner to use.
 * @param apply the apply body.
 * @return [StateSubscription]
 */
fun <T, R> R.setState(
    liveData: LiveData<T>,
    lifecycleOwner: LifecycleOwner,
    apply: R.(T) -> Unit
): StateSubscription {
    liveData.applyInitializedValue {
        apply(it)
    }

    val observer = Observer<T> { apply(it) }
    liveData.observe(lifecycleOwner, observer)

    return StateSubscription {
        liveData.removeObserver(observer)
    }
}

private fun <T> LiveData<T>.applyInitializedValue(apply: (T) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    if (isInitialized) apply(value as T)
}