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
 * This file is created by fankes on 2026/6/12.
 */
package com.highcapable.hikage.core.lint.detector.entity

/**
 * The Hikage view symbol item.
 * @param viewClass the view class name.
 * @param name the generated component function name.
 * @param packageName the generated component function package name.
 */
internal data class ViewSymbol(
    val viewClass: String?,
    val name: String?,
    val packageName: String?
)
