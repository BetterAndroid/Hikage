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
 * This file is created by fankes on 2026/8/30.
 */
package com.highcapable.hikage.core.attribute.resolver

import android.content.Context
import android.content.res.XmlResourceParser
import com.highcapable.hikage.core.R
import com.highcapable.hikage.core.attribute.exception.AttributeResolvingException
import org.xmlpull.v1.XmlPullParser

/**
 * The resolver for a simple framework attribute parser.
 */
internal object SimpleAttributeSetResolver {

    /**
     * Run [block] with a simple framework [XmlResourceParser] from the layout.
     *
     * The parser owns the native XML document, so it must remain open while framework view
     * constructors read it.
     * @param context the context.
     * @param block the block to run.
     * @return [R]
     */
    inline fun <R> withParser(context: Context, block: (XmlResourceParser) -> R) =
        context.resources.getLayout(R.layout.layout_hikage_simple_attribute_node).use { parser ->
            var type = parser.next()
            while (type != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
                type = parser.next()
            if (type != XmlPullParser.START_TAG)
                throw AttributeResolvingException("Failed to resolve simple attribute parser.")

            block(parser)
        }
}