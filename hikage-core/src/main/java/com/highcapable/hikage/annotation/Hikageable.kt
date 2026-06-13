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
 * This file is created by fankes on 2025/3/23.
 */
package com.highcapable.hikage.annotation

import com.highcapable.hikage.core.Hikage

/**
 * Declare an implementation of the [Hikage.Performer] function that
 * will be used for infectious creation of sub-layouts.
 *
 * Usage:
 *
 * ```kotlin
 * @Hikageable
 * inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>.MyView(
 *     lparams: LayoutParams? = null,
 *     id: String? = null,
 *     noinline attrs: HikageAttribute = {},
 *     noinline init: HikageView<MyView> = {}
 * ) = View<MyView>(lparams, id, init)
 * ```
 * @see Hikage.Performer
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Hikageable