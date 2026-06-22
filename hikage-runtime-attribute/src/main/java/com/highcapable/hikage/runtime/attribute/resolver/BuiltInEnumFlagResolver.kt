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
package com.highcapable.hikage.runtime.attribute.resolver

import android.view.ViewGroup
import com.highcapable.hikage.runtime.attribute.exception.XmlParserException

/**
 * The built-in (T1) [EnumFlagResolver].
 *
 * Covers the common framework symbols. The integer values below are part of the Android resource ABI
 * (changing them would break every compiled APK), so hard-coding them is safe.
 */
internal object BuiltInEnumFlagResolver : EnumFlagResolver {

    /**
     * A symbol attribute definition.
     * @param isFlag whether multiple symbols may be combined with `|`.
     * @param symbols the symbol -> int mapping.
     * @param allowsGenericValue whether unknown values may fall through to generic encoding.
     */
    private class Def(
        val isFlag: Boolean,
        val symbols: Map<String, Int>,
        val allowsGenericValue: Boolean = false
    )

    /** flag, [android.view.Gravity] values. */
    private val gravity = Def(
        isFlag = true,
        symbols = mapOf(
            "top" to 0x30,
            "bottom" to 0x50,
            "left" to 0x03,
            "right" to 0x05,
            "center_vertical" to 0x10,
            "fill_vertical" to 0x70,
            "center_horizontal" to 0x01,
            "fill_horizontal" to 0x07,
            "center" to 0x11,
            "fill" to 0x77,
            "clip_vertical" to 0x80,
            "clip_horizontal" to 0x08,
            "start" to 0x00800003,
            "end" to 0x00800005,
            "no_gravity" to 0x00,
            "none" to 0x00
        )
    )

    /** enum, [ViewGroup.LayoutParams] values. */
    private val layoutSize = Def(
        isFlag = false,
        allowsGenericValue = true,
        symbols = mapOf(
            "fill_parent" to -1,
            "match_parent" to -1,
            "wrap_content" to -2
        )
    )

