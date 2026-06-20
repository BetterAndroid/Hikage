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
 * This file is created by fankes on 2025/3/13.
 */
@file:Suppress("unused")
@file:JvmName("RelativeLayoutUtils")

package com.highcapable.hikage.extension.widget

import android.widget.RelativeLayout
import androidx.annotation.IdRes

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.LEFT_OF
 */
fun RelativeLayout.LayoutParams.leftOf(@IdRes id: Int) = addRule(RelativeLayout.LEFT_OF, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.RIGHT_OF
 */
fun RelativeLayout.LayoutParams.rightOf(@IdRes id: Int) = addRule(RelativeLayout.RIGHT_OF, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ABOVE
 */
fun RelativeLayout.LayoutParams.above(@IdRes id: Int) = addRule(RelativeLayout.ABOVE, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.BELOW
 */
fun RelativeLayout.LayoutParams.below(@IdRes id: Int) = addRule(RelativeLayout.BELOW, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_BASELINE
 */
fun RelativeLayout.LayoutParams.alignBaseline(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_BASELINE, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_LEFT
 */
fun RelativeLayout.LayoutParams.alignLeft(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_LEFT, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_TOP
 */
fun RelativeLayout.LayoutParams.alignTop(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_TOP, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_RIGHT
 */
fun RelativeLayout.LayoutParams.alignRight(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_RIGHT, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_BOTTOM
 */
fun RelativeLayout.LayoutParams.alignBottom(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_BOTTOM, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_LEFT
 */
fun RelativeLayout.LayoutParams.alignParentLeft() = addRule(RelativeLayout.ALIGN_PARENT_LEFT)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_TOP
 */
fun RelativeLayout.LayoutParams.alignParentTop() = addRule(RelativeLayout.ALIGN_PARENT_TOP)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_RIGHT
 */
fun RelativeLayout.LayoutParams.alignParentRight() = addRule(RelativeLayout.ALIGN_PARENT_RIGHT)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_BOTTOM
 */
fun RelativeLayout.LayoutParams.alignParentBottom() = addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.CENTER_IN_PARENT
 */
fun RelativeLayout.LayoutParams.centerInParent() = addRule(RelativeLayout.CENTER_IN_PARENT)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.CENTER_HORIZONTAL
 */
fun RelativeLayout.LayoutParams.centerHorizontally() = addRule(RelativeLayout.CENTER_HORIZONTAL)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.CENTER_VERTICAL
 */
fun RelativeLayout.LayoutParams.centerVertically() = addRule(RelativeLayout.CENTER_VERTICAL)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.START_OF
 */
fun RelativeLayout.LayoutParams.startOf(@IdRes id: Int) = addRule(RelativeLayout.START_OF, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.END_OF
 */
fun RelativeLayout.LayoutParams.endOf(@IdRes id: Int) = addRule(RelativeLayout.END_OF, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_START
 */
fun RelativeLayout.LayoutParams.alignStart(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_START, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_END
 */
fun RelativeLayout.LayoutParams.alignEnd(@IdRes id: Int) = addRule(RelativeLayout.ALIGN_END, id)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_START
 */
fun RelativeLayout.LayoutParams.alignParentStart() = addRule(RelativeLayout.ALIGN_PARENT_START)

/**
 * @see RelativeLayout.LayoutParams.addRule
 * @see RelativeLayout.ALIGN_PARENT_END
 */
fun RelativeLayout.LayoutParams.alignParentEnd() = addRule(RelativeLayout.ALIGN_PARENT_END)