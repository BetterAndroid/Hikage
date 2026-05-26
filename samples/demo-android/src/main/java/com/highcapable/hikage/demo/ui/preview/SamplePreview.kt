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
 * This file is created by fankes on 2025/3/12.
 */
@file:Suppress("SetTextI18n")

package com.highcapable.hikage.demo.ui.preview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.highcapable.hikage.core.base.Hikageable
import com.highcapable.hikage.core.preview.HikagePreview
import com.highcapable.hikage.widget.android.widget.Button
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.TextView

class SamplePreview(context: Context, attrs: AttributeSet) : HikagePreview(context, attrs) {

    override fun build() = Hikageable {
        LinearLayout(
            lparams = LayoutParams(matchParent = true),
            init = {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
        ) {
            TextView {
                text = "Hello, World!"
                textSize = 20f
            }
            Button(
                lparams = LayoutParams {
                    topMargin = 20.dp
                }
            ) {
                text = "Test Button"
            }
        }
    }
}