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
 * This file is created by fankes on 2026/6/2.
 */
@file:Suppress("DiscouragedApi")

package com.highcapable.hikage.core.attrs.runtime.builder

import android.content.Context
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.runtime.encoder.AttributeValueEncoder
import com.highcapable.hikage.core.attrs.runtime.entity.EncodedAttributeValue
import com.highcapable.hikage.core.attrs.runtime.entity.ResolvedAttribute
import com.highcapable.hikage.core.attrs.runtime.resolver.AttributeResolver
import com.highcapable.hikage.core.attrs.runtime.resolver.BuiltInEnumFlagResolver
import com.highcapable.hikage.core.attrs.runtime.resolver.EnumFlagResolver
import com.highcapable.hikage.core.base.XmlParserException
import com.highcapable.hikage.core.utils.LEWriter

/**
 * Synthesizes a compiled binary XML (`ResXMLTree`) document in memory from a list of [AttributeItem].
 *
 * The output byte array can be loaded by `android.content.res.XmlBlock(byte[])` to obtain a real
 * `XmlBlock.Parser` whose attributes are readable by `obtainStyledAttributes` (which casts the
 * `AttributeSet` to `XmlBlock.Parser` and reads typed attributes by resource id from native).
 *
 * Document layout (little-endian ResChunk format):
 * ```
 * RES_XML_TYPE
 *  ├─ RES_STRING_POOL_TYPE          (attribute names first, then uris/prefixes/element/values)
 *  ├─ RES_XML_RESOURCE_MAP_TYPE     (uint32[] resId, parallel to the leading attribute-name strings)
 *  ├─ RES_XML_START_NAMESPACE_TYPE  (one per distinct namespace)
 *  ├─ RES_XML_START_ELEMENT_TYPE    (attrExt + attributes)
 *  ├─ RES_XML_END_ELEMENT_TYPE
 *  └─ RES_XML_END_NAMESPACE_TYPE    (reverse order)
 * ```
 */
internal object BinaryXmlBuilder {

    // ResChunk types.
    private const val TYPE_STRING_POOL = 0x0001
    private const val TYPE_XML = 0x0003
    private const val TYPE_XML_START_NAMESPACE = 0x0100
    private const val TYPE_XML_END_NAMESPACE = 0x0101
    private const val TYPE_XML_START_ELEMENT = 0x0102
    private const val TYPE_XML_END_ELEMENT = 0x0103
    private const val TYPE_XML_RESOURCE_MAP = 0x0180

    /** The synthetic element tag name (irrelevant to attribute resolution). */
    private const val ELEMENT_NAME = "View"

    private const val NO_ENTRY = -1

    /**
     * Build the binary XML document.
     * @param context the context.
     * @param attrs the attributes.
     * @param resolver the framework symbol resolver.
     * @return [ByteArray]
     * @throws XmlParserException if an attribute name is duplicated or a value cannot be encoded.
     */
    fun build(
        context: Context,
        attrs: List<AttributeItem>,
        resolver: EnumFlagResolver = BuiltInEnumFlagResolver
    ) = buildResolved(context, AttributeResolver.resolve(context, attrs), resolver)

    /**
     * Build the binary XML document from parser-independent [attrs].
     * @param context the context.
     * @param attrs the resolved attributes.
     * @param resolver the framework symbol resolver.
     * @return [ByteArray]
     */
    private fun buildResolved(
        context: Context,
        attrs: List<ResolvedAttribute>,
        resolver: EnumFlagResolver
    ): ByteArray {
        val pool = StringPool()

        // 1) Attribute-name strings MUST occupy the first N indices (parallel to the resource map).
        attrs.forEach { pool.add(it.item.name) }

        // 2) Resolve each attribute's resource id (for the resource map).
        val resIds = attrs.map { it.resourceId }

        // 3) Element name + namespaces.
        val elementNameIndex = pool.add(ELEMENT_NAME)

        val nsUriByAttr = attrs.map { it.namespaceUri }
        val distinctUris = nsUriByAttr.distinct()
        val namespaces = distinctUris.map { uri ->
            val prefix = prefixForUri(uri)
            Namespace(prefixIndex = pool.add(prefix), uriIndex = pool.add(uri), uri = uri)
        }
        val uriIndexByUri = namespaces.associate { it.uri to it.uriIndex }

        // 4) Encode each attribute value (interns value strings into the pool after the names).
        val encoded = attrs.map { attr ->
            AttributeValueEncoder.encode(context, attr.item, resolver, pool::add)
        }

        // 5) Assemble chunks.
        val stringPoolChunk = pool.build()
        val resourceMapChunk = resIds.takeIf { it.isNotEmpty() }?.let { buildResourceMapChunk(it) }
        val startNamespaceChunks = namespaces.map { buildNamespaceChunk(TYPE_XML_START_NAMESPACE, it) }
        val startElementChunk = buildStartElementChunk(
            elementNameIndex = elementNameIndex,
            attrs = attrs.map { it.item },
            encoded = encoded,
            nsUriByAttr = nsUriByAttr,
            uriIndexByUri = uriIndexByUri
        )
        val endElementChunk = buildEndElementChunk(elementNameIndex)
        val endNamespaceChunks = namespaces.reversed().map { buildNamespaceChunk(TYPE_XML_END_NAMESPACE, it) }

        val childrenBytes = LEWriter {
            bytes(stringPoolChunk)
            resourceMapChunk?.let { bytes(it) }
            startNamespaceChunks.forEach { bytes(it) }
            bytes(startElementChunk)
            bytes(endElementChunk)
            endNamespaceChunks.forEach { bytes(it) }
        }

        // 6) Wrap in the XML chunk.
        return LEWriter {
            u16(TYPE_XML)
            u16(8) // headerSize
            u32(8 + childrenBytes.size) // total size
            bytes(childrenBytes)
        }
    }