    /**
     * The table of known framework symbol attributes. The keys are the attribute names without namespace
     * prefixes, e.g. "gravity" not "android:gravity". The values are the definitions of how to resolve
     * the symbols for that attribute.
     */
    private val table = buildMap {
        put("layout_width", layoutSize)
        put("layout_height", layoutSize)
        put("dropDownWidth", layoutSize)
        put("dropDownHeight", layoutSize)

        // enum, action bar size accepts wrap_content besides dimensions.
        put("actionBarSize", Def(false, mapOf("wrap_content" to 0), allowsGenericValue = true))

        // enum, GridView.AUTO_FIT.
        put("numColumns", Def(false, mapOf("auto_fit" to -1)))

        // enum, TextView marquee repeat forever.
        put("marqueeRepeatLimit", Def(false, mapOf("marquee_forever" to -1)))

        // enum, Animation.INFINITE.
        put("repeatCount", Def(false, mapOf("infinite" to -1)))

        put("gravity", gravity)
        put("layout_gravity", gravity)
        put("foregroundGravity", gravity)

        // enum, View scrollbar style flags.
        put(
            "scrollbarStyle",
            Def(
                false,
                mapOf(
                    "insideOverlay" to 0x00000000,
                    "insideInset" to 0x01000000,
                    "outsideOverlay" to 0x02000000,
                    "outsideInset" to 0x03000000
                )
            )
        )

        // flag, View fading edge directions.
        val fadingEdge = Def(true, mapOf("none" to 0x00000000, "horizontal" to 0x00001000, "vertical" to 0x00002000))
        put("fadingEdge", fadingEdge)
        put("requiresFadingEdge", fadingEdge)

        // enum, View overscroll mode.
        put("overScrollMode", Def(false, mapOf("always" to 0, "ifContentScrolls" to 1, "never" to 2)))

        // enum, View vertical scrollbar position.
        put("verticalScrollbarPosition", Def(false, mapOf("defaultPosition" to 0, "left" to 1, "right" to 2)))

        // enum, View layer type.
        put("layerType", Def(false, mapOf("none" to 0, "software" to 1, "hardware" to 2)))

        // enum, LinearLayout.HORIZONTAL / VERTICAL
        put("orientation", Def(false, mapOf("horizontal" to 0, "vertical" to 1)))

        // flag, LinearLayout.SHOW_DIVIDER_* values.
        put("showDividers", Def(true, mapOf("none" to 0, "beginning" to 1, "middle" to 2, "end" to 4)))

        // enum, View.VISIBLE / INVISIBLE / GONE
        put("visibility", Def(false, mapOf("visible" to 0, "invisible" to 1, "gone" to 2)))

        // flag, Typeface.NORMAL / BOLD / ITALIC
        put("textStyle", Def(true, mapOf("normal" to 0, "bold" to 1, "italic" to 2)))

        // enum, TextView typeface family
        put("typeface", Def(false, mapOf("normal" to 0, "sans" to 1, "serif" to 2, "monospace" to 3)))

        // enum, TextUtils.TruncateAt
        put(
            "ellipsize",
            Def(false, mapOf("none" to 0, "start" to 1, "middle" to 2, "end" to 3, "marquee" to 4))
        )

        // flag, View scrollbars
        put(
            "scrollbars",
            Def(true, mapOf("none" to 0x00000000, "horizontal" to 0x00000100, "vertical" to 0x00000200))
        )

        // enum, View.textAlignment
        put(
            "textAlignment",
            Def(
                false,
                mapOf(
                    "inherit" to 0, "gravity" to 1, "textStart" to 2, "textEnd" to 3,
                    "center" to 4, "viewStart" to 5, "viewEnd" to 6
                )
            )
        )

        // enum, View.textDirection
        put(
            "textDirection",
            Def(
                false,
                mapOf(
                    "inherit" to 0, "firstStrong" to 1, "anyRtl" to 2, "ltr" to 3, "rtl" to 4,
                    "locale" to 5, "firstStrongLtr" to 6, "firstStrongRtl" to 7
                )
            )
        )

        // enum, View.layoutDirection
        put(
            "layoutDirection",
            Def(false, mapOf("ltr" to 0, "rtl" to 1, "inherit" to 2, "locale" to 3))
        )

        // enum, View.importantForAccessibility
        put(
            "importantForAccessibility",
            Def(false, mapOf("auto" to 0, "yes" to 1, "no" to 2, "noHideDescendants" to 4))
        )

        // flag, android.text.InputType (common subset)
        put(
            "inputType",
            Def(
                true,
                mapOf(
                    "none" to 0x00000000,
                    "text" to 0x00000001,
                    "textCapCharacters" to 0x00001001,
                    "textCapWords" to 0x00002001,
                    "textCapSentences" to 0x00004001,
                    "textAutoCorrect" to 0x00008001,
                    "textAutoComplete" to 0x00010001,
                    "textMultiLine" to 0x00020001,
                    "textImeMultiLine" to 0x00040001,
                    "textNoSuggestions" to 0x00080001,
                    "textEnableTextConversionSuggestions" to 0x00100001,
                    "textEnableTextSuggestionSelected" to 0x00200000,
                    "textUri" to 0x00000011,
                    "textEmailAddress" to 0x00000021,
                    "textEmailSubject" to 0x00000031,
                    "textShortMessage" to 0x00000041,
                    "textLongMessage" to 0x00000051,
                    "textPersonName" to 0x00000061,
                    "textPostalAddress" to 0x00000071,
                    "textPassword" to 0x00000081,
                    "textVisiblePassword" to 0x00000091,
                    "textWebEditText" to 0x000000a1,
                    "textFilter" to 0x000000b1,
                    "textPhonetic" to 0x000000c1,
                    "textWebEmailAddress" to 0x000000d1,
                    "textWebPassword" to 0x000000e1,
                    "number" to 0x00000002,
                    "numberSigned" to 0x00001002,
                    "numberDecimal" to 0x00002002,
                    "numberPassword" to 0x00000012,
                    "phone" to 0x00000003,
                    "datetime" to 0x00000004,
                    "date" to 0x00000014,
                    "time" to 0x00000024
                )
            )
        )

        // flag, android.view.inputmethod.EditorInfo imeOptions (common subset)
        put(
            "imeOptions",
            Def(
                true,
                mapOf(
                    "normal" to 0x00000000,
                    "actionUnspecified" to 0x00000000,
                    "actionNone" to 0x00000001,
                    "actionGo" to 0x00000002,
                    "actionSearch" to 0x00000003,
                    "actionSend" to 0x00000004,
                    "actionNext" to 0x00000005,
                    "actionDone" to 0x00000006,
                    "actionPrevious" to 0x00000007,
                    "flagNoPersonalizedLearning" to 0x01000000,
                    "flagNoFullscreen" to 0x02000000,
                    "flagNavigatePrevious" to 0x04000000,
                    "flagNavigateNext" to 0x08000000,
                    "flagNoExtractUi" to 0x10000000,
                    "flagNoAccessoryAction" to 0x20000000,
                    "flagNoEnterAction" to 0x40000000,
                    "flagForceAscii" to 0x80000000.toInt()
                )
            )
        )
    }

    override fun isEnumFlag(attrName: String) = table.containsKey(attrName)

    override fun resolveOrNull(attrName: String, value: String): Int? {
        val def = table[attrName] ?: return null
        val parts = value.split('|').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null
        if (!def.isFlag && parts.size > 1) return null
        if (parts.any { it !in def.symbols }) return if (def.allowsGenericValue) null else resolve(attrName, value)

        return resolve(attrName, value)
    }

    override fun resolve(attrName: String, value: String): Int {
        val def = table[attrName] ?: throw XmlParserException(
            "Attribute \"$attrName\" is not a known framework symbol attribute."
        )
        val parts = value.split('|').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) throw XmlParserException("Empty symbol value for attribute \"$attrName\".")
        if (!def.isFlag && parts.size > 1) throw XmlParserException(
            "Attribute \"$attrName\" cannot combine multiple symbols with \"|\"."
        )

        var result = 0
        parts.forEach { symbol ->
            val resolved = def.symbols[symbol] ?: throw XmlParserException(
                "Unknown framework symbol \"$symbol\" for attribute \"$attrName\". " +
                    "Supported symbols: ${def.symbols.keys.joinToString()}. " +
                    "Alternatively pass a raw Int value, e.g. set(\"$attrName\", <int>)."
            )
            result = result or resolved
        }

        return result
    }
}