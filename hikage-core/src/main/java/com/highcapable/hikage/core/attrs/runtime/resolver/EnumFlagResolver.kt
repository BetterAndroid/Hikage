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
package com.highcapable.hikage.core.attrs.runtime.resolver

import com.highcapable.hikage.core.base.XmlParserException

/**
 * Resolver for framework and resource-bag attribute symbols.
 *
 * Because the compiled resource table resolves enum/flag symbols (e.g. `gravity="center"`)
 * into integers at compile time, we must do the same at runtime when synthesizing a binary
 * `AttributeSet`. There is no compiler available at runtime, so the symbol -> int mapping
 * must be resolved another way.
 *
 * Resolution is layered:
 * - **T1 (default, [BuiltInEnumFlagResolver])**: a curated static table of the common framework
 *   symbols whose values are de-facto ABI and never change.
 * - **T2 ([AttributeBagResolver])**: read the attribute's bag (`ResTable_map`) at runtime via
 *   reflection to cover library / custom enum/flag attributes.
 */
internal interface EnumFlagResolver {

    /**
     * Whether this resolver recognizes [attrName] as a symbol attribute.
     * @param attrName the attribute name (without namespace prefix).
     * @return [Boolean]
     */
    fun isEnumFlag(attrName: String): Boolean

    /**
     * Resolve the [value] of the symbol attribute [attrName] into an integer, or null if this resolver
     * recognizes the attribute but not this value.
     *
     * This is useful for framework attributes whose compiled XML accepts both fixed symbols and generic
     * values (for example dimensions).
     * @param attrName the attribute name (without namespace prefix).
     * @param value the symbol value.
     * @return [Int] or null.
     */
    fun resolveOrNull(attrName: String, value: String): Int? = try {
        resolve(attrName, value)
    } catch (_: XmlParserException) {
        null
    }

    /**
     * Resolve the [value] of the symbol attribute [attrName] into an integer.
     *
     * Should only be called when [isEnumFlag] returns true.
     * @param attrName the attribute name (without namespace prefix).
     * @param value the symbol value, e.g. `center` or `center_horizontal|center_vertical`.
     * @return [Int]
     * @throws XmlParserException if a symbol cannot be resolved.
     */
    fun resolve(attrName: String, value: String): Int
}