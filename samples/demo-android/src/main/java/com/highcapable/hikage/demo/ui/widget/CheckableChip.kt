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
 * This file is created by fankes on 2025/4/20.
 */
@file:Suppress("PrivateResource")

package com.highcapable.hikage.demo.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.chip.Chip
import com.highcapable.betterandroid.ui.extension.component.base.getDrawableCompat
import com.highcapable.hikage.annotation.HikageView

@HikageView
class CheckableChip(context: Context, attrs: AttributeSet? = null) : Chip(context, attrs) {

    init {
        isCheckable = true
        isCheckedIconVisible = true
        checkedIcon = context.getDrawableCompat(R.drawable.ic_m3_chip_check)
    }
}