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
 * This file is created by fankes on 2025/3/23.
 */
package com.highcapable.hikage.annotation

import android.view.View
import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage
import kotlin.reflect.KClass

/**
 * Declare annotations to generate the [Hikage.Performer] function for the specified [View] at compile time.
 *
 * Usage:
 *
 * ```kotlin
 * @HikageView(lparams = FrameLayout.LayoutParams::class, alias = "MyView")
 * class MyView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
 *     // ...
 * }
 * ```
 * @param lparams the layout params class, only [ViewGroup] can be specified,
 * after specifying, the `performer` parameter will be generated for the function.
 * The parameters must be a class inherited from [ViewGroup.LayoutParams],
 * if the current [View] does not inherit from [ViewGroup], this parameter will be ignored and warned.
 * @param alias the view's class name alias will naming the function, default is the class name.
 * @param requireInit whether to force the `init` parameter to be called, default is false.
 * @param requirePerformer whether to force the `performer` parameter to be called, default is false,
 * this parameter will be ignored when no `performer` parameter is needed here.
 * @param final whether to declare this layout as "final layout", default is false, that is,
 * whether this layout inherits from or is [ViewGroup], the `performer` parameter will not be generated.
 * After set to `true`, [lparams] and [requirePerformer] will no longer be valid.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class HikageView(
    val lparams: KClass<*> = Any::class,
    val alias: String = "",
    val requireInit: Boolean = false,
    val requirePerformer: Boolean = false,
    val final: Boolean = false
)