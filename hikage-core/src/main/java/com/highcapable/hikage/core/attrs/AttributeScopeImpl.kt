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
 * This file is created by fankes on 2026/6/2.
 */
package com.highcapable.hikage.core.attrs

import com.highcapable.hikage.core.Hikage

/**
 * The [Hikage]'s [AttributeScope] collector.
 * @param namespace the namespace name.
 * @param context the attribute context.
 */
internal class AttributeScopeImpl(
    private val namespace: String,
    private val context: AttributeContextImpl
) : AttributeScope {

    override fun set(name: String, value: String) = context.set(namespace, name, value)
    override fun set(name: String, value: Int) = context.set(namespace, name, value)
    override fun set(name: String, value: Boolean) = context.set(namespace, name, value)
}