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
package com.highcapable.hikage.core.attrs.entity

import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.XmlParserException

/**
 * A resolved [Hikage] attribute name.
 */
internal data class AttributeName(
    override val namespace: String,
    override val name: String
) : AttributeParams {

    companion object {

        /**
         * Create a new [AttributeName] from the given name and namespace.
         * @param name the attribute name, which can be in the format of "namespace:name" or just "name".
         * @param namespace the default namespace, which is used when the name does not contain a namespace.
         * It can be null if the name contains a namespace.
         * @return [AttributeName]
         */
        fun from(name: String, namespace: String? = null): AttributeName {
            val separator = name.indexOf(':')
            if (separator >= 0) {
                val resolvedNamespace = name.substring(0, separator)
                val resolvedName = name.substring(separator + 1)
                if (resolvedNamespace.isEmpty() || resolvedName.isEmpty()) throw XmlParserException(
                    "Invalid attribute name \"$name\"."
                )

                return AttributeName(resolvedNamespace, resolvedName)
            }

            if (namespace.isNullOrEmpty()) throw XmlParserException(
                "Attribute \"$name\" must include a namespace or be declared inside a namespace."
            )
            return AttributeName(namespace, name)
        }
    }
}