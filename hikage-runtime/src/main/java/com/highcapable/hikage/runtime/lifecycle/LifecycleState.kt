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
@file:JvmName("LifecycleStateUtils")

package com.highcapable.hikage.runtime.lifecycle

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.highcapable.betterandroid.ui.extension.component.launch
import com.highcapable.betterandroid.ui.extension.component.lifecycleOwner
import com.highcapable.hikage.runtime.StateSubscription
import kotlinx.coroutines.Job

/**
 * Bind the lifecycle state to the [block] and execute it when the lifecycle is active.
 * @receiver the view to bind the lifecycle state.
 * @param lifecycleOwner the lifecycle owner to bind, if null, it will be found from the view tree.
 * @param minActiveState the minimum active state to execute the block, default is [Lifecycle.State.STARTED].
 * @param block the block to execute when the lifecycle is active.
 * @return [StateSubscription] to cancel the subscription.
 */
internal fun View.bindLifecycleState(
    lifecycleOwner: LifecycleOwner?,
    minActiveState: Lifecycle.State,
    block: suspend () -> Unit
): StateSubscription {
    var job: Job? = null
    var isCanceled = false

    fun resolveOwner() = lifecycleOwner ?: this.lifecycleOwner
        ?: error("LifecycleOwner was not found from View tree. Pass lifecycleOwner explicitly.")

    fun start() {
        if (job != null || isCanceled) return
        val owner = resolveOwner()
        job = owner.launch {
            owner.repeatOnLifecycle(minActiveState) {
                block()
            }
        }
    }

    val listener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(view: View) {
            start()
        }

        override fun onViewDetachedFromWindow(view: View) {
            job?.cancel()
            job = null
        }
    }

    addOnAttachStateChangeListener(listener)
    if (isAttachedToWindow) start()

    return StateSubscription {
        if (isCanceled) return@StateSubscription
        isCanceled = true
        job?.cancel()
        job = null

        removeOnAttachStateChangeListener(listener)
    }
}

/**
 * Bind the lifecycle state to the [block] and execute it when the lifecycle is active.
 * @receiver the lifecycle owner to bind.
 * @param minActiveState the minimum active state to execute the block, default is [Lifecycle.State.STARTED].
 * @param block the block to execute when the lifecycle is active.
 * @return [StateSubscription] to cancel the subscription.
 */
internal fun LifecycleOwner.bindLifecycleState(
    minActiveState: Lifecycle.State,
    block: suspend () -> Unit
): StateSubscription {
    val job = launch {
        repeatOnLifecycle(minActiveState) {
            block()
        }
    }

    return StateSubscription {
        job.cancel()
    }
}