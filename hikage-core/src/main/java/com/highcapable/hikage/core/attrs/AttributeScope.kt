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
 * This file is created by fankes on 2026/6/2.
 */
package com.highcapable.hikage.core.attrs

import com.highcapable.hikage.core.Hikage

/**
 * The attribute setting scope of [Hikage].
 */
interface AttributeScope {

    /**
     * Set an attribute with a string value (mirrors the original XML attribute value).
     *
     * The value will be parsed into a typed resource value, supporting:
     * references (`@pkg:type/name`), attribute references (`?attr/name`), colors (`#RRGGBB`),
     * booleans, integers, floats, dimensions (`16dp`/`16sp`), fractions (`50%`),
     * enum/flag symbols (e.g. `center`, `vertical|fill`), and plain strings.
     * @param name the attribute name (without namespace prefix).
     * @param value the attribute value.
     */
    fun set(name: String, value: String)

    /**
     * Set an attribute with a raw integer value.
     *
     * Use this for enum/flag attributes whose symbols cannot be resolved
     * (e.g. `set("gravity", Gravity.CENTER)`), or for any value you have already resolved.
     * @param name the attribute name (without namespace prefix).
     * @param value the raw integer value.
     */
    fun set(name: String, value: Int)

    /**
     * Set an attribute with a boolean value.
     * @param name the attribute name (without namespace prefix).
     * @param value the boolean value.
     */
    fun set(name: String, value: Boolean)
}