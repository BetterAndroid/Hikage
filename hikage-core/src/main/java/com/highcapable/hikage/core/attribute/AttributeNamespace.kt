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
 * This file is created by fankes on 2026/6/3.
 */
@file:Suppress("unused")
@file:JvmName("HikageAttributeNamespaceUtils")

package com.highcapable.hikage.core.attribute

import com.highcapable.hikage.core.Hikage

/**
 * Declare attributes under the `android` namespace.
 * @receiver the attribute.
 * @see namespace
 * @return [AttributeScope]
 */
val Hikage.Attribute.android get() = namespace("android")

/**
 * Declare attributes under the `app` namespace.
 * @receiver the attribute.
 * @see namespace
 * @return [AttributeScope]
 */
val Hikage.Attribute.app get() = namespace("app")

/**
 * Declare attributes under the `android` namespace.
 * @receiver the attribute.
 * @see namespace
 * @param block the namespace body.
 */
inline fun Hikage.Attribute.android(block: AttributeScope.() -> Unit) = namespace("android", block)

/**
 * Declare attributes under the `app` namespace.
 * @receiver the attribute.
 * @see namespace
 * @param block the namespace body.
 */
inline fun Hikage.Attribute.app(block: AttributeScope.() -> Unit) = namespace("app", block)