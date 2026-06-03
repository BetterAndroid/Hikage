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
 * This file is created by fankes on 2026/6/3.
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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.uast.UCallExpression

class HikageAttributeDetector : Detector(), Detector.UastScanner {

    companion object {

        val MISSING_NAMESPACE_ISSUE = Issue.create(
            id = "MissingHikageAttributeNamespace",
            briefDescription = "Hikage attribute missing namespace.",
            explanation = "Attributes declared at the root attribute scope must include a namespace prefix.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val DUPLICATE_ISSUE = Issue.create(
            id = "DuplicateHikageAttribute",
            briefDescription = "Hikage attribute duplicate.",
            explanation = "Attributes declared in the same attribute scope must not use duplicate keys.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val NAMESPACE_ISSUE = Issue.create(
            id = "ReplaceWithHikageAttributeNamespaceShortcuts",
            briefDescription = "Hikage attribute namespace usage.",
            explanation = "Use Hikage attribute namespace shortcuts and keep attribute names consistent with their namespace scope.",
            category = Category.USABILITY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val NAMESPACE_FUNCTION = "namespace"
        private const val SET_FUNCTION = "set"
        private const val HIKAGE_ATTRIBUTE_FUNCTION = "HikageAttribute"
        private const val ANDROID_NAMESPACE = "android"
        private const val APP_NAMESPACE = "app"
        private const val ATTRS_UTILS_SUFFIX = ".attrs.HikageAttributeUtils"
        private const val ATTRIBUTE_SCOPE_SUFFIX = ".attrs.AttributeScope"
        private const val HIKAGE_CLASS_SUFFIX = ".Hikage"
    }

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {

        private val attributes = hashMapOf<PsiElement, MutableMap<String, PsiElement>>()

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(context, node, callExpr, method, attributes)
        }
    }

    private fun startLint(
        context: JavaContext,
        node: UCallExpression,
        callExpr: KtCallExpression,
        method: PsiMethod,
        attributes: MutableMap<PsiElement, MutableMap<String, PsiElement>>
    ) {
        when {
            method.isHikageNamespaceFunction() -> visitAndReportNamespaceShortcut(context, callExpr)
            method.isHikageRootSetFunction() -> visitAndReportRootSet(context, node, callExpr)
            method.isHikageScopeSetFunction() -> visitAndReportScopedSet(context, node, callExpr)
        }

        if (method.isHikageRootSetFunction() || method.isHikageScopeSetFunction())
            visitAndReportDuplicate(context, node, callExpr, attributes)
    }

    private fun visitAndReportNamespaceShortcut(context: JavaContext, callExpr: KtCallExpression) {
        val namespace = callExpr.firstStringLiteralText() ?: return
        if (namespace != ANDROID_NAMESPACE && namespace != APP_NAMESPACE) return

        val location = context.getLocation(callExpr)
        val replacement = callExpr.namespaceShortcutReplacement(namespace) ?: return
        val lintFix = LintFix.create()
            .name("Replace with '$namespace'")
            .replace()
            .range(location)
            .with(replacement)
            .imports("$attrsPackageName.$namespace")
            .reformat(true)
            .build()

        context.report(
            NAMESPACE_ISSUE,
            callExpr,
            location,
            message = "Can be simplified to `$namespace`.",
            quickfixData = lintFix
        )
    }

    private fun visitAndReportRootSet(context: JavaContext, node: UCallExpression, callExpr: KtCallExpression) {
        val attrName = callExpr.firstStringLiteralText() ?: return
        if (attrName.contains(':')) return

        val attrNameExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return
        val location = context.getLocation(attrNameExpr)
        val lintFix = LintFix.create()
            .name("Prefix with 'android:'")
            .replace()
            .range(location)
            .with("\"$ANDROID_NAMESPACE:$attrName\"")
            .reformat(true)
            .build()

        context.report(
            MISSING_NAMESPACE_ISSUE,
            attrNameExpr,
            location,
            message = "Attribute `$attrName` must include a namespace or be declared inside a namespace.",
            quickfixData = lintFix
        )
    }

    private fun visitAndReportScopedSet(context: JavaContext, node: UCallExpression, callExpr: KtCallExpression) {
        val namespace = callExpr.findAttributeNamespace() ?: return
        val attrName = callExpr.firstStringLiteralText() ?: return
        val separator = attrName.indexOf(':')
        if (separator < 0) return

        val attrNamespace = attrName.substring(0, separator)
        val unprefixedName = attrName.substring(separator + 1)
        if (attrNamespace.isEmpty() || unprefixedName.isEmpty()) return

        val attrNameExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return
        val location = context.getLocation(attrNameExpr)
        if (attrNamespace == namespace) {
            val lintFix = LintFix.create()
                .name("Remove redundant '$namespace:' prefix")
                .replace()
                .range(location)
                .with("\"$unprefixedName\"")
                .reformat(true)
                .build()

            context.report(
                NAMESPACE_ISSUE,
                attrNameExpr,
                location,
                message = "Attribute `$attrName` is already inside the `$namespace` namespace.",
                quickfixData = lintFix
            )
            return
        }

        context.report(
            NAMESPACE_ISSUE,
            attrNameExpr,
            location,
            message = "Attribute `$attrName` uses the `$attrNamespace` namespace inside the `$namespace` namespace."
        )
    }

