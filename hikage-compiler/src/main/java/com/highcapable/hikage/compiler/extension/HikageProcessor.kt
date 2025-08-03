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
 * This file is created by fankes on 2025/3/30.
 */
package com.highcapable.hikage.compiler.extension

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ClassName

fun KSDeclaration.getClassDeclaration(resolver: Resolver) =
    if (this is KSClassDeclaration) this else qualifiedName?.let { resolver.getClassDeclarationByName(it) }

fun KSDeclaration.getSimpleNameString(): String {
    val packageName = packageName.asString()
    return qualifiedName?.asString()?.replace("$packageName.", "") ?: simpleName.asString()
}

fun KSClassDeclaration.isSubclassOf(superType: KSType?): Boolean {
    if (superType == null) return false
    if (this == superType.declaration) return true

    superTypes.forEach { parent ->
        val resolvedParent = parent.resolve()
        // Direct match.
        if (resolvedParent == superType) return true

        val parentDeclaration = resolvedParent.declaration as? KSClassDeclaration
            ?: return@forEach

        // Recursively check the parent class.
        if (parentDeclaration.isSubclassOf(superType)) return true
    }

    return false
}

fun KSClassDeclaration.asType() = asType(emptyList())

inline fun <reified T> List<KSValueArgument>.getOrNull(name: String) =
    firstOrNull { it.name?.asString() == name }?.value as? T?

fun ClassName.getTypedSimpleName() = simpleName.replace(".", "_")

object ClassDetector {

    private val javaKeywords = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while"
    )

    private val invalidPattern = "^(\\d.*|.*[^A-Za-z0-9_\$].*)$".toRegex()

    fun verify(name: String) = name !in javaKeywords && !invalidPattern.matches(name)
}