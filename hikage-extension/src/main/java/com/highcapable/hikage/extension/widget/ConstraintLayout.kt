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
 * This file is created by fankes on 2025/3/13.
 */
@file:Suppress("unused")
@file:JvmName("ConstraintLayoutUtils")

package com.highcapable.hikage.extension.widget

import androidx.constraintlayout.widget.ConstraintLayout

/**
 * @see ConstraintLayout.LayoutParams.leftToLeft
 */
fun ConstraintLayout.LayoutParams.leftToParent() {
    leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.rightToRight
 */
fun ConstraintLayout.LayoutParams.rightToParent() {
    rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.startToStart
 */
fun ConstraintLayout.LayoutParams.startToParent() {
    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.endToEnd
 */
fun ConstraintLayout.LayoutParams.endToParent() {
    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.topToTop
 */
fun ConstraintLayout.LayoutParams.topToParent() {
    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.bottomToBottom
 */
fun ConstraintLayout.LayoutParams.bottomToParent() {
    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
}

/**
 * @see ConstraintLayout.LayoutParams.startToParent
 * @see ConstraintLayout.LayoutParams.endToParent
 */
fun ConstraintLayout.LayoutParams.centerHorizontally() {
    startToParent()
    endToParent()
}

/**
 * @see ConstraintLayout.LayoutParams.topToParent
 * @see ConstraintLayout.LayoutParams.bottomToParent
 */
fun ConstraintLayout.LayoutParams.centerVertically() {
    topToParent()
    bottomToParent()
}

/**
 * @see ConstraintLayout.LayoutParams.centerHorizontally
 * @see ConstraintLayout.LayoutParams.centerVertically
 */
fun ConstraintLayout.LayoutParams.centerInParent() {
    centerHorizontally()
    centerVertically()
}