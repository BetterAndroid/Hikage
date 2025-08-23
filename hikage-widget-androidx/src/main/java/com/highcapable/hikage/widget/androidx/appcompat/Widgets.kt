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
 * This file is created by fankes on 2025/2/26.
 */
@file:Suppress("unused")
@file:JvmName("WidgetsDeclaration")

package com.highcapable.hikage.widget.androidx.appcompat

import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import com.highcapable.hikage.annotation.HikageViewDeclaration
import androidx.appcompat.widget.ActionMenuView as AppCompatActionMenuView
import androidx.appcompat.widget.SearchView as AppCompatSearchView
import androidx.appcompat.widget.Toolbar as AppCompatToolbar

@HikageViewDeclaration(AppCompatRadioButton::class)
private object AppCompatRadioButtonDeclaration

@HikageViewDeclaration(AppCompatRatingBar::class)
private object AppCompatRatingBarDeclaration

@HikageViewDeclaration(AppCompatImageButton::class)
private object AppCompatImageButtonDeclaration

@HikageViewDeclaration(AppCompatEditText::class)
private object AppCompatEditTextDeclaration

@HikageViewDeclaration(AppCompatActionMenuView::class)
private object AppCompatActionMenuViewDeclaration

@HikageViewDeclaration(AppCompatImageView::class)
private object AppCompatImageViewDeclaration

@HikageViewDeclaration(AppCompatButton::class)
private object AppCompatButtonDeclaration

@HikageViewDeclaration(AppCompatSeekBar::class)
private object AppCompatSeekBarDeclaration

@HikageViewDeclaration(AppCompatCheckBox::class)
private object AppCompatCheckBoxDeclaration

@HikageViewDeclaration(AppCompatTextView::class)
private object AppCompatTextViewDeclaration

@HikageViewDeclaration(AppCompatToggleButton::class)
private object AppCompatToggleButtonDeclaration

@HikageViewDeclaration(AppCompatToolbar::class, final = true)
private object AppCompatToolbarDeclaration

@HikageViewDeclaration(AppCompatCheckedTextView::class)
private object AppCompatCheckedTextViewDeclaration

@HikageViewDeclaration(SwitchCompat::class)
private object SwitchCompatDeclaration

@HikageViewDeclaration(AppCompatSearchView::class, final = true)
private object AppCompatSearchViewDeclaration

@HikageViewDeclaration(LinearLayoutCompat::class, LinearLayoutCompat.LayoutParams::class)
private object LinearLayoutCompatDeclaration

@HikageViewDeclaration(AppCompatAutoCompleteTextView::class)
private object AppCompatAutoCompleteTextViewDeclaration

@HikageViewDeclaration(AppCompatSpinner::class)
private object AppCompatSpinnerDeclaration

@HikageViewDeclaration(AppCompatMultiAutoCompleteTextView::class)
private object AppCompatMultiAutoCompleteTextViewDeclaration