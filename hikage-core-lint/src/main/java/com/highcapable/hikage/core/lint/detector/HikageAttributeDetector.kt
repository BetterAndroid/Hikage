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
package com.highcapable.hikage.core.lint.detector

import com.android.resources.ResourceType
import com.android.tools.lint.client.api.ResourceRepositoryScope
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
import com.highcapable.hikage.core.lint.detector.extension.hasHikagable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtQualifiedExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.toUElementOfType
import java.io.File
import javax.lang.model.SourceVersion

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

        val INEFFECTIVE_LAYOUT_ATTRIBUTE_ISSUE = Issue.create(
            id = "IneffectiveHikageLayoutAttribute",
            briefDescription = "Hikage layout attribute ineffective.",
            explanation = "Attributes with the `layout_` prefix have no effect when `lparams` is specified in the same view declaration.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val CREATE_ID_ISSUE = Issue.create(
            id = "CreateIdInHikageAttribute",
            briefDescription = "Hikage attribute creates ID resource.",
            explanation = "Hikage attributes are resolved at runtime and cannot create new ID resources. " +
                "Declare the ID in `ids.xml` and reference it with `@id/name`.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val MISSING_ID_ISSUE = Issue.create(
            id = "MissingIdInHikageAttribute",
            briefDescription = "Hikage attribute ID resource missing.",
            explanation = "ID resources used in Hikage attributes must be declared in the current project " +
                "or provided by one of its dependencies.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val INVALID_NAME_ISSUE = Issue.create(
            id = "InvalidHikageAttributeName",
            briefDescription = "Hikage attribute name invalid.",
            explanation = "Attribute names must use a valid namespace prefix and local name.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val INVALID_RESOURCE_REFERENCE_ISSUE = Issue.create(
            id = "InvalidHikageAttributeResourceReference",
            briefDescription = "Hikage attribute resource reference invalid.",
            explanation = "Resource references must use the same resource reference format as XML.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val INVALID_COLOR_VALUE_ISSUE = Issue.create(
            id = "InvalidHikageAttributeColorValue",
            briefDescription = "Hikage attribute color value invalid.",
            explanation = "Color values must be #RGB, #ARGB, #RRGGBB or #AARRGGBB.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val TOO_LONG_STRING_ISSUE = Issue.create(
            id = "TooLongHikageAttributeString",
            briefDescription = "Hikage attribute string too long.",
            explanation = "Attribute strings must fit in the binary XML string pool.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                HikageAttributeDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val NAMESPACE_FUNCTION = "namespace"
        private const val SET_FUNCTION = "set"
        private const val HIKAGE_ATTRIBUTE_FUNCTION = "HikageAttribute"
        private const val ATTRS_ARGUMENT = "attrs"
        private const val LPARAMS_ARGUMENT = "lparams"
        private const val LAYOUT_ATTRIBUTE_PREFIX = "layout_"
        private const val ATTRIBUTE_STRING_MAX_LENGTH = 0x7FFF
        private const val ANDROID_NAMESPACE = "android"
        private const val APP_NAMESPACE = "app"
        private const val ID_RESOURCE_TYPE = "id"
        private const val ATTR_RESOURCE_TYPE = "attr"
        private const val IDS_XML_FILE = "ids.xml"
        private const val ATTRIBUTE_UTILS_SUFFIX = ".attribute.HikageAttributeUtils"
        private const val ATTRIBUTE_SCOPE_SUFFIX = ".attribute.AttributeScope"
        private const val HIKAGE_CLASS_SUFFIX = ".Hikage"

        private val COLOR_VALUE_REGEX = "^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$".toRegex()
    }

    override fun getApplicableUastTypes() = listOf(UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        private val attributes = hashMapOf<PsiElement, MutableMap<String, MutableList<AttributeUsage>>>()
        private val reportedLayoutAttributes = hashSetOf<PsiElement>()

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(context, node, callExpr, method, attributes, reportedLayoutAttributes)
        }
    })

    private fun startLint(
        context: JavaContext,
        node: UCallExpression,
        callExpr: KtCallExpression,
        method: PsiMethod,
        attributes: MutableMap<PsiElement, MutableMap<String, MutableList<AttributeUsage>>>,
        reportedLayoutAttributes: MutableSet<PsiElement>
    ) {
        val isHikageSet = method.isHikageRootSetFunction() || method.isHikageScopeSetFunction()
        if (isHikageSet && visitAndReportInvalidAttribute(context, node, callExpr)) return

        when {
            method.isHikageNamespaceFunction() -> {
                visitAndReportNamespaceShortcut(context, callExpr)
                visitAndReportTooLongNamespace(context, node, callExpr)
            }
            method.isHikageRootSetFunction() -> visitAndReportRootSet(context, node, callExpr)
            method.isHikageScopeSetFunction() -> visitAndReportScopedSet(context, node, callExpr)
        }

        if (isHikageSet) {
            visitAndReportIdResource(context, callExpr)
            visitAndReportDuplicate(context, node, callExpr, attributes)
        }

        if (method.hasHikagable()) visitAndReportIneffectiveLayoutAttributes(context, callExpr, method, reportedLayoutAttributes)
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
            .imports("$attributePackageName.$namespace")
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

    private fun visitAndReportTooLongNamespace(context: JavaContext, node: UCallExpression, callExpr: KtCallExpression) {
        val namespace = callExpr.firstStringLiteralText() ?: return
        if (namespace.length <= ATTRIBUTE_STRING_MAX_LENGTH) return

        val namespaceExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return
        val location = context.getLocation(namespaceExpr)
        context.report(
            TOO_LONG_STRING_ISSUE,
            namespaceExpr,
            location,
            message = "Attribute string is too long. Maximum length is $ATTRIBUTE_STRING_MAX_LENGTH characters."
        )
    }

    private fun visitAndReportInvalidAttribute(context: JavaContext, node: UCallExpression, callExpr: KtCallExpression): Boolean {
        var hasError = false
        val attrNameExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return false

        val attrName = callExpr.stringLiteralTextAt(0)
        if (attrName != null) {
            val location = context.getLocation(attrNameExpr)
            attrName.invalidAttributeNameMessage()?.let {
                hasError = true
                context.report(
                    INVALID_NAME_ISSUE,
                    attrNameExpr,
                    location,
                    message = it
                )
            }
            attrName.attributeNameString().takeIf { it.length > ATTRIBUTE_STRING_MAX_LENGTH }?.let {
                hasError = true
                context.report(
                    TOO_LONG_STRING_ISSUE,
                    attrNameExpr,
                    location,
                    message = "Attribute string is too long. Maximum length is $ATTRIBUTE_STRING_MAX_LENGTH characters."
                )
            }
        }

        val valueExpr = callExpr.valueArguments.getOrNull(1)?.getArgumentExpression() ?: return hasError
        val value = valueExpr.staticStringText() ?: return hasError
        val valueLocation = context.getLocation(valueExpr)

        value.invalidResourceReferenceMessage()?.let {
            hasError = true
            context.report(
                INVALID_RESOURCE_REFERENCE_ISSUE,
                valueExpr,
                valueLocation,
                message = it
            )
        }
        value.invalidColorValueMessage()?.let {
            hasError = true
            context.report(
                INVALID_COLOR_VALUE_ISSUE,
                valueExpr,
                valueLocation,
                message = it
            )
        }
        if (value.length > ATTRIBUTE_STRING_MAX_LENGTH) {
            hasError = true
            context.report(
                TOO_LONG_STRING_ISSUE,
                valueExpr,
                valueLocation,
                message = "Attribute string is too long. Maximum length is $ATTRIBUTE_STRING_MAX_LENGTH characters."
            )
        }

        return hasError
    }

    private fun visitAndReportIdResource(context: JavaContext, callExpr: KtCallExpression) {
        val valueExpr = callExpr.valueArguments.getOrNull(1)?.getArgumentExpression() ?: return
        val value = valueExpr.staticStringText() ?: return
        val idReference = value.idResourceReference() ?: return

        val idName = idReference.name
        val idExists = context.hasIdResource(idName)
        if (!idReference.createsId && idExists) return

        val location = context.getLocation(valueExpr)
        val issue = if (idReference.createsId) CREATE_ID_ISSUE else MISSING_ID_ISSUE
        val message = if (idReference.createsId) 
            "Resource ID `$idName` cannot be created from Hikage attributes at runtime."
        else "Resource ID `$idName` does not exist."

        context.report(
            issue,
            valueExpr,
            location,
            message = message,
            quickfixData = valueExpr.createIdResourceFix(context, idName, idReference.createsId, idExists)
        )
    }

    private fun visitAndReportIneffectiveLayoutAttributes(
        context: JavaContext,
        callExpr: KtCallExpression,
        method: PsiMethod,
        reportedLayoutAttributes: MutableSet<PsiElement>
    ) {
        val lparamsArg = callExpr.findArgument(method, LPARAMS_ARGUMENT) ?: return
        val lparamsExpr = lparamsArg.getArgumentExpression() ?: return
        if (lparamsExpr.text == "null") return

        val attrsArg = callExpr.findArgument(method, ATTRS_ARGUMENT) ?: return
        val attrsExpr = attrsArg.getArgumentExpression() ?: return
        val inlineLambda = attrsExpr as? KtLambdaExpression
        if (inlineLambda != null) {
            context.reportIneffectiveLayoutAttributes(inlineLambda.collectLayoutAttributeReports(), reportedLayoutAttributes)
            return
        }

        val reusableLambda = attrsExpr.resolveAttributeLambda() ?: return
        val reports = reusableLambda.collectLayoutAttributeReports()
        if (reports.isEmpty()) return

        val location = context.getLocation(attrsExpr)
        context.report(
            INEFFECTIVE_LAYOUT_ATTRIBUTE_ISSUE,
            attrsExpr,
            location,
            message = "Attributes in `${attrsExpr.text}` with the `layout_` prefix have no effect because `lparams` is specified."
        )
        context.reportIneffectiveLayoutAttributes(reports, reportedLayoutAttributes)
    }

    private fun visitAndReportDuplicate(
        context: JavaContext,
        node: UCallExpression,
        callExpr: KtCallExpression,
        attributes: MutableMap<PsiElement, MutableMap<String, MutableList<AttributeUsage>>>
    ) {
        val attrName = callExpr.firstStringLiteralText() ?: return
        val attrKey = attrName.attributeKey() ?: return
        val attrNameExpr = node.valueArguments.firstOrNull()?.sourcePsi ?: return
        val root = callExpr.findAttributeRoot() ?: return
        val usages = attributes.getOrPut(root) { hashMapOf() }.getOrPut(attrKey) { mutableListOf() }
        val exists = usages.firstOrNull { it.callExpr.canCoexistInExecutionPath(callExpr) }
        usages.add(AttributeUsage(callExpr))
        if (exists == null) return

        val location = context.getLocation(attrNameExpr)
        context.report(
            DUPLICATE_ISSUE,
            attrNameExpr,
            location,
            message = "Attribute `$attrKey` is duplicated in the same attribute scope."
        )
    }

    private fun JavaContext.reportIneffectiveLayoutAttributes(
        reports: List<LayoutAttributeReport>,
        reportedLayoutAttributes: MutableSet<PsiElement>
    ) {
        reports.forEach {
            if (!reportedLayoutAttributes.add(it.element)) return@forEach

            val location = getLocation(it.element)
            report(
                INEFFECTIVE_LAYOUT_ATTRIBUTE_ISSUE,
                it.element,
                location,
                message = "Attribute `${it.name}` has no effect because `lparams` is specified."
            )
        }
    }

    private fun PsiMethod.isHikageNamespaceFunction() =
        name == NAMESPACE_FUNCTION && containingClass?.qualifiedName == attributeUtilsClassName

    private fun PsiMethod.isHikageRootSetFunction() =
        name == SET_FUNCTION && containingClass?.qualifiedName == attributeUtilsClassName

    private fun PsiMethod.isHikageScopeSetFunction() =
        name == SET_FUNCTION && containingClass?.qualifiedName == attributeScopeClassName

    private val attributeUtilsClassName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ATTRIBUTE_UTILS_SUFFIX

    private val attributeScopeClassName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ATTRIBUTE_SCOPE_SUFFIX

    private val attributePackageName
        get() = DeclaredSymbol.HIKAGE_CLASS.removeSuffix(HIKAGE_CLASS_SUFFIX) + ".attribute"

    private fun KtCallExpression.findArgument(method: PsiMethod, name: String): KtValueArgument? {
        val parameters = method.parameterList.parameters
        val arguments = valueArgumentList?.arguments ?: emptyList()
        val namedArg = arguments.firstOrNull { it.getArgumentName()?.asName?.identifier == name }
        if (namedArg != null) return namedArg

        return arguments
            .takeWhile { it.getArgumentName() == null }
            .withIndex()
            .firstOrNull { (index, _) -> parameters.getOrNull(index)?.name == name }
            ?.value
    }

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

    private tailrec fun KtExpression.resolveAttributeLambda(visited: MutableSet<PsiElement> = hashSetOf()): KtLambdaExpression? {
        if (!visited.add(this)) return null

        return when (this) {
            is KtLambdaExpression -> this
            is KtCallExpression -> attributeLambda()
            is KtNameReferenceExpression -> {
                val property = references.firstOrNull()?.resolve() as? KtProperty ?: return null
                if (!visited.add(property)) return null
                property.initializer?.resolveAttributeLambda(visited)
            }
            else -> null
        }
    }

    private fun KtCallExpression.attributeLambda(): KtLambdaExpression? {
        if (calleeExpression?.text != HIKAGE_ATTRIBUTE_FUNCTION) return null
        return lambdaArguments.lastOrNull()?.getLambdaExpression()
    }

    private fun KtLambdaExpression.collectLayoutAttributeReports(): List<LayoutAttributeReport> {
        val reports = mutableListOf<LayoutAttributeReport>()

        fun visit(element: PsiElement) {
            val callExpr = element as? KtCallExpression
            val method = callExpr?.toUElementOfType<UCallExpression>()?.resolve()
            val isHikageSet = method?.isHikageRootSetFunction() == true || method?.isHikageScopeSetFunction() == true
            val attrName = callExpr?.takeIf { isHikageSet }?.firstStringLiteralText()
            val attrKey = attrName?.attributeKey()
            if (attrKey?.startsWith(LAYOUT_ATTRIBUTE_PREFIX) == true) {
                val attrNameExpr = callExpr.valueArguments.firstOrNull()?.getArgumentExpression()
                if (attrNameExpr != null)
                    reports.add(LayoutAttributeReport(attrKey, attrNameExpr))
            }

            element.children.forEach { visit(it) }
        }

        visit(this)
        return reports
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

    private fun KtCallExpression.firstStringLiteralText() = stringLiteralTextAt(0)
    private fun KtCallExpression.stringLiteralTextAt(index: Int) = valueArguments.getOrNull(index)?.getArgumentExpression()?.staticStringText()

    private fun KtExpression.staticStringText(visited: MutableSet<PsiElement> = hashSetOf()): String? {
        if (!visited.add(this)) return null

        if (this is KtStringTemplateExpression) {
            val text = text
            if (!text.startsWith("\"") || !text.endsWith("\"") || text.startsWith("\"\"\"")) return null
            if (text.contains('$')) return null

            return text.substring(1, text.length - 1)
        }

        if (this is KtQualifiedExpression) {
            val receiverText = receiverExpression.staticStringText(visited) ?: return null
            val selector = selectorExpression as? KtCallExpression ?: return null
            if (selector.calleeExpression?.text != "repeat") return null
            val repeatCount = selector.valueArguments.singleOrNull()
                ?.getArgumentExpression()
                ?.text
                ?.parseIntLiteralOrNull()
                ?: return null

            if (repeatCount < 0) return null
            if (receiverText.isEmpty()) return ""

            val cappedCount = minOf(repeatCount, ATTRIBUTE_STRING_MAX_LENGTH / receiverText.length + 1)
            return receiverText.repeat(cappedCount)
        }

        if (this is KtNameReferenceExpression) {
            val property = references.firstOrNull()?.resolve() as? KtProperty ?: return null
            if (!visited.add(property)) return null
            return property.initializer?.staticStringText(visited)
        }

        return null
    }

    private fun String.attributeNameString(): String {
        val separator = indexOf(':')
        return if (separator in 0..<lastIndex) substring(separator + 1) else this
    }

    private fun String.invalidAttributeNameMessage(): String? {
        if (isEmpty()) return "Attribute name must not be empty."

        val separator = indexOf(':')
        if (separator < 0) return invalidResourceNameMessage("Attribute name")
        return when {
            separator == 0 -> "Attribute `$this` is missing a namespace before `:`."
            separator == lastIndex -> "Attribute `$this` is missing a name after `:`."
            indexOf(':', separator + 1) >= 0 -> "Attribute `$this` must not contain more than one `:`."
            !substring(0, separator).isValidResourceNamespace() -> "Attribute `$this` has an invalid namespace prefix."
            else -> substring(separator + 1).invalidResourceNameMessage("Attribute name")
        }
    }

    private fun String.invalidResourceReferenceMessage(): String? {
        if (startsWith("@")) return invalidResourceValueReferenceMessage()
        if (startsWith("?")) return invalidAttributeValueReferenceMessage()
        return null
    }

    private fun String.invalidResourceValueReferenceMessage(): String? {
        if (this == "@null") return null

        var body = removePrefix("@")
        if (body.isEmpty()) return "Resource reference `$this` is missing a resource type and name."

        val createsResource = body.startsWith("+")
        if (createsResource) body = body.drop(1)
        if (body.isEmpty()) return "Resource reference `$this` is missing a resource type and name."

        val reference = body.resourceReferenceBody(displayValue = this, requireType = true)
        reference.message?.let { return it }

        return when {
            createsResource && reference.type != ID_RESOURCE_TYPE -> "Resource reference `$this` can only create ID resources."
            else -> null
        }
    }

    private fun String.invalidAttributeValueReferenceMessage(): String? {
        val body = removePrefix("?")
        if (body.isEmpty()) return "Attribute reference `$this` is missing an attribute name."

        val reference = body.resourceReferenceBody(displayValue = this, requireType = false)
        reference.message?.let { return it.replace("Resource reference", "Attribute reference") }

        return when {
            reference.type != null && reference.type != ATTR_RESOURCE_TYPE ->
                "Attribute reference `$this` must use the attr resource type."
            else -> null
        }
    }

    private fun String.resourceReferenceBody(displayValue: String, requireType: Boolean): ResourceReferenceBody {
        var body = this
        val colon = body.indexOf(':')
        if (colon >= 0) {
            if (colon == 0) return ResourceReferenceBody(message = "Resource reference `$displayValue` is missing a package name before `:`.")
            if (colon == body.lastIndex)
                return ResourceReferenceBody(message = "Resource reference `$displayValue` is missing a resource type and name after `:`.")
            if (body.indexOf(':', colon + 1) >= 0)
                return ResourceReferenceBody(message = "Resource reference `$displayValue` must not contain more than one `:`.")

            val packageName = body.substring(0, colon)
            if (!packageName.isValidResourceNamespace())
                return ResourceReferenceBody(message = "Resource reference `$displayValue` has an invalid package name.")

            body = body.substring(colon + 1)
        }

        val slash = body.indexOf('/')
        val type: String?
        val name: String
        if (slash < 0) {
            if (requireType) return ResourceReferenceBody(
                message = "Resource reference `$displayValue` must include a resource type, for example `@string/name`."
            )

            type = null
            name = body
        } else {
            if (slash == 0) return ResourceReferenceBody(message = "Resource reference `$displayValue` is missing a resource type before `/`.")
            if (slash == body.lastIndex)
                return ResourceReferenceBody(message = "Resource reference `$displayValue` is missing a resource name after `/`.")
            if (body.indexOf('/', slash + 1) >= 0)
                return ResourceReferenceBody(message = "Resource reference `$displayValue` must not contain more than one `/`.")

            type = body.substring(0, slash)
            name = body.substring(slash + 1)
        }

        if (type != null && !type.isValidResourceTypeName())
            return ResourceReferenceBody(type, name, "Resource reference `$displayValue` has an invalid resource type.")
        name.invalidResourceNameMessage("Resource reference name")?.let {
            return ResourceReferenceBody(type, name, it.replace("Resource reference name", "Resource reference `$displayValue` name"))
        }

        return ResourceReferenceBody(type, name)
    }

    private fun String.invalidColorValueMessage(): String? {
        if (!startsWith("#") || COLOR_VALUE_REGEX.matches(this)) return null
        return "Color value `$this` must be #RGB, #ARGB, #RRGGBB or #AARRGGBB."
    }

    private fun String.invalidResourceNameMessage(label: String): String? {
        val normalized = replace('.', '_')
        return when {
            isEmpty() -> "$label must not be empty."
            startsWith(".") -> "$label `$this` must not start with `.`."
            !SourceVersion.isIdentifier(normalized) -> "$label `$this` is not a valid resource name."
            SourceVersion.isKeyword(normalized) -> "$label `$this` must not be a reserved Java keyword."
            else -> null
        }
    }

    private fun String.isValidResourceName() = invalidResourceNameMessage("Resource name") == null
    private fun String.isValidResourceTypeName() = isValidResourceName()
    private fun String.isValidResourceNamespace() = split('.').all { it.isValidResourceName() }

    private fun String.idResourceReference(): IdResourceReference? {
        val createsId = startsWith("@+$ID_RESOURCE_TYPE/")
        val referencesId = startsWith("@$ID_RESOURCE_TYPE/")
        if (!createsId && !referencesId) return null

        val prefix = if (createsId) "@+$ID_RESOURCE_TYPE/" else "@$ID_RESOURCE_TYPE/"
        val idName = removePrefix(prefix)
        if (idName.isEmpty()) return null

        return IdResourceReference(idName, createsId)
    }

    private fun KtExpression.createIdResourceFix(
        context: JavaContext,
        idName: String,
        createsId: Boolean,
        idExists: Boolean
    ): LintFix? {
        if (this !is KtStringTemplateExpression) return null
        if (!idName.isValidResourceName()) return null

        val replaceFix = if (createsId) createReplaceCreatedIdFix(context, idName) else null
        if (idExists) return replaceFix

        val idsXml = context.findIdsXml()
        val idFix = if (idsXml.exists()) idsXml.createInsertIdFix(idName) else idsXml.createIdsXmlFix(idName)
        if (replaceFix == null) return idFix

        return LintFix.create()
            .name("Declare '$idName' in $IDS_XML_FILE")
            .composite(idFix, replaceFix)
    }

    private fun KtExpression.createReplaceCreatedIdFix(context: JavaContext, idName: String) = LintFix.create()
        .name("Replace with '@$ID_RESOURCE_TYPE/$idName'")
        .replace()
        .range(context.getLocation(this))
        .with("\"@$ID_RESOURCE_TYPE/$idName\"")
        .reformat(true)
        .build()

    private fun JavaContext.findIdsXml(): File {
        val resourceFolders = project.resourceFolders
        val resDir = resourceFolders.firstOrNull { File(File(it, "values"), IDS_XML_FILE).exists() }
            ?: resourceFolders.firstOrNull { it.name == "res" && "/src/main/" in it.invariantSeparatorsPath }
            ?: resourceFolders.firstOrNull { it.name == "res" && "/build/" !in it.invariantSeparatorsPath }
            ?: resourceFolders.firstOrNull { it.name == "res" }
            ?: File(project.dir, "src/main/res")

        return File(File(resDir, "values"), IDS_XML_FILE)
    }

    private fun JavaContext.hasIdResource(idName: String) =
        runCatching {
            client.getResources(project, ResourceRepositoryScope.ALL_DEPENDENCIES)
                .hasResources(project.resourceNamespace, ResourceType.ID, idName)
        }.getOrDefault(false)

    private fun File.createIdsXmlFix(idName: String) = LintFix.create()
        .name("Create $IDS_XML_FILE")
        .newFile(this, """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <item name="$idName" type="$ID_RESOURCE_TYPE" />
            </resources>
        """.trimIndent())
        .reformat(true)
        .build()

    private fun File.createInsertIdFix(idName: String) = LintFix.create()
        .name("Declare '$idName' in $IDS_XML_FILE")
        .replace()
        .range(Location.create(this))
        .pattern("</resources>")
        .with("    <item name=\"$idName\" type=\"$ID_RESOURCE_TYPE\" />\n</resources>")
        .reformat(true)
        .build()

    private fun String.parseIntLiteralOrNull(): Int? {
        val value = replace("_", "")
        return when {
            value.startsWith("0x", ignoreCase = true) -> value.substring(2).toIntOrNull(16)
            else -> value.toIntOrNull()
        }
    }

    private fun KtCallExpression.canCoexistInExecutionPath(other: KtCallExpression): Boolean {
        val otherAncestors = other.ancestorsWithSelf().toList()
        return ancestorsWithSelf()
            .filter { ancestor -> otherAncestors.any { it === ancestor } }
            .none { it.hasMutuallyExclusiveBranches(this, other) }
    }

    private fun PsiElement.hasMutuallyExclusiveBranches(first: PsiElement, second: PsiElement) = when (this) {
        is KtIfExpression -> {
            val firstBranch = branchContaining(first)
            val secondBranch = branchContaining(second)
            firstBranch != null && secondBranch != null && firstBranch != secondBranch
        }
        is KtWhenExpression -> {
            val firstBranch = entries.firstOrNull { it.isSelfOrAncestorOf(first) }
            val secondBranch = entries.firstOrNull { it.isSelfOrAncestorOf(second) }
            firstBranch != null && secondBranch != null && firstBranch != secondBranch
        }
        is KtBinaryExpression -> {
            operationToken == KtTokens.ELVIS && run {
                val firstBranch = elvisBranchContaining(first)
                val secondBranch = elvisBranchContaining(second)
                firstBranch != null && secondBranch != null && firstBranch != secondBranch
            }
        }
        else -> false
    }

    private fun KtIfExpression.branchContaining(element: PsiElement): KtExpression? {
        then?.takeIf { it.isSelfOrAncestorOf(element) }?.let { return it }
        `else`?.takeIf { it.isSelfOrAncestorOf(element) }?.let { return it }
        return null
    }

    private fun KtBinaryExpression.elvisBranchContaining(element: PsiElement): KtExpression? {
        left?.takeIf { it.isSelfOrAncestorOf(element) }?.let { return it }
        right?.takeIf { it.isSelfOrAncestorOf(element) }?.let { return it }
        return null
    }

    private fun PsiElement.ancestorsWithSelf() = generateSequence(this) { it.parent }

    private fun PsiElement.isSelfOrAncestorOf(element: PsiElement) =
        element.ancestorsWithSelf().any { it === this }

    private data class AttributeUsage(
        val callExpr: KtCallExpression
    )

    private data class LayoutAttributeReport(
        val name: String,
        val element: PsiElement
    )

    private data class ResourceReferenceBody(
        val type: String? = null,
        val name: String = "",
        val message: String? = null
    )

    private data class IdResourceReference(
        val name: String,
        val createsId: Boolean
    )
}