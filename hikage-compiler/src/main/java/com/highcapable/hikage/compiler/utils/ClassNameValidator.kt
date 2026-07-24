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
 * This file is created by fankes on 2026/7/24.
 */
package com.highcapable.hikage.compiler.utils

import javax.lang.model.SourceVersion

/**
 * Validate the class name for Java and Kotlin.
 */
object ClassNameValidator {

    private val kotlinKeywords = setOf(
        "as", "break", "class", "continue", "do", "else", "false", "for", "fun",
        "if", "in", "interface", "is", "null", "object", "package", "return",
        "super", "this", "throw", "true", "try", "typealias", "typeof", "val",
        "var", "when", "while", "by", "catch", "constructor", "delegate",
        "dynamic", "field", "file", "finally", "get", "import", "init", "param",
        "property", "receiver", "set", "setparam", "where", "actual", "abstract",
        "annotation", "companion", "const", "crossinline", "data", "enum", "expect",
        "external", "final", "infix", "inline", "inner", "internal", "lateinit",
        "noinline", "open", "operator", "out", "override", "private", "protected",
        "public", "reified", "sealed", "suspend", "tailrec", "value", "vararg", "_"
    )

    fun check(name: String) = SourceVersion.isIdentifier(name) &&
        !SourceVersion.isKeyword(name) &&
        name !in kotlinKeywords && '$' !in name
}