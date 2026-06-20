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
 * This file is created by fankes on 2025/3/12.
 */
package com.highcapable.hikage.core.layout.extension

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.FractionRes
import androidx.annotation.IntegerRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.highcapable.hikage.core.Hikage

/**
 * An extension for [Hikage] view scope to provide resources.
 */
interface ResourcesScope {

    /**
     * Get the string from [resId].
     * @param resId the resource id.
     * @param formatArgs the format arguments.
     * @return [String]
     */
    fun stringResource(@StringRes resId: Int, vararg formatArgs: Any): String

    /**
     * Get the plural string from [resId].
     * @param resId the resource id.
     * @param quantity the quantity used to select the plural form.
     * @param formatArgs the format arguments.
     * @return [String]
     */
    fun pluralStringResource(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String

    /**
     * Get the plural text from [resId].
     * @param resId the resource id.
     * @param quantity the quantity used to select the plural form.
     * @return [CharSequence]
     */
    fun pluralTextResource(@PluralsRes resId: Int, quantity: Int): CharSequence

    /**
     * Get the text from [resId].
     * @param resId the resource id.
     * @return [CharSequence]
     */
    fun textResource(@StringRes resId: Int): CharSequence

    /**
     * Get the string array from [resId].
     * @param resId the resource id.
     * @return [Array] of [String]
     */
    fun stringArrayResource(@ArrayRes resId: Int): Array<String>

    /**
     * Get the integer from [resId].
     * @param resId the resource id.
     * @return [Int]
     */
    fun integerResource(@IntegerRes resId: Int): Int

    /**
     * Get the integer array from [resId].
     * @param resId the resource id.
     * @return [IntArray]
     */
    fun integerArrayResource(@ArrayRes resId: Int): IntArray

    /**
     * Get the boolean from [resId].
     * @param resId the resource id.
     * @return [Boolean]
     */
    fun booleanResource(@BoolRes resId: Int): Boolean

    /**
     * Get the color from [resId].
     * @param resId the resource id.
     * @return [Int]
     */
    fun colorResource(@ColorRes resId: Int): Int

    /**
     * Get the [ColorStateList] from [resId].
     * @param resId the resource id.
     * @return [Int]
     */
    fun stateColorResource(@ColorRes resId: Int): ColorStateList

    /**
     * Get the [Drawable] from [resId].
     * @param resId the resource id.
     * @return [Drawable]
     */
    fun drawableResource(@DrawableRes resId: Int): Drawable

    /**
     * Get the [Bitmap] from [resId].
     * @param resId the resource id.
     * @return [Bitmap]
     */
    fun bitmapResource(@DrawableRes resId: Int): Bitmap

    /**
     * Get the dimension from [resId].
     * @param resId the resource id.
     * @return [Float]
     */
    fun dimenResource(@DimenRes resId: Int): Float

    /**
     * Get the dimension pixel size from [resId].
     * @param resId the resource id.
     * @return [Int]
     */
    fun dimenPixelSizeResource(@DimenRes resId: Int): Int

    /**
     * Get the dimension pixel offset from [resId].
     * @param resId the resource id.
     * @return [Int]
     */
    fun dimenPixelOffsetResource(@DimenRes resId: Int): Int

    /**
     * Get the fraction from [resId].
     * @param resId the resource id.
     * @param base the base value of this fraction.
     * @param pbase the parent base value of this fraction.
     * @return [Float]
     */
    fun fractionResource(@FractionRes resId: Int, base: Int, pbase: Int): Float

    /**
     * Get the font from [resId].
     * @param resId the resource id.
     * @return [Typeface] or null.
     */
    fun fontResource(@FontRes resId: Int): Typeface?
}