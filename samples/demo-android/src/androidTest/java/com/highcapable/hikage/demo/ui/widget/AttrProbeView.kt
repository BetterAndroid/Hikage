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
 * This file is created by fankes on 2026/6/16.
 */
package com.highcapable.hikage.demo.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.highcapable.hikage.demo.test.R
import android.R as Android_R

/**
 * Probe view used by instrumentation tests to verify framework and custom attributes.
 */
class AttrProbeView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    val mode: Int
    val flags: Int
    val intValue: Int
    val dimensionPx: Int
    val color: Int
    val boolean: Boolean
    val string: String?
    val reference: Int
    val fraction: Float
    val floatValue: Float
    val androidLayoutWidth: Int
    val androidLayoutHeight: Int
    val androidScrollbarStyle: Int
    val androidRequiresFadingEdge: Int
    val androidOverScrollMode: Int
    val androidVerticalScrollbarPosition: Int

    init {
        val customArray = context.obtainStyledAttributes(attrs, R.styleable.AttrProbeView)
        try {
            mode = customArray.getInt(R.styleable.AttrProbeView_hikageProbeMode, Int.MIN_VALUE)
            flags = customArray.getInt(R.styleable.AttrProbeView_hikageProbeFlags, Int.MIN_VALUE)
            intValue = customArray.getInt(R.styleable.AttrProbeView_hikageProbeInt, Int.MIN_VALUE)
            dimensionPx = customArray.getDimensionPixelSize(R.styleable.AttrProbeView_hikageProbeDimension, Int.MIN_VALUE)
            color = customArray.getColor(R.styleable.AttrProbeView_hikageProbeColor, Int.MIN_VALUE)
            boolean = customArray.getBoolean(R.styleable.AttrProbeView_hikageProbeBoolean, false)
            string = customArray.getString(R.styleable.AttrProbeView_hikageProbeString)
            reference = customArray.getResourceId(R.styleable.AttrProbeView_hikageProbeReference, 0)
            fraction = customArray.getFraction(R.styleable.AttrProbeView_hikageProbeFraction, 1, 1, Float.NaN)
            floatValue = customArray.getFloat(R.styleable.AttrProbeView_hikageProbeFloat, Float.NaN)
        } finally {
            customArray.recycle()
        }

        val frameworkAttrs = intArrayOf(
            Android_R.attr.scrollbarStyle,
            Android_R.attr.layout_width,
            Android_R.attr.layout_height,
            Android_R.attr.overScrollMode,
            Android_R.attr.verticalScrollbarPosition,
            Android_R.attr.requiresFadingEdge
        )
        val frameworkArray = context.obtainStyledAttributes(attrs, frameworkAttrs)
        try {
            androidScrollbarStyle = frameworkArray.getInt(0, Int.MIN_VALUE)
            androidLayoutWidth = frameworkArray.getLayoutDimension(1, Int.MIN_VALUE)
            androidLayoutHeight = frameworkArray.getLayoutDimension(2, Int.MIN_VALUE)
            androidOverScrollMode = frameworkArray.getInt(3, Int.MIN_VALUE)
            androidVerticalScrollbarPosition = frameworkArray.getInt(4, Int.MIN_VALUE)
            androidRequiresFadingEdge = frameworkArray.getInt(5, Int.MIN_VALUE)
        } finally {
            frameworkArray.recycle()
        }
    }
}