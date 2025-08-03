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
import com.highcapable.hikage.core.lint.detector.extension.hasHikageable
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UBlockExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UReturnExpression
import org.jetbrains.uast.tryResolve

class HikageableFunctionsDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "HikageableFunctions",
            briefDescription = "Hikageable functions.",
            explanation = "Functions which invoke `@Hikageable` functions must be marked with the `@Hikageable` annotation.",
            category = Category.COMPLIANCE,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageableFunctionsDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private val functionRegex = "(\\s?.+)?fun\\s?".toRegex()
    }

    override fun getApplicableUastTypes() = listOf(UMethod::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {

        override fun visitMethod(node: UMethod) {
            val uastBody = node.uastBody as? UBlockExpression ?: return

            val bodyHasHikageable = uastBody.expressions.any {
                when (it) {
                    is UCallExpression -> it.resolve()?.hasHikageable() ?: false
                    is UReturnExpression ->
                        (it.returnExpression?.tryResolve() as? PsiMethod?)?.hasHikageable() ?: false
                    else -> false
                }
            }

            if (!node.hasHikageable() && bodyHasHikageable) {
                val location = context.getLocation(node)
                val nameLocation = context.getNameLocation(node)

                val message = "Function `${node.name}` must be marked with the `@Hikageable` annotation."

                val functionText = node.asSourceString()
                val hasDoubleSlash = functionText.startsWith("//")

                val replacement = functionRegex.replace(functionText) { result ->
                    val functionBody = result.groupValues.getOrNull(0) ?: functionText
                    val prefix = if (hasDoubleSlash) "\n" else ""
                    "$prefix@Hikageable $functionBody"
                }

                val lintFix = LintFix.create()
                    .name("Add '@Hikageable' to '${node.name}'")
                    .replace()
                    .range(location)
                    .with(replacement)
                    .imports(DeclaredSymbol.HIKAGEABLE_ANNOTATION_CLASS)
                    .reformat(true)
                    .build()

                context.report(ISSUE, node, nameLocation, message, lintFix)
            }
        }
    }
}