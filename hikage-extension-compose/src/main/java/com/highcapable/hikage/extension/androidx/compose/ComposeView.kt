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
 * This file is created by fankes on 2025/2/28.
 */
@file:Suppress("unused", "FunctionName")
@file:JvmName("ComposeViewPerformer")

package com.highcapable.hikage.extension.androidx.compose

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.highcapable.hikage.annotation.Hikageable
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikageView

/**
 * Composable in [Hikage].
 *
 * Usage:
 *
 * ```kotlin
 * Hikageable {
 *    ComposeView(
 *        lparams = LayoutParams(matchParent = true)
 *    ) {
 *        Text("Hello, Compose in Hikage!")
 *    }
 * }
 * ```
 * @see ComposeView
 * @see Hikage.Performer.View
 */
@Hikageable
fun <LP : ViewGroup.LayoutParams> Hikage.Performer<LP>.ComposeView(
    lparams: Hikage.LayoutParams? = null,
    id: String? = null,
    init: HikageView<ComposeView> = {},
    content: (@Composable () -> Unit)? = null
) {
    View<ComposeView>(lparams, id) {
        init(this)
        content?.let { setContent(it) }
    }
}