    private fun buildResourceMapChunk(resIds: List<Int>) = LEWriter {
        u16(TYPE_XML_RESOURCE_MAP)
        u16(8) // headerSize
        u32(8 + resIds.size * 4) // size
        resIds.forEach { u32(it) }
    }

    private fun buildNamespaceChunk(type: Int, ns: Namespace) = LEWriter {
        u16(type)
        u16(16) // headerSize
        u32(24) // size
        u32(1) // lineNumber
        u32(NO_ENTRY) // comment
        u32(ns.prefixIndex)
        u32(ns.uriIndex)
    }

    private fun buildStartElementChunk(
        elementNameIndex: Int,
        attrs: List<AttributeItem>,
        encoded: List<EncodedAttributeValue>,
        nsUriByAttr: List<String>,
        uriIndexByUri: Map<String, Int>
    ): ByteArray {
        val count = attrs.size
        val size = 16 + 20 + count * 20

        return LEWriter {
            u16(TYPE_XML_START_ELEMENT)
            u16(16) // headerSize
            u32(size)
            u32(1) // lineNumber
            u32(NO_ENTRY) // comment

            // attrExt
            u32(NO_ENTRY) // element ns
            u32(elementNameIndex) // element name
            u16(20) // attributeStart
            u16(20) // attributeSize
            u16(count) // attributeCount
            u16(0) // idIndex
            u16(0) // classIndex
            u16(0) // styleIndex

            // attributes; attribute name string index == its position (names occupy leading pool indices)
            attrs.forEachIndexed { i, _ ->
                val enc = encoded[i]
                val nsIndex = uriIndexByUri[nsUriByAttr[i]] ?: NO_ENTRY
                u32(nsIndex) // ns
                u32(i) // name (string pool index == i)
                u32(enc.rawIndex) // rawValue
                // Res_value
                u16(8) // size
                u8(0) // res0
                u8(enc.dataType)
                u32(enc.data)
            }
        }
    }

    private fun buildEndElementChunk(elementNameIndex: Int) = LEWriter {
        u16(TYPE_XML_END_ELEMENT)
        u16(16) // headerSize
        u32(24) // size
        u32(1) // lineNumber
        u32(NO_ENTRY) // comment
        u32(NO_ENTRY) // ns
        u32(elementNameIndex) // name
    }

    private fun prefixForUri(uri: String) = when (uri) {
        "http://schemas.android.com/apk/res/android" -> "android"
        else -> "app"
    }

    /**
     * A namespace entry.
     */
    private class Namespace(
        val prefixIndex: Int,
        val uriIndex: Int,
        val uri: String
    )

    /**
     * A UTF-16 string pool builder (ResStringPool).
     */
    private class StringPool {

        private val strings = mutableListOf<String>()
        private val indices = hashMapOf<String, Int>()

        /**
         * Add (or reuse) a string, returning its index.
         */
        fun add(value: String) = indices.getOrPut(value) {
            strings.add(value)
            strings.size - 1
        }

        /**
         * Build the string pool chunk (UTF-16, little-endian).
         */
        fun build(): ByteArray {
            val count = strings.size
            val headerSize = 28
            val stringsStart = headerSize + count * 4

            val offsets = IntArray(count)

            // Encode string data and record offsets.
            val dataBytes = LEWriter {
                strings.forEachIndexed { i, s ->
                    offsets[i] = size()

                    // length prefix in UTF-16 code units (assume <= 0x7FFF).
                    val len = s.length
                    if (len > 0x7FFF) throw XmlParserException("Attribute string too long: \"$s\".")
                    u16(len)
                    for (c in s) u16(c.code)
                    u16(0) // null terminator
                }

                // Pad string data to a 4-byte boundary.
                while (size() % 4 != 0) u8(0)
            }
            val chunkSize = stringsStart + dataBytes.size

            return LEWriter {
                u16(TYPE_STRING_POOL)
                u16(headerSize)
                u32(chunkSize)
                u32(count) // stringCount
                u32(0) // styleCount
                u32(0) // flags (0 = UTF-16, not sorted)
                u32(stringsStart) // stringsStart
                u32(0) // stylesStart
                offsets.forEach { u32(it) }
                bytes(dataBytes)
            }
        }
    }
}