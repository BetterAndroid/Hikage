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
import com.highcapable.hikage.core.lint.detector.extension.createKotlinOnlyUastHandler
import com.highcapable.hikage.core.lint.detector.extension.hasHikageable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.asJava.classes.KtLightClass
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.toUElement
import java.util.concurrent.ConcurrentHashMap

class HikageComponentsUsageDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "ReplaceWithHikageComponents",
            briefDescription = "Hikage component function usability.",
            explanation = "Use the generated Hikage component function like `TextView(...)` " +
                "instead of wrapping the same component with `View<TextView>(...)`.",
            category = Category.USABILITY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikageComponentsUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val VIEW_FUNCTION_PACKAGE_PREFIX = "com.highcapable.hikage.widget"

        private const val VIEW_METADATA_CLASS_SUFFIX = "HikageView"
        private const val VIEW_METADATA_FUNCTION_NAME = "FUNCTION_NAME"

        private const val VIEW_CLASS_NAME = "android.view.View"
        private const val VIEW_GROUP_CLASS_NAME = "android.view.ViewGroup"

        private val viewExpressionRegex = "(?:View|ViewGroup)<.*?>".toRegex()
        private val declaredComponents = ConcurrentHashMap<String, ViewCandidate>()
    }

    override fun getApplicableUastTypes() = listOf(
        UImportStatement::class.java,
        UClass::class.java,
        UCallExpression::class.java
    )

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        private val sourceViews = mutableMapOf<String, ViewCandidate>()
        private val importViews = mutableMapOf<String, ViewCandidate>()
        private val resolvedViews = mutableMapOf<String, ViewCandidate?>()

        override fun visitImportStatement(node: UImportStatement) {
            node.asSourceString().toImportName()?.toCandidate()
                ?.let { importViews[it.viewClassName] = it }
        }

        override fun visitClass(node: UClass) {
            node.javaPsi.findSourceView()?.let {
                sourceViews[it.viewClassName] = it
                declaredComponents[it.viewClassName] = it
            }
        }

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(node, callExpr, method)
        }

        private fun startLint(node: UCallExpression, callExpr: KtCallExpression, method: PsiMethod) {
            val hasHikageable = method.hasHikageable()
            if (hasHikageable) visitAndReport(node, callExpr, method)
        }

        private fun visitAndReport(node: UCallExpression, callExpr: KtCallExpression, method: PsiMethod) {
            val typeParameters = method.typeParameterList?.typeParameters ?: emptyArray()
            val typedViewFunctionIndex = typeParameters.indexOfFirst {
                it.extendsListTypes.any { type ->
                    type.canonicalText == VIEW_CLASS_NAME ||
                        type.canonicalText == VIEW_GROUP_CLASS_NAME
                }
            }

            val isTypedViewFunction = typedViewFunctionIndex >= 0
            if (!isTypedViewFunction) return

            val viewClassName = node.typeArguments.getOrNull(typedViewFunctionIndex)
                ?.let { context.evaluator.getTypeClass(it) }
                ?.qualifiedName ?: return

            val view = resolveView(viewClassName) ?: return

            val sourceLocation = context.getLocation(callExpr)
            val sourceText = callExpr.toUElement()?.asSourceString() ?: return
            val callExprElement = callExpr.toUElement() ?: return

            // Matches '>' and like `View<TextView`'s length + 1.
            val callExprLength = sourceText.split(">")[0].trim().length + 1
            val nameLocation = context.getRangeLocation(callExprElement, fromDelta = 0, callExprLength)

            // Only replace the first one, because there may be multiple sub-functions in DSL.
            val replacement = sourceText.replaceFirst(viewExpressionRegex, view.functionName)

            val lintFix = LintFix.create()
                .name("Replace with '${view.functionName}'")
                .replace()
                .range(sourceLocation)
                .with(replacement)
                .imports(view.functionImportName)
                .reformat(true)
                .build()

            val message = "Can be simplified to `${view.functionName}`."
            context.report(ISSUE, callExpr, nameLocation, message, lintFix)
        }

        private fun resolveView(viewClassName: String): ViewCandidate? {
            if (resolvedViews.containsKey(viewClassName)) return resolvedViews[viewClassName]

            val view = sourceViews[viewClassName]
                ?: importViews[viewClassName]
                ?: declaredComponents[viewClassName]
                ?: findGeneratedView(viewClassName)
            resolvedViews[viewClassName] = view

            return view
        }

        private fun findGeneratedView(viewClassName: String): ViewCandidate? {
            val viewClass = context.evaluator.findClass(viewClassName) ?: return null
            val sourceView = viewClass.findSourceView()
            if (sourceView != null) {
                sourceViews[viewClassName] = sourceView
                return sourceView
            }

            val functionName = viewClassName.substringAfterLast(".").replace(".", "_")
            val candidate = ViewCandidate(viewClassName, functionName)
            val metadataView = candidate.findMetadataView()
            if (metadataView != null) return metadataView

            val functionClass = context.evaluator.findClass(candidate.functionClassName)
            val hasFunction = functionClass?.methods?.any { it.name == functionName } == true

            return candidate.takeIf { hasFunction }
        }

        private fun ViewCandidate.findMetadataView(): ViewCandidate? {
            val metadataClass = context.evaluator.findClass(metadataClassName) ?: return null
            val functionName = metadataClass.fields.firstOrNull { it.name == VIEW_METADATA_FUNCTION_NAME }
                ?.computeConstantValue() as? String ?: return null

            return copy(functionName = functionName)
        }

        private fun PsiClass.findSourceView(): ViewCandidate? {
            val source = (this as? KtLightClass)?.kotlinOrigin ?: return null
            val hikageViewAnnotation = source.findAnnotation(DeclaredSymbol.HIKAGE_VIEW_ANNOTATION_CLASS)
            val hikageViewDeclarationAnnotation = source.findAnnotation(DeclaredSymbol.HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS)
            val viewClassName = hikageViewDeclarationAnnotation?.findClassName("view")
                ?: hikageViewAnnotation?.let { qualifiedName }
                ?: return null
            val functionName = (hikageViewAnnotation ?: hikageViewDeclarationAnnotation)
                ?.findStringValue("alias")
                .orEmpty()
                .takeIf { it.isNotBlank() }
                ?: viewClassName.substringAfterLast(".").replace(".", "_")

            return ViewCandidate(viewClassName, functionName)
        }

        private fun KtClassOrObject.findAnnotation(className: String) = annotationEntries.firstOrNull {
            it.typeReference?.mainReference?.resolve()?.let { element ->
                (element as? PsiClass)?.qualifiedName == className
            } == true || it.shortName?.asString() == className.substringAfterLast(".")
        }

        private fun KtAnnotationEntry.findClassName(name: String) = valueArguments.firstOrNull {
            it.getArgumentName()?.asName?.identifier == name
        }?.getArgumentExpression()?.children?.filterIsInstance<KtNameReferenceExpression>()?.lastOrNull()
            ?.resolvedClassName()
            ?: valueArguments.getOrNull(if (name == "view") 0 else -1)
                ?.getArgumentExpression()?.children?.filterIsInstance<KtNameReferenceExpression>()?.lastOrNull()
                ?.resolvedClassName()

        private fun KtAnnotationEntry.findStringValue(name: String) = valueArguments.firstOrNull {
            it.getArgumentName()?.asName?.identifier == name
        }?.getArgumentExpression()?.text?.removeSurrounding("\"")

        private fun KtNameReferenceExpression.resolvedClassName() = (mainReference.resolve() as? PsiClass)?.qualifiedName

        private fun String.toImportName() = removePrefix("import")
            .substringBefore("//")
            .substringBefore("/*")
            .substringBefore(" as ")
            .trim()
            .takeIf { it.isNotBlank() && !it.endsWith(".*") }
    })

    private fun String.toCandidate(): ViewCandidate? {
        if (!startsWith("$VIEW_FUNCTION_PACKAGE_PREFIX.")) return null

        val viewClassName = removePrefix("$VIEW_FUNCTION_PACKAGE_PREFIX.")
        val functionName = viewClassName.substringAfterLast(".")

        return ViewCandidate(viewClassName, functionName)
    }

    private data class ViewCandidate(val viewClassName: String, val functionName: String) {

        val functionPackageName = "$VIEW_FUNCTION_PACKAGE_PREFIX.${viewClassName.substringBeforeLast(".")}"

        val functionClassName = "$functionPackageName.${functionName}Performer"

        val metadataClassName = "$functionPackageName._${viewClassName.substringAfterLast(".").replace(".", "_")}$VIEW_METADATA_CLASS_SUFFIX"

        val functionImportName = "$functionPackageName.$functionName"
    }
}