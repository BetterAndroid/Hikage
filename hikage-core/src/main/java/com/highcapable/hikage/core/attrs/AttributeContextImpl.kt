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
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.entity.AttributeName

/**
 * The [Hikage]'s [AttributeContext] collector.
 */
internal class AttributeContextImpl : Hikage.Attribute, AttributeContext {

    /** The collected attributes. */
    private val attrs = ArrayList<AttributeItem>(8)

    override fun namespace(name: String) = AttributeScopeImpl(name, this)

    override fun set(name: String, value: String) {
        val attrName = AttributeName.from(name)
        attrs += AttributeItem.of(attrName, AttributeItem.Value.Str(value))
    }

    override fun set(name: String, value: Int) {
        val attrName = AttributeName.from(name)
        attrs += AttributeItem.of(attrName, AttributeItem.Value.Raw(value))
    }

    override fun set(name: String, value: Boolean) {
        val attrName = AttributeName.from(name)
        attrs += AttributeItem.of(attrName, AttributeItem.Value.Bool(value))
    }

    /** Set an attribute with a string value. */
    fun set(namespace: String, name: String, value: String) {
        attrs += createAttributeItem(namespace, name, AttributeItem.Value.Str(value))
    }

    /** Set an attribute with a raw integer value. */
    fun set(namespace: String, name: String, value: Int) {
        attrs += createAttributeItem(namespace, name, AttributeItem.Value.Raw(value))
    }

    /** Set an attribute with a boolean value. */
    fun set(namespace: String, name: String, value: Boolean) {
        attrs += createAttributeItem(namespace, name, AttributeItem.Value.Bool(value))
    }

    /**
     * Build the collected attributes into a list.
     * @return [List]<[AttributeItem]>
     */
    fun build(): List<AttributeItem> = attrs

    private fun createAttributeItem(namespace: String, name: String, value: AttributeItem.Value): AttributeItem {
        if (name.indexOf(':') < 0) return AttributeItem(namespace, name, value)
        return AttributeItem.of(AttributeName.from(name, namespace), value)
    }
}