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
 * This file is created by fankes on 2025/4/2.
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
import org.jetbrains.uast.UArrayAccessExpression
import org.jetbrains.uast.UBinaryExpressionWithType
import org.jetbrains.uast.UParenthesizedExpression
import org.jetbrains.uast.UQualifiedReferenceExpression

class HikageSafeTypeCastDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "UseHikageSafeTypeCast",
            briefDescription = "Hikage safe type cast usage.",
            explanation = "Recommended to use `hikage.get<YourView>(\"your_id\")` instead of `hikage[\"your_id\"] as YourView`.",
            category = Category.COMPLIANCE,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikageSafeTypeCastDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableUastTypes() = listOf(UQualifiedReferenceExpression::class.java, UBinaryExpressionWithType::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {

        override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
            if (node.selector !is UBinaryExpressionWithType) return

            val castExpr = node.selector as UBinaryExpressionWithType
            visitAndLint(context, castExpr, node)
        }

        override fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType) {
            visitAndLint(context, node)
        }

        private fun visitAndLint(
            context: JavaContext,
            node: UBinaryExpressionWithType,
            parent: UQualifiedReferenceExpression? = null
        ) {
            // Get the parent node, if it is wrapped with brackets will also be included.
            val locationNode = node.uastParent as? UParenthesizedExpression ?: node

            val receiver = parent?.receiver ?: node.operand
            val receiverType = (node.operand as? UArrayAccessExpression)?.receiver?.getExpressionType() ?: return
            val receiverClass = receiverType.canonicalText

            // Filter retains results that meet the conditions.
            if (receiverClass != DeclaredSymbol.HIKAGE_CLASS) return

            // Like `hikage["your_id"] as YourView`.
            val exprText = node.sourcePsi?.text ?: return
            // Like `hikage["your_id"]`.
            val receiverText = receiver.sourcePsi?.text ?: return
            // Like `hikage`.
            val receiverNameText = receiverText.split("[")[0]
            // Like `"your_id"`.
            val receiverContent = runCatching { receiverText.split("[")[1].split("]")[0] }.getOrNull() ?: return

            val isSafeCast = exprText.contains("as?") || exprText.endsWith("?")

            // Like `YourView`.
            val castTypeContent = node.typeReference?.sourcePsi?.text?.removeSuffix("?") ?: return

            val replacement = "$receiverNameText.${if (isSafeCast) "getOrNull" else "get"}<$castTypeContent>($receiverContent)"
            val replaceSuggestion = if (isSafeCast) "Hikage.getOrNull<$castTypeContent>" else "Hikage.get<$castTypeContent>"

            val location = context.getLocation(locationNode)
            val lintFix = LintFix.create()
                .name("Replace with '$replacement'")
                .replace()
                .range(location)
                .with(replacement)
                .reformat(true)
                .build()

            context.report(
                ISSUE, locationNode, location,
                message = "Can be replaced with safe type cast `$replaceSuggestion`.",
                quickfixData = lintFix
            )
        }
    }
}