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
package com.highcapable.hikage.core.attrs.runtime.resolver

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import com.highcapable.hikage.core.attrs.entity.AttributeItem
import com.highcapable.hikage.core.attrs.runtime.builder.PreviewXmlBuilder
import com.highcapable.hikage.core.attrs.runtime.resolver.BridgeXmlBlockResolver.bridgeParserConstructor
import com.highcapable.hikage.core.base.XmlParserException
import com.highcapable.hikage.core.layout.bypass.findLayoutPreviewContext
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.isSubclassOf
import com.highcapable.kavaref.extension.toClass
import org.xmlpull.v1.XmlPullParser

/**
 * The layoutlib resolver for creating a `BridgeXmlBlockParser`.
 *
 * Layoutlib rejects ordinary [AttributeSet] implementations in
 * `BridgeContext.internalObtainStyledAttributes`, so this resolver creates a text XML parser and wraps
 * it with layoutlib's own bridge parser. The text values are then resolved by layoutlib with the same
 * semantics as attributes written in a layout XML file.
 */
internal object BridgeXmlBlockResolver : AttributeSetResolver {

    /** The Android Studio layout preview context class simple name. */
    private const val BRIDGE_CONTEXT_CLASS_NAME = "BridgeContext"

    /** The layoutlib bridge parser class simple name. */
    private const val BRIDGE_XML_BLOCK_PARSER_CLASS_NAME = "BridgeXmlBlockParser"

    /** The `ResourceNamespace.fromPackageName` method name. */
    private const val FROM_PACKAGE_NAME_METHOD_NAME = "fromPackageName"

    /** The `ResourceNamespace.RES_AUTO` field name. */
    private const val RES_AUTO_FIELD_NAME = "RES_AUTO"

    /** The `BridgeXmlBlockParser.ensurePopped` method name. */
    private const val ENSURE_POPPED_METHOD_NAME = "ensurePopped"

    /** The layoutlib bridge parser class. */
    private var bridgeParserClass: Class<XmlResourceParser>? = null

    /** The `BridgeXmlBlockParser(XmlPullParser, BridgeContext, ResourceNamespace)` constructor. */
    private val bridgeParserConstructor by lazy {
        requireBridgeParserClass().resolve()
            .optional(silent = true)
            .firstConstructorOrNull {
                parameterCount = 3
                parameters { types ->
                    val condition1 = types[0] isSubclassOf XmlPullParser::class
                    val condition2 = requireBridgeContextClass() isSubclassOf types[1]

                    condition1 && condition2
                }
            } ?: throw XmlParserException("Cannot find the $BRIDGE_XML_BLOCK_PARSER_CLASS_NAME constructor.")
    }

    /** The layoutlib `ResourceNamespace` class accepted by [bridgeParserConstructor]. */
    private val resourceNamespaceClass by lazy {
        bridgeParserConstructor.self.parameterTypes[2].toClass()
    }

    /** The static `ResourceNamespace.fromPackageName(String)` method. */
    private val namespaceFromPackageName by lazy {
        resourceNamespaceClass.resolve()
            .optional(silent = true)
            .firstMethodOrNull {
                name = FROM_PACKAGE_NAME_METHOD_NAME
                parameters(String::class)
                modifiers(Modifiers.STATIC)
            }
    }

    /** The static `ResourceNamespace.RES_AUTO` field. */
    private val namespaceResAuto by lazy {
        resourceNamespaceClass.resolve()
            .optional(silent = true)
            .firstFieldOrNull {
                name = RES_AUTO_FIELD_NAME
                modifiers(Modifiers.STATIC)
            }
    }

    /** The `BridgeXmlBlockParser.ensurePopped()` method. */
    private val ensurePopped by lazy {
        requireBridgeParserClass().resolve()
            .optional(silent = true)
            .firstMethodOrNull {
                name = ENSURE_POPPED_METHOD_NAME
                emptyParameters()
            }
    }

    override fun newParser(context: Context, attrs: List<AttributeItem>): XmlResourceParser {
        val bridgeContext = context.findLayoutPreviewContext() ?: throw XmlParserException(
            "Failed to find $BRIDGE_CONTEXT_CLASS_NAME while creating layout preview attributes."
        )
        val parser = PreviewXmlBuilder.build(context, AttributeResolver.resolve(context, attrs))
        val bridgeParser = createBridgeParser(bridgeContext, context.packageName, parser)
        advanceToStartTag(bridgeParser)

        return bridgeParser
    }

