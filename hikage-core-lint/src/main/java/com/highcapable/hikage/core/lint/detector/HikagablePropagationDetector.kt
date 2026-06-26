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
 * This file is created by fankes on 2025/3/17.
 */
package com.highcapable.hikage.core.lint.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.highcapable.hikage.core.lint.DeclaredSymbol
import com.highcapable.hikage.core.lint.detector.extension.createKotlinOnlyUastHandler
import com.highcapable.hikage.core.lint.detector.extension.hasHikagable
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.tryResolve

class HikagablePropagationDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "MissingHikagableAnnotation",
            briefDescription = "Missing @Hikagable annotation.",
            explanation = "Functions which invoke `@Hikagable` functions must be marked with the `@Hikagable` annotation.",
            category = Category.CORRECTNESS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikagablePropagationDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private val functionRegex = "(\\s?.+)?fun\\s?".toRegex()
    }

    override fun getApplicableUastTypes() = listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        override fun visitMethod(node: UMethod) {
            val uastBody = node.uastBody as? UBlockExpression ?: return

            val bodyHasHikagable = uastBody.expressions.any {
                when (it) {
                    is UCallExpression -> it.resolve()?.hasHikagable() ?: false
                    is UReturnExpression ->
                        (it.returnExpression?.tryResolve() as? PsiMethod?)?.hasHikagable() ?: false
                    else -> false
                }
            }

            if (!node.hasHikagable() && bodyHasHikagable) {
                val location = context.getLocation(node)
                val nameLocation = context.getNameLocation(node)

                val message = "Function `${node.name}` must be marked with the `@Hikagable` annotation."

                val functionText = node.asSourceString()
                val hasDoubleSlash = functionText.startsWith("//")

                val replacement = functionRegex.replace(functionText) { result ->
                    val functionBody = result.groupValues.getOrNull(0) ?: functionText
                    val prefix = if (hasDoubleSlash) "\n" else ""
                    "$prefix@Hikagable $functionBody"
                }

                val lintFix = LintFix.create()
                    .name("Add '@Hikagable' to '${node.name}'")
                    .replace()
                    .range(location)
                    .with(replacement)
                    .imports(DeclaredSymbol.HIKAGABLE_ANNOTATION_CLASS)
                    .reformat(true)
                    .build()

                context.report(ISSUE, node, nameLocation, message, lintFix)
            }
        }
    })
}