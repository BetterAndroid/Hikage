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
package com.highcapable.hikage.runtime.attribute.entity

import com.highcapable.hikage.runtime.attribute.exception.XmlParserException
import java.io.Serializable

/**
 * A single resolved attribute item.
 * @param namespace the attribute namespace, which is used to resolve the attribute name and value.
 * @param name the attribute name, which is used to resolve the attribute value.
 * @param value the attribute value.
 */
data class AttributeItem(
    val namespace: String,
    val name: String,
    val value: Value
) : Serializable {

    companion object {

        /**
         * Create a new [AttributeItem] from the given name, namespace and value.
         * @param name the attribute name, which can be in the format of "namespace:name" or just "name".
         * @param value the attribute value.
         * @param namespace the default namespace, which is used when the name does not contain a namespace.
         * It can be null if the name contains a namespace.
         * @return [AttributeItem]
         */
        @JvmStatic
        @JvmOverloads
        fun from(name: String, value: Value, namespace: String? = null): AttributeItem {
            val separator = name.indexOf(':')
            if (separator >= 0) {
                val resolvedNamespace = name.substring(0, separator)
                val resolvedName = name.substring(separator + 1)
                if (resolvedNamespace.isEmpty() || resolvedName.isEmpty()) throw XmlParserException(
                    "Invalid attribute name \"$name\"."
                )

                return AttributeItem(resolvedNamespace, resolvedName, value)
            }

            if (namespace.isNullOrEmpty()) throw XmlParserException(
                "Attribute \"$name\" must include a namespace or be declared inside a namespace."
            )
            return AttributeItem(namespace, name, value)
        }
    }

    /**
     * The value of an [AttributeItem].
     */
    sealed interface Value : Serializable {

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

        /**
         * A real number value.
         */
        data class Real(val value: Float) : Value
    }
}