    private fun visitAndReportDuplicate(
        context: JavaContext,
        node: UCallExpression,
        callExpr: KtCallExpression,
        attributes: MutableMap<PsiElement, MutableMap<String, PsiElement>>
    ) {
        val attrName = callExpr.firstStringLiteralText() ?: return
        val attrKey = attrName.attributeKey() ?: return
        val attrNameExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return
        val root = callExpr.findAttributeRoot() ?: return
        val exists = attributes.getOrPut(root) { hashMapOf() }.putIfAbsent(attrKey, attrNameExpr)
        if (exists == null) return

        val location = context.getLocation(attrNameExpr)
        context.report(
            DUPLICATE_ISSUE,
            attrNameExpr,
            location,
            message = "Attribute `$attrKey` is duplicated in the same attribute scope."
        )
    }

    private fun PsiMethod.isHikageNamespaceFunction() =
        name == NAMESPACE_FUNCTION && containingClass?.qualifiedName == attrsUtilsClassName

    private fun PsiMethod.isHikageRootSetFunction() =
        name == SET_FUNCTION && containingClass?.qualifiedName == attrsUtilsClassName

    private fun PsiMethod.isHikageScopeSetFunction() =
        name == SET_FUNCTION && containingClass?.qualifiedName == attributeScopeClassName

    private val attrsUtilsClassName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ATTRS_UTILS_SUFFIX

    private val attributeScopeClassName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ATTRIBUTE_SCOPE_SUFFIX

    private val attrsPackageName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ".attrs"

    private fun KtCallExpression.namespaceShortcutReplacement(namespace: String): String? {
        if (calleeExpression?.text != NAMESPACE_FUNCTION) return null
        if ((valueArgumentList?.arguments?.size ?: 0) != 1) return null

        val lambdaText = lambdaArguments.joinToString(" ") { it.text }.takeIf { it.isNotEmpty() }
        return listOfNotNull(namespace, lambdaText).joinToString(" ")
    }

    private fun String.attributeKey(): String? {
        val separator = indexOf(':')
        if (separator < 0) return takeIf { it.isNotEmpty() }

        val namespace = substring(0, separator)
        val name = substring(separator + 1)
        if (namespace.isEmpty() || name.isEmpty()) return null
        return name
    }

    private fun KtCallExpression.findAttributeRoot(): PsiElement? {
        var current: PsiElement = this
        var parent = current.parent
        while (parent != null) {
            if (parent is KtLambdaExpression && !parent.isNamespaceLambda()) {
                val callExpr = parent.findOwnerCall()
                val isAttributeBlock = parent.isAttrsArgument() || callExpr?.calleeExpression?.text == HIKAGE_ATTRIBUTE_FUNCTION
                if (isAttributeBlock) return parent
            }
            current = parent
            parent = current.parent
        }

        return null
    }

    private fun KtLambdaExpression.isNamespaceLambda() =
        findOwnerCall()?.namespaceFromBlockCall() != null

    private fun KtLambdaExpression.isAttrsArgument(): Boolean {
        var parent = parent
        while (parent != null) {
            if (parent is KtValueArgument)
                return parent.getArgumentName()?.asName?.identifier == "attrs"
            if (parent is KtCallExpression) return false
            parent = parent.parent
        }

        return false
    }

    private fun KtLambdaExpression.findOwnerCall(): KtCallExpression? {
        var parent = parent
        while (parent != null) {
            if (parent is KtCallExpression) return parent
            if (parent is KtLambdaExpression) return null
            parent = parent.parent
        }

        return null
    }

    private fun KtCallExpression.findAttributeNamespace(): String? {
        namespaceFromReceiver()?.let { return it }

        var current: PsiElement = this
        var parent = current.parent
        while (parent != null) {
            if (parent is KtCallExpression)
                parent.namespaceFromBlockCall()?.let { return it }
            current = parent
            parent = current.parent
        }

        return null
    }

    private fun KtCallExpression.namespaceFromReceiver(): String? {
        val qualified = parent as? KtQualifiedExpression ?: return null
        if (qualified.selectorExpression != this) return null

        return when (val receiver = qualified.receiverExpression) {
            is KtCallExpression -> receiver.namespaceFromCall()
            is KtNameReferenceExpression -> receiver.text.takeIf { it == ANDROID_NAMESPACE || it == APP_NAMESPACE }
            else -> null
        }
    }

    private fun KtCallExpression.namespaceFromBlockCall(): String? {
        if (lambdaArguments.isEmpty()) return null
        return namespaceFromCall()
    }

    private fun KtCallExpression.namespaceFromCall() = when (calleeExpression?.text) {
        NAMESPACE_FUNCTION -> firstStringLiteralText()
        ANDROID_NAMESPACE -> ANDROID_NAMESPACE
        APP_NAMESPACE -> APP_NAMESPACE
        else -> null
    }

    private fun KtCallExpression.firstStringLiteralText(): String? {
        val expression = valueArguments.firstOrNull()?.getArgumentExpression() as? KtStringTemplateExpression ?: return null
        val text = expression.text
        if (!text.startsWith("\"") || !text.endsWith("\"") || text.startsWith("\"\"\"")) return null
        if (text.contains('$')) return null

        return text.substring(1, text.length - 1)
    }
}