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
package com.highcapable.hikage.core.attrs.entity

import com.highcapable.hikage.core.Hikage

/**
 * A single resolved [Hikage] attribute item.
 * @param value the attribute value.
 */
internal data class AttributeItem(
    override val namespace: String,
    override val name: String,
    val value: Value
) : AttributeParams {

    companion object {

        /**
         * Create a new [AttributeItem] from an [AttributeName].
         * @param name the attribute name.
         * @param value the attribute value.
         * @return [AttributeItem]
         */
        fun of(name: AttributeName, value: Value) = AttributeItem(name.namespace, name.name, value)
    }

    /**
     * The value of an [AttributeItem].
     */
    sealed interface Value {

        /**
         * A string value to be parsed into a typed resource value.
         */
        data class Str(val value: String) : Value

        /**
         * A raw integer value.
         */
        data class Raw(val value: Int) : Value

        /**
         * A boolean value.
         */
        data class Bool(val value: Boolean) : Value
    }
}