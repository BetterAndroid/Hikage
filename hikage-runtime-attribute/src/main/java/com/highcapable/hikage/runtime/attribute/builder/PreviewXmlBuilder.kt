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
 * This file is created by fankes on 2026/6/3.
 */
package com.highcapable.hikage.runtime.attribute.builder

import android.content.Context
import android.util.Xml
import com.highcapable.hikage.runtime.attribute.encoder.AttributeXmlValueEncoder
import com.highcapable.hikage.runtime.attribute.entity.AttributeResolverParams
import com.highcapable.hikage.runtime.attribute.entity.ResolvedAttribute
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.io.StringWriter

/**
 * Builds a text XML parser for Android Studio layout preview.
 *
 * Layoutlib resolves attributes from XML text through `BridgeXmlPullAttributes`, so this path keeps
 * XML-equivalent string values while adapting raw Kotlin values into their corresponding XML forms.
 */
internal object PreviewXmlBuilder : BaseXmlBuilder() {

    /** The synthetic element tag name. */
    private const val ELEMENT_NAME = "View"

    /**
     * Build a text XML parser from the given [attrs].
     * @param context the context.
     * @param attrs the resolved attributes.
     * @param params the parameters.
     * @return [XmlPullParser]
     */
    fun build(
        context: Context,
        attrs: List<ResolvedAttribute>,
        params: AttributeResolverParams
    ): XmlPullParser {
        val writer = StringWriter()
        val serializer = Xml.newSerializer()
        serializer.setOutput(writer)
        serializer.startDocument("utf-8", true)
        attrs.map { it.namespaceUri }.distinct().forEach { uri ->
            serializer.setPrefix(uriPrefix(uri), uri)
        }
        serializer.startTag(null, ELEMENT_NAME)
        attrs.forEach { attr ->
            serializer.attribute(
                attr.namespaceUri,
                attr.item.name,
                AttributeXmlValueEncoder.encode(context, attr.item, params)
            )
        }
        serializer.endTag(null, ELEMENT_NAME)
        serializer.endDocument()

        return Xml.newPullParser().apply {
            setInput(StringReader(writer.toString()))
        }
    }
}