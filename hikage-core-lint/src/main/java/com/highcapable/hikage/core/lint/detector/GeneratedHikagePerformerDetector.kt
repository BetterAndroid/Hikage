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
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.PartialResult
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.highcapable.hikage.core.lint.DeclaredSymbol
import com.highcapable.hikage.core.lint.detector.entity.PerformerSymbol
import com.highcapable.hikage.core.lint.detector.extension.createKotlinOnlyUastHandler
import com.highcapable.hikage.core.lint.detector.extension.hasHikagable
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
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.toUElement
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

class GeneratedHikagePerformerDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "ReplaceWithGeneratedHikagePerformer",
            briefDescription = "Hikage generated performer function usage.",
            explanation = "Use the generated Hikage layout component function (Hikage Performer) like `TextView(...)` " +
                "instead of wrapping the same View with `View<TextView>(...)` or `ViewGroup<...>(...)`.",
            category = Category.USABILITY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                GeneratedHikagePerformerDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val VIEW_FUNCTION_PACKAGE_PREFIX = "com.highcapable.hikage.widget"

        private const val VIEW_CLASS_NAME = "android.view.View"
        private const val VIEW_GROUP_CLASS_NAME = "android.view.ViewGroup"

        private const val PARTIAL_PERFORMER_FUNCTIONS_KEY = "performerFunctions"
        private const val PARTIAL_FUNCTION_NAME_KEY = "functionName"
        private const val PARTIAL_PACKAGE_NAME_KEY = "packageName"

        private val excludedFunctionNames = setOf("View", "ViewGroup", "Layout", "LayoutParams")
        private val viewExpressionRegex = "(?:View|ViewGroup)<.*?>".toRegex()

        private const val GENERATED_KSP_DIR = "build/generated/ksp"
        private const val GENERATED_KSP_RESOURCES_DIR = "resources"
        private const val PERFORMER_SYMBOL_DIRECTORY_NAME = "META-INF/hikage/performer-symbol"
        private const val PERFORMER_SYMBOL_INDEX_FILE_NAME = "index.json"

        private val performerSymbolListType = object : TypeToken<List<PerformerSymbol>>() {}.type
        private val gson = Gson()

        private val declaredViews = ConcurrentHashMap<String, ViewCandidate>()
        private val projectViews = ConcurrentHashMap<Project, MutableMap<String, ViewCandidate>>()
    }

    override fun getApplicableUastTypes() = listOf(
        UImportStatement::class.java,
        UClass::class.java,
        UMethod::class.java,
        UCallExpression::class.java
    )

    override fun afterCheckEachProject(context: Context) {
        val performerFunctions = context.getPartialResults(ISSUE).map().getMap(PARTIAL_PERFORMER_FUNCTIONS_KEY) ?: LintMap()
        projectViews.remove(context.project).orEmpty().forEach { (viewClassName, view) ->
            val item = LintMap()
                .put(PARTIAL_FUNCTION_NAME_KEY, view.functionName)
                .put(PARTIAL_PACKAGE_NAME_KEY, view.functionPackageName)
            performerFunctions.put(viewClassName, item)
        }
        context.getPartialResults(ISSUE).map().put(PARTIAL_PERFORMER_FUNCTIONS_KEY, performerFunctions)
    }

    override fun checkPartialResults(context: Context, partialResults: PartialResult) = Unit

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        private val sourceViews = mutableMapOf<String, ViewCandidate>()
        private val importViews = mutableMapOf<String, ViewCandidate>()
        private val resolvedViews = mutableMapOf<String, ViewCandidate?>()
        private val partialViews by lazy { context.getPartialResults(ISSUE).toCandidates() }

        private val generatedViews by lazy {
            context.project.generatedPerformerSymbols()
                .associateBy { it.viewClassName }
        }

        private fun registerSourceView(view: ViewCandidate) {
            sourceViews[view.viewClassName] = view
            declaredViews[view.viewClassName] = view
            projectViews.getOrPut(context.project) { ConcurrentHashMap() }[view.viewClassName] = view
        }

        override fun visitImportStatement(node: UImportStatement) {
            node.asSourceString().toImportName()?.toCandidate()
                ?.let { importViews[it.viewClassName] = it }
        }

        override fun visitClass(node: UClass) {
            node.javaPsi.findSourceView()?.let(::registerSourceView)
        }

        override fun visitMethod(node: UMethod) {
            node.javaPsi.toSourceView()?.let(::registerSourceView)
        }

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(node, callExpr, method)
        }

        private fun startLint(node: UCallExpression, callExpr: KtCallExpression, method: PsiMethod) {
            val hasHikagable = method.hasHikagable()
            if (hasHikagable) visitAndReport(node, callExpr, method)
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
                ?: declaredViews[viewClassName]
                ?: partialViews[viewClassName]
                ?: generatedViews[viewClassName]
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
            val functionClass = context.evaluator.findClass(candidate.functionClassName)
            val generatedView = functionClass?.methods?.firstNotNullOfOrNull { it.toSourceView() }
            if (generatedView != null) return generatedView

            val hasFunction = functionClass?.methods?.any { it.name == functionName && it.hasHikagable() } == true

            return candidate.takeIf { hasFunction }
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

        private fun PsiMethod.toSourceView(): ViewCandidate? {
            if (!hasHikagable()) return null
            val name = name.takeIf { it !in excludedFunctionNames } ?: return null
            val returnType = returnType ?: return null
            val viewClass = context.evaluator.getTypeClass(returnType) ?: return null
            val viewClassName = viewClass.qualifiedName ?: return null
            if (!context.evaluator.extendsClass(viewClass, VIEW_CLASS_NAME, false)) return null

            val packageName = containingClass?.qualifiedName?.substringBeforeLast(".")
                ?.takeIf { it.isNotBlank() }
                ?: viewClassName.substringBeforeLast(".")

            return ViewCandidate(viewClassName, name, packageName)
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

        private fun Project.generatedPerformerSymbols(): Sequence<ViewCandidate> {
            val generatedResourceRoots = generatedResourceFolders.asSequence()
            val libraryGeneratedResourceRoots = allLibraries.asSequence().flatMap { it.generatedResourceFolders.asSequence() }
            val kspGeneratedResourceRoots = dir.resolve(GENERATED_KSP_DIR)
                .takeIf { it.exists() && it.isDirectory }
                ?.listFiles()
                ?.asSequence()
                ?.map { it.resolve(GENERATED_KSP_RESOURCES_DIR) }
                ?: emptySequence()
            val directorySymbols = sequenceOf(
                generatedResourceRoots,
                libraryGeneratedResourceRoots,
                kspGeneratedResourceRoots
            ).flatten()
                .filter { it.exists() && it.isDirectory }
                .flatMap { it.readPerformerSymbolIndexes() }
            val archiveSymbols = javaLibraries.asSequence()
                .filter { it.exists() && it.isFile }
                .flatMap { it.readPerformerSymbolIndexes() }

            return directorySymbols.plus(archiveSymbols).flatMap { it.toPerformerSymbols() }
        }

        private fun File.readPerformerSymbolIndexes(): Sequence<String> {
            if (isDirectory) return resolve(PERFORMER_SYMBOL_DIRECTORY_NAME)
                .takeIf { it.exists() && it.isDirectory }
                ?.walkTopDown()
                ?.filter { it.isFile && it.name == PERFORMER_SYMBOL_INDEX_FILE_NAME }
                ?.mapNotNull { runCatching { it.readText() }.getOrNull() }
                ?: emptySequence()

            return runCatching {
                JarFile(this).use { jar ->
                    jar.entries().asSequence()
                        .filter {
                            !it.isDirectory &&
                                it.name.startsWith(PERFORMER_SYMBOL_DIRECTORY_NAME) &&
                                it.name.endsWith("/$PERFORMER_SYMBOL_INDEX_FILE_NAME")
                        }
                        .mapNotNull { entry ->
                            runCatching {
                                jar.getInputStream(entry).bufferedReader().use { it.readText() }
                            }.getOrNull()
                        }
                        .toList()
                        .asSequence()
                }
            }.getOrElse { emptySequence() }
        }

        private fun String.toPerformerSymbols() = runCatching {
            gson.fromJson<List<PerformerSymbol>>(this, performerSymbolListType)
                .asSequence()
                .mapNotNull { it.toViewCandidate() }
        }.getOrElse { emptySequence() }

        private fun PerformerSymbol.toViewCandidate(): ViewCandidate? {
            val viewClass = viewClass?.takeIf { it.isNotBlank() } ?: return null
            val name = name?.takeIf { it.isNotBlank() } ?: return null
            val packageName = packageName?.takeIf { it.isNotBlank() } ?: return null

            return ViewCandidate(
                viewClassName = viewClass,
                functionName = name,
                actualFunctionPackageName = packageName
            )
        }
    })

    private fun Iterable<Map.Entry<Project, LintMap>>.toCandidates() = flatMap { (_, map) ->
        map.getMap(PARTIAL_PERFORMER_FUNCTIONS_KEY)?.keys()?.mapNotNull { viewClassName ->
            val functionName = map.getMap(PARTIAL_PERFORMER_FUNCTIONS_KEY)
                ?.getMap(viewClassName)
                ?.getString(PARTIAL_FUNCTION_NAME_KEY)
                ?: return@mapNotNull null
            val packageName = map.getMap(PARTIAL_PERFORMER_FUNCTIONS_KEY)
                ?.getMap(viewClassName)
                ?.getString(PARTIAL_PACKAGE_NAME_KEY)
                ?: return@mapNotNull null

            viewClassName to ViewCandidate(viewClassName, functionName, packageName)
        }.orEmpty().toList()
    }.toMap()

    private fun String.toCandidate(): ViewCandidate? {
        if (!startsWith("$VIEW_FUNCTION_PACKAGE_PREFIX.")) return null

        val viewClassName = removePrefix("$VIEW_FUNCTION_PACKAGE_PREFIX.")
        val functionName = viewClassName.substringAfterLast(".")

        return ViewCandidate(viewClassName, functionName)
    }

    private data class ViewCandidate(
        val viewClassName: String,
        val functionName: String,
        private val actualFunctionPackageName: String? = null
    ) {

        val functionPackageName = actualFunctionPackageName ?: "$VIEW_FUNCTION_PACKAGE_PREFIX.${viewClassName.substringBeforeLast(".")}"

        val functionClassName = "$functionPackageName.${functionName}Performer"

        val functionImportName = "$functionPackageName.$functionName"
    }
}