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
 * This file is created by fankes on 2025/3/12.
 */
@file:Suppress("unused")
@file:JvmName("HikageViewUtils")

package com.highcapable.hikage.extension.androidx.compose

import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.Hikagable
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer

/**
 * [Hikage] in composable.
 *
 * Usage:
 *
 * ```kotlin
 * Column(
 *    modifier = Modifier.fillMaxSize()
 * ) {
 *     HikageView {
 *         TextView(
 *             lparams = LayoutParams(matchParent = true)
 *         ) {
 *             text = "Hello, Hikage in Compose!"
 *             textSize = 20f
 *         }
 *     }
 * }
 * ```
 * @see AndroidView
 */
@Composable
@UiComposable
fun HikageView(
    modifier: Modifier = Modifier,
    onReset: ((View) -> Unit)? = null,
    onRelease: (View) -> Unit = NoOpUpdate,
    update: (View) -> Unit = {},
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) {
    AndroidView(
        factory = { context ->
            Hikagable(
                context = context,
                factory = factory,
                performer = performer
            ).root
        },
        modifier = modifier,
        onReset = onReset,
        onRelease = onRelease,
        update = update
    )
}

/**
 * [Hikage] in composable.
 * @see AndroidView
 * @param delegate the [Hikage.Delegate] instance.
 */
@Composable
@UiComposable
fun HikageView(
    delegate: Hikage.Delegate<*>,
    modifier: Modifier = Modifier,
    onReset: ((View) -> Unit)? = null,
    onRelease: (View) -> Unit = NoOpUpdate,
    update: (View) -> Unit = {}
) {
    AndroidView(
        factory = { context ->
            delegate.create(context).root
        },
        modifier = modifier,
        onReset = onReset,
        onRelease = onRelease,
        update = update
    )
}