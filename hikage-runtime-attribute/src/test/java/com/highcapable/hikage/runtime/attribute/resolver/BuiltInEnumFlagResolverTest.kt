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
import org.junit.Assert.assertNull
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
    fun imageScaleTypeSymbolsMatchFrameworkAttrs() {
        assertEquals(0, BuiltInEnumFlagResolver.resolve("scaleType", "matrix"))
        assertEquals(1, BuiltInEnumFlagResolver.resolve("scaleType", "fitXY"))
        assertEquals(2, BuiltInEnumFlagResolver.resolve("scaleType", "fitStart"))
        assertEquals(3, BuiltInEnumFlagResolver.resolve("scaleType", "fitCenter"))
        assertEquals(4, BuiltInEnumFlagResolver.resolve("scaleType", "fitEnd"))
        assertEquals(5, BuiltInEnumFlagResolver.resolve("scaleType", "center"))
        assertEquals(6, BuiltInEnumFlagResolver.resolve("scaleType", "centerCrop"))
        assertEquals(7, BuiltInEnumFlagResolver.resolve("scaleType", "centerInside"))
    }

    @Test
    fun activeFrameworkViewSymbolsMatchFrameworkAttrs() {
        val tintModes = mapOf(
            "src_over" to 3,
            "src_in" to 5,
            "src_atop" to 9,
            "multiply" to 14,
            "screen" to 15,
            "add" to 16
        )
        val contentImportance = mapOf(
            "auto" to 0,
            "yes" to 0x1,
            "no" to 0x2,
            "yesExcludeDescendants" to 0x4,
            "noExcludeDescendants" to 0x8
        )
        val expected = buildMap {
            put("accessibilityDataSensitive", mapOf("auto" to 0, "yes" to 1, "no" to 2))
            put("accessibilityLiveRegion", mapOf("none" to 0, "polite" to 1, "assertive" to 2))
            put("alignmentMode", mapOf("alignBounds" to 0, "alignMargins" to 1))
            put("autoLink", mapOf("none" to 0x00, "web" to 0x01, "email" to 0x02, "phone" to 0x04, "map" to 0x08, "all" to 0x0f))
            put("autoSizeTextType", mapOf("none" to 0, "uniform" to 1))
            put("breakStrategy", mapOf("simple" to 0, "high_quality" to 1, "balanced" to 2))
            put("bufferType", mapOf("normal" to 0, "spannable" to 1, "editable" to 2))
            put("buttonGravity", mapOf("top" to 0x30, "bottom" to 0x50))
            put("capitalize", mapOf("none" to 0, "sentences" to 1, "words" to 2, "characters" to 3))
            put("choiceMode", mapOf("none" to 0, "singleChoice" to 1, "multipleChoice" to 2, "multipleChoiceModal" to 3))
            put("contentSensitivity", mapOf("auto" to 0, "sensitive" to 0x1, "notSensitive" to 0x2))
            put("datePickerMode", mapOf("spinner" to 1, "calendar" to 2))
            put("descendantFocusability", mapOf("beforeDescendants" to 0, "afterDescendants" to 1, "blocksDescendants" to 2))
            put("drawingCacheQuality", mapOf("auto" to 0, "low" to 1, "high" to 2))
            put("focusable", mapOf("auto" to 0x00000010))
            put("gestureStrokeType", mapOf("single" to 0, "multiple" to 1))
            put("hyphenationFrequency", mapOf("none" to 0, "normal" to 1, "full" to 2, "normalFast" to 3, "fullFast" to 4))
            put("importantForAutofill", contentImportance)
            put("importantForContentCapture", contentImportance)
            put("indeterminateBehavior", mapOf("repeat" to 1, "cycle" to 2))
            put("justificationMode", mapOf("none" to 0, "inter_word" to 1, "inter_character" to 2))
            put("layoutMode", mapOf("clipBounds" to 0, "opticalBounds" to 1))
            put("lineBreakStyle", mapOf("none" to 0, "loose" to 1, "normal" to 2, "strict" to 3))
            put("lineBreakWordStyle", mapOf("none" to 0, "phrase" to 1))
            put("mediaRouteTypes", mapOf("liveAudio" to 0x1, "user" to 0x800000))
            put("numeric", mapOf("integer" to 0x01, "signed" to 0x03, "decimal" to 0x05))
            put("outlineProvider", mapOf("background" to 0, "none" to 1, "bounds" to 2, "paddedBounds" to 3))
            put("persistentDrawingCache", mapOf("none" to 0x0, "animation" to 0x1, "scrolling" to 0x2, "all" to 0x3))
            put(
                "pointerIcon",
                mapOf(
                    "none" to 0,
                    "arrow" to 1000,
                    "context_menu" to 1001,
                    "hand" to 1002,
                    "help" to 1003,
                    "wait" to 1004,
                    "cell" to 1006,
                    "crosshair" to 1007,
                    "text" to 1008,
                    "vertical_text" to 1009,
                    "alias" to 1010,
                    "copy" to 1011,
                    "no_drop" to 1012,
                    "all_scroll" to 1013,
                    "horizontal_double_arrow" to 1014,
                    "vertical_double_arrow" to 1015,
                    "top_right_diagonal_double_arrow" to 1016,
                    "top_left_diagonal_double_arrow" to 1017,
                    "zoom_in" to 1018,
                    "zoom_out" to 1019,
                    "grab" to 1020,
                    "grabbing" to 1021,
                    "handwriting" to 1022
                )
            )
            put(
                "scrollIndicators",
                mapOf("none" to 0x00, "top" to 0x01, "bottom" to 0x02, "left" to 0x04, "right" to 0x08, "start" to 0x10, "end" to 0x20)
            )
            put("spinnerMode", mapOf("dialog" to 0, "dropdown" to 1))
            put("stretchMode", mapOf("none" to 0, "spacingWidth" to 1, "columnWidth" to 2, "spacingWidthUniform" to 3))
            put("timePickerMode", mapOf("spinner" to 1, "clock" to 2))
            put("transcriptMode", mapOf("disabled" to 0, "normal" to 1, "alwaysScroll" to 2))
            listOf(
                "backgroundTintMode",
                "buttonTintMode",
                "checkMarkTintMode",
                "dialTintMode",
                "drawableTintMode",
                "foregroundTintMode",
                "hand_hourTintMode",
                "hand_minuteTintMode",
                "hand_secondTintMode",
                "indeterminateTintMode",
                "progressBackgroundTintMode",
                "progressTintMode",
                "secondaryProgressTintMode",
                "thumbTintMode",
                "tickMarkTintMode",
                "tintMode",
                "trackTintMode"
            ).forEach { put(it, tintModes) }
        }

        assertEquals(51, expected.size)
        expected.forEach { (attrName, symbols) ->
            symbols.forEach { (symbol, value) ->
                assertEquals("$attrName=$symbol", value, BuiltInEnumFlagResolver.resolve(attrName, symbol))
            }
        }
        assertEquals(0x03, BuiltInEnumFlagResolver.resolve("autoLink", "web|email"))
        assertEquals(0x03, BuiltInEnumFlagResolver.resolve("persistentDrawingCache", "animation|scrolling"))
        assertEquals(0x18, BuiltInEnumFlagResolver.resolve("scrollIndicators", "right|start"))
        assertNull(BuiltInEnumFlagResolver.resolveOrNull("focusable", "true"))
        assertNull(BuiltInEnumFlagResolver.resolveOrNull("focusable", "false"))
    }

    @Test
    fun commonViewSymbolsAreResolved() {
        assertEquals(0x03000000, BuiltInEnumFlagResolver.resolve("scrollbarStyle", "outsideInset"))
        assertEquals(0x00003000, BuiltInEnumFlagResolver.resolve("requiresFadingEdge", "horizontal|vertical"))
        assertEquals(2, BuiltInEnumFlagResolver.resolve("overScrollMode", "never"))
        assertEquals(1, BuiltInEnumFlagResolver.resolve("verticalScrollbarPosition", "left"))
        assertEquals(2, BuiltInEnumFlagResolver.resolve("layerType", "hardware"))
        assertEquals(0, BuiltInEnumFlagResolver.resolve("gravity", "no_gravity"))
        assertEquals(0, BuiltInEnumFlagResolver.resolve("layout_gravity", "none"))
    }
}