    override fun release(parser: XmlResourceParser) {
        ensurePopped?.copy()?.of(parser)?.invokeQuietly()
    }

    /**
     * Create a `BridgeXmlBlockParser` from the given text XML [parser].
     * @param bridgeContext the layoutlib bridge context.
     * @param packageName the application package name.
     * @param parser the text XML parser.
     * @return [XmlResourceParser]
     */
    private fun createBridgeParser(bridgeContext: Context, packageName: String, parser: XmlPullParser): XmlResourceParser {
        initBridgeParserClass(bridgeContext)

        val namespace = createResourceNamespace(packageName)
        return bridgeParserConstructor.createQuietly(parser, bridgeContext, namespace)
            ?: throw XmlParserException("Failed to create $BRIDGE_XML_BLOCK_PARSER_CLASS_NAME.")
    }

    /**
     * Load the layoutlib bridge parser class from the same package as [bridgeContext].
     *
     * Layoutlib classes are not compile-time dependencies of Hikage. The only stable object we have at
     * runtime is the preview context instance, so the parser class is resolved from its class loader.
     * @param bridgeContext the layoutlib bridge context.
     * @return [Class]
     */
    private fun initBridgeParserClass(bridgeContext: Context) {
        if (bridgeParserClass != null) return

        val bridgeContextClass = bridgeContext.javaClass
        val packageName = bridgeContextClass.name.substringBeforeLast('.', missingDelimiterValue = "")
        if (packageName.isEmpty()) throw XmlParserException("Cannot resolve the $BRIDGE_CONTEXT_CLASS_NAME package.")

        bridgeParserClass = runCatching {
            val classLoader = bridgeContextClass.classLoader ?: ClassLoader.getSystemClassLoader()
            classLoader.loadClass("$packageName.$BRIDGE_XML_BLOCK_PARSER_CLASS_NAME").toClass<XmlResourceParser>()
        }.getOrElse { throw XmlParserException("Cannot load $BRIDGE_XML_BLOCK_PARSER_CLASS_NAME: ${it.message}") }
    }

    /**
     * Require the layoutlib bridge parser class.
     * @return [Class]<[XmlResourceParser]>
     */
    private fun requireBridgeParserClass() = bridgeParserClass
        ?: throw XmlParserException("$BRIDGE_XML_BLOCK_PARSER_CLASS_NAME has not been initialized.")

    /**
     * Require the Android Studio layout preview context class.
     * @return [Class]<[Context]>
     */
    private fun requireBridgeContextClass(): Class<Context> {
        val parserClass = requireBridgeParserClass()
        val classLoader = parserClass.classLoader ?: ClassLoader.getSystemClassLoader()

        @SuppressLint("NewApi")
        return classLoader.loadClass("${parserClass.packageName}.$BRIDGE_CONTEXT_CLASS_NAME").toClass<Context>()
    }

    /**
     * Create the layoutlib file resource namespace passed into `BridgeXmlBlockParser`.
     *
     * Newer layoutlib exposes `ResourceNamespace.fromPackageName(String)`. If that method is not
     * available, fall back to `ResourceNamespace.RES_AUTO`, which is sufficient for resolving app
     * attributes from `res-auto` XML namespace.
     * @param packageName the application package name.
     * @return the layoutlib resource namespace instance.
     */
    private fun createResourceNamespace(packageName: String): Any {
        namespaceFromPackageName?.invokeQuietly(packageName)?.let { return it }
        return namespaceResAuto?.getQuietly() ?: throw XmlParserException("Cannot create the layoutlib resource namespace.")
    }

    /**
     * Advance the [parser] to its start element.
     * @param parser the parser.
     */
    private fun advanceToStartTag(parser: XmlResourceParser) {
        runCatching {
            var event = parser.eventType
            while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT)
                event = parser.next()
        }.getOrElse {
            throw XmlParserException("Failed to advance $BRIDGE_XML_BLOCK_PARSER_CLASS_NAME to the start tag: ${it.message}")
        }
    }
}