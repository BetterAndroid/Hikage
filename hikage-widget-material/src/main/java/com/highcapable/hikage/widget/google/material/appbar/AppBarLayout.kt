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
 * This file is created by fankes on 2025/3/3.
 */
@file:Suppress("unused")
@file:JvmName("AppBarLayoutDeclaration")

package com.highcapable.hikage.widget.google.material.appbar

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.highcapable.hikage.annotation.HikageViewDeclaration

@HikageViewDeclaration(AppBarLayout::class, AppBarLayout.LayoutParams::class)
private object AppBarLayoutDeclaration

@HikageViewDeclaration(MaterialToolbar::class, final = true)
private object MaterialToolbarDeclaration

@HikageViewDeclaration(CollapsingToolbarLayout::class, CollapsingToolbarLayout.LayoutParams::class)
private object CollapsingToolbarLayoutDeclaration