/*
 * Hikage - A Kotlin DSL-based Android real-time UI building framework.
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
 * This file is created by fankes on 2026/6/21.
 */
package com.highcapable.hikage.core.lint.detector

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.highcapable.hikage.core.lint.DeclaredSymbol
import com.highcapable.hikage.core.lint.detector.extension.createKotlinOnlyUastHandler
import com.highcapable.hikage.core.lint.detector.extension.hasHikageable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaArgument
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.toUElementOfType

class HikageResourcesScopeDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "UseHikageResourcesScope",
            briefDescription = "Hikage resources scope violation.",
            explanation = "Hikage layouts can be created from different contexts or parents. Calling Android resource APIs " +
                "directly inside a Hikage DSL may accidentally use an outer Context or Activity instead of the current " +
                "performer context. Use ResourcesScope APIs such as stringResource, pluralStringResource, pluralTextResource, " +
                "textResource, colorResource, stateColorResource, drawableResource, dimenResource and fontResource to keep resource access scoped " +
                "to the current Hikage creation context.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikageResourcesScopeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val ANDROID_CONTEXT_CLASS = "android.content.Context"
        private const val ANDROID_RESOURCES_CLASS = "android.content.res.Resources"
        private const val CONTEXT_COMPAT_CLASS = "androidx.core.content.ContextCompat"
        private const val RESOURCES_COMPAT_CLASS = "androidx.core.content.res.ResourcesCompat"
        private const val BETTERANDROID_RESOURCES_UTILS_CLASS =
            "com.highcapable.betterandroid.ui.extension.component.base.ResourcesUtils"

        private const val STRING_RESOURCE_FUNCTION = "stringResource"
        private const val PLURAL_STRING_RESOURCE_FUNCTION = "pluralStringResource"
        private const val PLURAL_TEXT_RESOURCE_FUNCTION = "pluralTextResource"
        private const val TEXT_RESOURCE_FUNCTION = "textResource"
        private const val STRING_ARRAY_RESOURCE_FUNCTION = "stringArrayResource"
        private const val INTEGER_RESOURCE_FUNCTION = "integerResource"
        private const val INTEGER_ARRAY_RESOURCE_FUNCTION = "integerArrayResource"
        private const val BOOLEAN_RESOURCE_FUNCTION = "booleanResource"
        private const val COLOR_RESOURCE_FUNCTION = "colorResource"
        private const val STATE_COLOR_RESOURCE_FUNCTION = "stateColorResource"
        private const val DRAWABLE_RESOURCE_FUNCTION = "drawableResource"
        private const val DIMEN_RESOURCE_FUNCTION = "dimenResource"
        private const val DIMEN_PIXEL_SIZE_RESOURCE_FUNCTION = "dimenPixelSizeResource"
        private const val DIMEN_PIXEL_OFFSET_RESOURCE_FUNCTION = "dimenPixelOffsetResource"
        private const val FRACTION_RESOURCE_FUNCTION = "fractionResource"
        private const val FONT_RESOURCE_FUNCTION = "fontResource"

        private val resourceMethodNames = listOf(
            "getString",
            "getQuantityString",
            "getQuantityText",
            "getText",
            "getStringArray",
            "getInteger",
            "getIntArray",
            "getBoolean",
            "getColor",
            "getColorStateList",
            "getDrawable",
            "getDimension",
            "getDimensionPixelSize",
            "getDimensionPixelOffset",
            "getFraction",
            "getFont",
            "getColorCompat",
            "getColorStateListCompat",
            "getDrawableCompat",
            "getDrawableCompatTyped",
            "getFontCompat"
        )
    }

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        private val reportedResources = hashSetOf<ReportedResource>()

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return
            val replacement = method.toResourceReplacement(context) ?: return
            if (!node.isInsideHikagePerformerScope(callExpr)) return

            val reportTarget = node.fullCallSourcePsi() ?: callExpr
            if (!reportedResources.add(reportTarget.toReportedResource(replacement))) return

            val location = context.getLocation(reportTarget)
            val lintFix = node.createReplacementFix(location, replacement)
            val message = "Use `${replacement.functionName}` to access resources from the current Hikage performer scope."

            context.report(ISSUE, reportTarget, location, message, lintFix)
        }
    })

    private fun PsiMethod.toResourceReplacement(context: JavaContext): ResourceReplacement? {
        if (name !in resourceMethodNames) return null

        val className = containingClass?.qualifiedName ?: return null
        return when {
            className == CONTEXT_COMPAT_CLASS -> when (name) {
                "getColor" -> ResourceReplacement(COLOR_RESOURCE_FUNCTION, 1)
                "getColorStateList" -> ResourceReplacement(STATE_COLOR_RESOURCE_FUNCTION, 1)
                "getDrawable" -> ResourceReplacement(DRAWABLE_RESOURCE_FUNCTION, 1)
                else -> null
            }
            className == RESOURCES_COMPAT_CLASS -> when (name) {
                "getColor" -> ResourceReplacement(COLOR_RESOURCE_FUNCTION, 1)
                "getColorStateList" -> ResourceReplacement(STATE_COLOR_RESOURCE_FUNCTION, 1)
                "getDrawable" -> ResourceReplacement(DRAWABLE_RESOURCE_FUNCTION, 1)
                "getFont" -> ResourceReplacement(FONT_RESOURCE_FUNCTION, 1)
                else -> null
            }
            className == BETTERANDROID_RESOURCES_UTILS_CLASS -> when (name) {
                "getColorCompat" -> ResourceReplacement(COLOR_RESOURCE_FUNCTION)
                "getColorStateListCompat" -> ResourceReplacement(STATE_COLOR_RESOURCE_FUNCTION)
                "getDrawableCompat", "getDrawableCompatTyped" -> ResourceReplacement(DRAWABLE_RESOURCE_FUNCTION)
                "getFontCompat" -> ResourceReplacement(FONT_RESOURCE_FUNCTION)
                else -> null
            }
            extendsClass(context, ANDROID_CONTEXT_CLASS) -> when (name) {
                "getString" -> ResourceReplacement(STRING_RESOURCE_FUNCTION, keepRemainingArguments = true)
                "getText" -> ResourceReplacement(TEXT_RESOURCE_FUNCTION)
                "getColor" -> ResourceReplacement(COLOR_RESOURCE_FUNCTION)
                "getColorStateList" -> ResourceReplacement(STATE_COLOR_RESOURCE_FUNCTION)
                "getDrawable" -> ResourceReplacement(DRAWABLE_RESOURCE_FUNCTION)
                "getFont" -> ResourceReplacement(FONT_RESOURCE_FUNCTION)
                else -> null
            }
            extendsClass(context, ANDROID_RESOURCES_CLASS) -> when (name) {
                "getString" -> ResourceReplacement(STRING_RESOURCE_FUNCTION, keepRemainingArguments = true)
                "getQuantityString" -> ResourceReplacement(PLURAL_STRING_RESOURCE_FUNCTION, keepRemainingArguments = true)
                "getQuantityText" -> ResourceReplacement(PLURAL_TEXT_RESOURCE_FUNCTION, keepRemainingArguments = true)
                "getText" -> ResourceReplacement(TEXT_RESOURCE_FUNCTION)
                "getStringArray" -> ResourceReplacement(STRING_ARRAY_RESOURCE_FUNCTION)
                "getInteger" -> ResourceReplacement(INTEGER_RESOURCE_FUNCTION)
                "getIntArray" -> ResourceReplacement(INTEGER_ARRAY_RESOURCE_FUNCTION)
                "getBoolean" -> ResourceReplacement(BOOLEAN_RESOURCE_FUNCTION)
                "getColor" -> ResourceReplacement(COLOR_RESOURCE_FUNCTION)
                "getColorStateList" -> ResourceReplacement(STATE_COLOR_RESOURCE_FUNCTION)
                "getDrawable" -> ResourceReplacement(DRAWABLE_RESOURCE_FUNCTION)
                "getDimension" -> ResourceReplacement(DIMEN_RESOURCE_FUNCTION)
                "getDimensionPixelSize" -> ResourceReplacement(DIMEN_PIXEL_SIZE_RESOURCE_FUNCTION)
                "getDimensionPixelOffset" -> ResourceReplacement(DIMEN_PIXEL_OFFSET_RESOURCE_FUNCTION)
                "getFraction" -> ResourceReplacement(FRACTION_RESOURCE_FUNCTION, keepRemainingArguments = true)
                "getFont" -> ResourceReplacement(FONT_RESOURCE_FUNCTION)
                else -> null
            }
            else -> null
        }
    }

    private fun PsiMethod.extendsClass(context: JavaContext, className: String): Boolean {
        val clazz = containingClass ?: return false
        return clazz.qualifiedName == className || context.evaluator.extendsClass(clazz, className, false)
    }

    private fun UCallExpression.isInsideHikagePerformerScope(callExpr: KtCallExpression): Boolean {
        var currentUast = uastParent
        while (currentUast != null) {
            val method = (currentUast as? UMethod)?.javaPsi
            if (method?.isHikagePerformerFunction() == true) return true
            currentUast = currentUast.uastParent
        }

        var currentPsi: PsiElement? = callExpr.parent
        while (currentPsi != null) {
            val lambda = currentPsi as? KtLambdaExpression
            if (lambda?.isHikagePerformerArgument() == true) return true
            currentPsi = currentPsi.parent
        }

        return false
    }

    private fun PsiMethod.isHikagePerformerFunction() =
        hasHikageable() && parameterList.parameters.any { it.type.isHikagePerformerType() }

    private fun KtLambdaExpression.isHikagePerformerArgument(): Boolean {
        val argument = findValueArgument() ?: return false
        val callExpr = argument.findOwnerCallExpression() ?: return false
        val method = callExpr.toUElementOfType<UCallExpression>()?.resolve() ?: return false
        val parameter = callExpr.findParameter(argument, method) ?: return false

        return parameter.type.isHikagePerformerType()
    }

    private fun KtLambdaExpression.findValueArgument(): KtValueArgument? {
        var current: PsiElement? = parent
        while (current != null) {
            when (current) {
                is KtLambdaArgument -> return current
                is KtValueArgument -> return current
                is KtCallExpression -> return null
            }
            current = current.parent
        }

        return null
    }

    private fun KtValueArgument.findOwnerCallExpression(): KtCallExpression? {
        var current: PsiElement? = parent
        while (current != null) {
            if (current is KtCallExpression) return current
            current = current.parent
        }

        return null
    }

    private fun KtCallExpression.findParameter(argument: KtValueArgument, method: PsiMethod): PsiParameter? {
        val parameters = method.parameterList.parameters
        argument.getArgumentName()?.asName?.identifier?.let { name ->
            return parameters.firstOrNull { it.name == name }
        }

        if (argument is KtLambdaArgument) return parameters.lastOrNull()

        val argumentIndex = valueArgumentList?.arguments?.indexOf(argument) ?: -1
        return parameters.getOrNull(argumentIndex)
    }

    private fun PsiType.isHikagePerformerType() =
        canonicalText.contains(DeclaredSymbol.HIKAGE_PERFORMER_CLASS)

    private fun UCallExpression.fullCallSourcePsi() = when (val parent = uastParent) {
        is UQualifiedReferenceExpression -> if (parent.selector == this) parent.sourcePsi else sourcePsi
        else -> sourcePsi
    }

    private fun UCallExpression.createReplacementFix(
        location: Location,
        replacement: ResourceReplacement
    ): LintFix? {
        val arguments = valueArguments
        val replacementArgs = if (replacement.keepRemainingArguments) {
            arguments.drop(replacement.resourceArgumentIndex)
        } else {
            arguments.getOrNull(replacement.resourceArgumentIndex)?.let(::listOf).orEmpty()
        }
        if (replacementArgs.isEmpty()) return null

        val replacementText = replacementArgs.joinToString { it.asSourceString() }
        return LintFix.create()
            .name("Replace with '${replacement.functionName}'")
            .replace()
            .range(location)
            .with("${replacement.functionName}($replacementText)")
            .reformat(true)
            .build()
    }

    private fun PsiElement.toReportedResource(replacement: ResourceReplacement) = ReportedResource(
        filePath = containingFile?.virtualFile?.path,
        startOffset = textRange.startOffset,
        endOffset = textRange.endOffset,
        functionName = replacement.functionName
    )

    private data class ResourceReplacement(
        val functionName: String,
        val resourceArgumentIndex: Int = 0,
        val keepRemainingArguments: Boolean = false
    )

    private data class ReportedResource(
        val filePath: String?,
        val startOffset: Int,
        val endOffset: Int,
        val functionName: String
    )
}