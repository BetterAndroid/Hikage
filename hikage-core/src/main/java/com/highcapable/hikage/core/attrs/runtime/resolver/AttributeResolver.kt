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
 * This file is created by fankes on 2026/6/3.
 */
@file:Suppress("DiscouragedApi")

package com.highcapable.hikage.core.attrs.runtime.resolver

import android.content.Context
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.runtime.encoder.AttributeValueEncoder
import com.highcapable.hikage.core.attrs.runtime.entity.ResolvedAttribute
import com.highcapable.hikage.core.base.XmlParserException

/**
 * Resolves injected attributes into the common parser-independent domain model.
 */
internal object AttributeResolver {

    /**
     * Resolve the given [attrs].
     * @param context the context.
     * @param attrs the injected attributes.
     * @return resolved attributes ordered by resource id.
     */
    fun resolve(context: Context, attrs: List<AttributeItem>): List<ResolvedAttribute> {
        val seen = hashSetOf<String>()
        return attrs.map { attr ->
            if (!seen.add(attr.name)) throw XmlParserException(
                "Duplicate attribute name \"${attr.name}\" in the same view's attrs."
            )

            val pkg = AttributeValueEncoder.namespaceToPackage(context, attr.namespace)
            val id = context.resources.getIdentifier(attr.name, "attr", pkg)
            if (id == 0) throw XmlParserException(
                "Cannot resolve attribute \"$pkg:attr/${attr.name}\". " +
                    "Make sure the attribute exists and the namespace is correct."
            )
            ResolvedAttributeImpl(
                item = attr,
                namespaceUri = AttributeValueEncoder.namespaceToUri(attr.namespace),
                resourceId = id
            )
        }.sortedBy { it.resourceId }
    }

    /**
     * The default [ResolvedAttribute] implementation.
     */
    private data class ResolvedAttributeImpl(
        override val item: AttributeItem,
        override val namespaceUri: String,
        override val resourceId: Int
    ) : ResolvedAttribute
}