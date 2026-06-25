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
 * This file is created by fankes on 2065/6/23.
 */
package com.highcapable.hikage.runtime.attribute.resolver

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies the built-in framework enum/flag fallback table used by runtime attribute encoding.
 */
class BuiltInEnumFlagResolverTest {

    @Test
    fun layoutSizeSymbolsAreResolved() {
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("layout_width", "match_parent"))
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("layout_height", "fill_parent"))
        assertEquals(-2, BuiltInEnumFlagResolver.resolve("layout_width", "wrap_content"))
    }

    @Test
    fun dimensionAndIntegerSymbolsAreResolved() {
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("dropDownWidth", "match_parent"))
        assertEquals(-2, BuiltInEnumFlagResolver.resolve("dropDownHeight", "wrap_content"))
        assertEquals(0, BuiltInEnumFlagResolver.resolve("actionBarSize", "wrap_content"))
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("numColumns", "auto_fit"))
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("marqueeRepeatLimit", "marquee_forever"))
        assertEquals(-1, BuiltInEnumFlagResolver.resolve("repeatCount", "infinite"))
    }

    @Test
    fun inputTypeSymbolsMatchFrameworkAttrs() {
        assertEquals(0x00100001, BuiltInEnumFlagResolver.resolve("inputType", "textEnableTextConversionSuggestions"))
        assertEquals(0x00200000, BuiltInEnumFlagResolver.resolve("inputType", "textEnableTextSuggestionSelected"))
    }

    @Test
    fun commonViewSymbolsAreResolved() {
        assertEquals(0x03000000, BuiltInEnumFlagResolver.resolve("scrollbarStyle", "outsideInset"))
        assertEquals(0x00003000, BuiltInEnumFlagResolver.resolve("requiresFadingEdge", "horizontal|vertical"))
        assertEquals(2, BuiltInEnumFlagResolver.resolve("overScrollMode", "never"))
        assertEquals(1, BuiltInEnumFlagResolver.resolve("verticalScrollbarPosition", "left"))
        assertEquals(2, BuiltInEnumFlagResolver.resolve("layerType", "hardware"))
    }
}