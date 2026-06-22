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
package com.highcapable.hikage.runtime.attribute.resolver

import android.content.Context
import android.content.res.XmlResourceParser
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem
import com.highcapable.hikage.runtime.attribute.entity.AttributeResolverParams
import com.highcapable.hikage.runtime.attribute.extension.isLayoutPreview
import java.io.Closeable

/**
 * The resolver for creating an [XmlResourceParser].
 */
internal interface XmlResourceParserResolver : Closeable {

    companion object {

        /**
         * Create a new [XmlResourceParserResolver] from the given context.
         * @param context the context.
         * @return [XmlResourceParserResolver]
         */
        fun from(context: Context): XmlResourceParserResolver =
            if (context.isLayoutPreview()) BridgeXmlBlockResolver(context) else XmlBlockResolver(context)
    }

    /**
     * Create a new parser from a synthesized binary XML with [attrs].
     * @param attrs the resolved attribute item.
     * @param params the parameters.
     * @return [XmlResourceParser]
     */
    fun newParser(
        attrs: List<AttributeItem> = listOf(),
        params: AttributeResolverParams = AttributeResolverParams()
    ): XmlResourceParser

    /**
     * Release a parser after the view creation process has completed.
     * @param parser the parser.
     */
    fun release(parser: XmlResourceParser) = Unit
}