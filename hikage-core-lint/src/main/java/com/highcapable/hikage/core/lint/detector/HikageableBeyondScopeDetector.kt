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
import com.highcapable.hikage.core.lint.detector.entity.ReportDetail
import com.highcapable.hikage.core.lint.detector.extension.hasHikageable
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.toUElementOfType

class HikageableBeyondScopeDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "HikageableBeyondScope",
            briefDescription = "Hikageable beyond scope.",
            explanation = "Functions marked with `@Hikageable` can only be passed in `Hikage.Performer`.",
            category = Category.COMPLIANCE,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageableBeyondScopeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {

        private val reportedNodes = mutableSetOf<UCallExpression>()
        private val reports = mutableListOf<ReportDetail>()

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(callExpr, method)
            organizeAndReport()
        }

        private fun startLint(callExpr: KtCallExpression, method: PsiMethod) {
            val className = method.containingClass?.qualifiedName ?: ""
            val hasHikageable = method.hasHikageable()
            val hasLayoutParams = className == DeclaredSymbol.HIKAGE_PERFORMER_CLASS && method.name == "LayoutParams"

            if (hasHikageable || hasLayoutParams) visitAndLint(callExpr, method)
        }

        private fun organizeAndReport() {
            reports.forEach {
                // Check if the call has been reported before reporting.
                if (reportedNodes.contains(it.callExpr)) return@forEach

                val location = context.getLocation(it.callExpr)
                val lintFix = LintFix.create()
                    .name("Delete Call Expression")
                    .replace()
                    .range(location)
                    // Delete the call expression.
                    .with("")
                    .build()

                context.report(ISSUE, it.callExpr, location, it.message, lintFix)
                reportedNodes.add(it.callExpr)
            }
        }

        private fun visitAndLint(callExpr: KtCallExpression, method: PsiMethod) {
            val bodyBlocks = mutableMapOf<String, KtExpression>()
            val parameters = method.parameterList.parameters
            val valueArguments = callExpr.valueArgumentList?.arguments ?: emptyList()

            fun visitValueArg(arg: KtValueArgument) {
                val name = arg.getArgumentName()?.asName?.identifier ?: ""
                val expr = arg.getArgumentExpression()

                val parameter = parameters.firstOrNull { it.name == name }
                    // If the last bit is a lambda expression, then `parameter` must have a lambda parameter defined by the last bit.
                    ?: if (arg is KtLambdaArgument) parameters.lastOrNull() else null

                val isMatched = parameter?.type?.canonicalText?.matches(DeclaredSymbol.HIKAGE_VIEW_REGEX) == true &&
                    !parameter.type.canonicalText.contains(DeclaredSymbol.HIKAGE_PERFORMER_CLASS)

                if (expr is KtLambdaExpression && isMatched)
                    expr.bodyExpression?.let { bodyBlocks[name] = it }
            }

            // Get the last lambda expression.
            val lastLambda = callExpr.lambdaArguments.lastOrNull()
            if (lastLambda != null) visitValueArg(lastLambda)

            valueArguments.forEach { arg -> visitValueArg(arg) }

            bodyBlocks.toList().flatMap { (_, value) -> value.children.filterIsInstance<KtCallExpression>() }.forEach {
                val expression = it.toUElementOfType<UCallExpression>() ?: return@forEach
                val sCallExpr = expression.sourcePsi as? KtCallExpression ?: return@forEach
                val sMethod = expression.resolve() ?: return@forEach

                if (sMethod.hasHikageable()) {
                    val message = "Performers are not allowed to appear in `${method.name}` DSL creation process."
                    reports.add(ReportDetail(message, expression))

                    // Recursively to visit next level.
                    visitAndLint(sCallExpr, sMethod)
                }
            }
        }
    }
}