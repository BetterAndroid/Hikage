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
@file:Suppress("unused", "NOTHING_TO_INLINE")
@file:JvmName("HikageAttributeUtils")

package com.highcapable.hikage.core.attribute

import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.attribute.exception.AttributeResolvingException
import com.highcapable.hikage.runtime.attribute.AttributeSetResolver
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem

/**
 * The [Hikage.Attribute] type.
 */
typealias HikageAttribute = Hikage.Attribute.() -> Unit

/**
 * Create a [HikageAttribute] block.
 * @param block the attribute declaration body.
 * @return [HikageAttribute]
 */
fun HikageAttribute(block: HikageAttribute): HikageAttribute = block

/**
 * Declare attributes under a namespace.
 *
 * The [name] can be:
 * - `"android"` for the framework namespace.
 * - An application or library package name for custom attributes.
 * @see android
 * @see app
 * @receiver the attribute.
 * @param name the namespace name.
 * @return [AttributeScope]
 */
fun Hikage.Attribute.namespace(name: String) = resolveContext().namespace(name)

/**
 * Declare attributes under a namespace.
 *
 * The [name] can be:
 * - `"android"` for the framework namespace.
 * - An application or library package name for custom attributes.
 * @see android
 * @see app
 * @receiver the attribute.
 * @param name the namespace name.
 * @param block the namespace body.
 */
inline fun Hikage.Attribute.namespace(name: String, block: AttributeScope.() -> Unit) = namespace(name).block()

/**
 * Set an attribute with a string value (mirrors the original XML attribute value).
 *
 * The value will be parsed into a typed resource value, supporting:
 * references (`@pkg:type/name`), attribute references (`?attr/name`), colors (`#RRGGBB`),
 * booleans, integers, floats, dimensions (`16dp`/`16sp`), fractions (`50%`),
 * enum/flag symbols (e.g. `center`, `vertical|fill`), and plain strings.
 * @receiver the attribute.
 * @param name the qualified attribute name, e.g. `android:background`.
 * @param value the attribute value.
 */
fun Hikage.Attribute.set(name: String, value: String) = resolveContext().set(name, value)

/**
 * Set an attribute with a raw integer value.
 *
 * Use this for enum/flag attributes whose symbols cannot be resolved
 * (e.g. `set("android:gravity", Gravity.CENTER)`), or for any value you have already resolved.
 * @receiver the attribute.
 * @param name the qualified attribute name, e.g. `android:background`.
 * @param value the raw integer value.
 */
fun Hikage.Attribute.set(name: String, value: Int) = resolveContext().set(name, value)

/**
 * Set an attribute with a boolean value.
 * @receiver the attribute.
 * @param name the qualified attribute name, e.g. `android:enabled`.
 * @param value the boolean value.
 */
fun Hikage.Attribute.set(name: String, value: Boolean) = resolveContext().set(name, value)

/**
 * Set an attribute with a real number value.
 * @receiver the attribute.
 * @param name the qualified attribute name, e.g. `android:layout_weight`.
 * @param value the real number value.
 */
fun Hikage.Attribute.set(name: String, value: Float) = resolveContext().set(name, value)

/**
 * Convert a [Collection]<[AttributeItem]> to [HikageAttribute].
 * @see HikageAttribute.build
 * @receiver the resolved attribute item.
 * @return [HikageAttribute]
 */
fun Collection<AttributeItem>.toHikageAttribute() = HikageAttribute {
    forEach { item ->
        val scope = namespace(item.namespace)
        when (val value = item.value) {
            is AttributeItem.Value.Str -> scope.set(item.name, value.value)
            is AttributeItem.Value.Raw -> scope.set(item.name, value.value)
            is AttributeItem.Value.Bool -> scope.set(item.name, value.value)
            is AttributeItem.Value.Real -> scope.set(item.name, value.value)
        }
    }
}

/**
 * Build the attributes declared by this [HikageAttribute].
 * @see HikageAttribute.isNotEmpty
 * @receiver the attribute body.
 * @return [List]<[AttributeItem]>
 * @throws AttributeResolvingException if the `hikage-runtime-attribute` dependency is not available.
 */
fun HikageAttribute.build() = runCatching { buildUnchecked() }.getOrElse {
    if (it is ClassNotFoundException || it is NoClassDefFoundError) throw AttributeResolvingException(
        "Failed to build attributes. Please make sure you have added the `hikage-runtime-attribute` dependency for attribute resolving."
    ) else throw it
}

/**
 * Check if the attributes declared by this [HikageAttribute] are not empty.
 *
 * If it fails, it proves that there may not have `hikage-runtime-attribute` dependency, so always returns `true`.
 * @see HikageAttribute.build
 * @receiver the attribute body.
 * @return [Boolean]
 */
fun HikageAttribute.isNotEmpty() = runCatching { buildUnchecked().isNotEmpty() }.getOrNull() ?: true

/**
 * Check if the attributes declared by this [HikageAttribute] are empty.
 *
 * If it fails, it proves that there may not have `hikage-runtime-attribute` dependency, so always returns `false`.
 * @see HikageAttribute.build
 * @receiver the attribute body.
 * @return [Boolean]
 */
inline fun HikageAttribute.isEmpty() = !isNotEmpty()

/**
 * Build the attributes declared by this [HikageAttribute] without checking.
 * @receiver the attribute body.
 * @return [List]<[AttributeItem]>
 */
private fun HikageAttribute.buildUnchecked() = AttributeContextImpl().apply(this).build()

/**
 * Check if the `hikage-runtime-attribute` dependency is available.
 * @return [Boolean]
 */
internal fun hikageAttributeIsAvailable() = runCatching {
    // Try to access the `AttributeSetResolver` to check if the attribute resolving feature is available.
    AttributeSetResolver.let {}; true
}.getOrNull() ?: false

/**
 * Resolve the [AttributeContext] from the [Hikage.Attribute].
 * @receiver the attribute.
 * @return [AttributeContext]
 */
private fun Hikage.Attribute.resolveContext() = this as? AttributeContext
    ?: throw AttributeResolvingException("This Attribute is not created by Hikage.")