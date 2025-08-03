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
@file:Suppress("unused", "FunctionName")

package com.highcapable.hikage.core.preview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.builder.HikageBuilder

/**
 * A preview layout for [Hikage].
 *
 * - Note: This class should only be used in the layout preview mode by [View.isInEditMode].
 */
abstract class HikagePreview(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs), HikageBuilder {

    init {
        require(isInEditMode) {
            "HikagePreview should only be used in the layout preview mode."
        }
    }

    @CallSuper
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        removeAllViews()
        build().create(context, parent = this)
    }
}