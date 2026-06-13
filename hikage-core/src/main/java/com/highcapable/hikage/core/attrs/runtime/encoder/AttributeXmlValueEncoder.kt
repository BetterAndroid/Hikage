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
 * This file is created by fankes on 2026/6/3.
 */
package com.highcapable.hikage.core.attrs.runtime.encoder

import android.content.Context
import android.util.TypedValue
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.runtime.resolver.BuiltInEnumFlagResolver
import java.util.Locale

/**
 * Encodes injected attribute values into their XML text representation for layoutlib.
 */
internal object AttributeXmlValueEncoder {

    /**
     * Encode the value of the given [attr].
     * @param context the context.
     * @param attr the attribute.
     * @return the XML text value.
     */
    fun encode(context: Context, attr: AttributeItem) = when (val value = attr.value) {
        is AttributeItem.Value.Str -> value.value
        is AttributeItem.Value.Bool -> value.value.toString()
        is AttributeItem.Value.Raw -> encodeRawInt(context, attr, value.value)
    }

    private fun encodeRawInt(context: Context, attr: AttributeItem, value: Int): String {
        val encoded = AttributeValueEncoder.encode(context, attr, BuiltInEnumFlagResolver) { -1 }
        return when (encoded.dataType) {
            TypedValue.TYPE_REFERENCE -> resourceReferenceOf(context, value)
            TypedValue.TYPE_INT_COLOR_ARGB8,
            TypedValue.TYPE_INT_COLOR_RGB8,
            TypedValue.TYPE_INT_COLOR_ARGB4,
            TypedValue.TYPE_INT_COLOR_RGB4 -> String.format(Locale.US, "#%08X", value)
            TypedValue.TYPE_DIMENSION -> "${value}px"
            TypedValue.TYPE_INT_BOOLEAN -> (value != 0).toString()
            else -> value.toString()
        }
    }

    private fun resourceReferenceOf(context: Context, resId: Int) = runCatching {
        val resources = context.resources
        val pkg = resources.getResourcePackageName(resId)
        val type = resources.getResourceTypeName(resId)
        val name = resources.getResourceEntryName(resId)

        when (pkg) {
            "android" -> "@android:$type/$name"
            context.packageName -> "@$type/$name"
            else -> "@$pkg:$type/$name"
        }
    }.getOrElse { resId.toString() }
}