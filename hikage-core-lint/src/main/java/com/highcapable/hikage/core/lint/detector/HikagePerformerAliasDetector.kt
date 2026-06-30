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
 * This file is created by fankes on 2026/7/1.
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
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.highcapable.hikage.core.lint.detector.extension.createKotlinOnlyUastHandler
import com.highcapable.hikage.core.lint.detector.extension.hasHikagable
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UFile

class HikagePerformerAliasDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "RemoveHikagePerformerAlias",
            briefDescription = "Hikage performer import alias.",
            explanation = "Hikage generated performer functions should keep their original names when there is no same-named " +
                "function conflict. A same-named class import does not require aliasing the Hikage performer.",
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                HikagePerformerAliasDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val HIKAGE_WIDGET_FUNCTION_PACKAGE_PREFIX = "com.highcapable.hikage.widget."
    }

    private var activeContext: JavaContext? = null
    private val aliasImports = mutableMapOf<String, PerformerAliasImport>()
    private val conflictingFunctionNames = mutableSetOf<String>()
    private val aliasUsages = mutableMapOf<String, MutableList<PerformerAliasUsage>>()

    override fun getApplicableUastTypes() = listOf(
        UFile::class.java,
        UCallExpression::class.java
    )

    override fun beforeCheckFile(context: Context) {
        activeContext = context as? JavaContext
        aliasImports.clear()
        conflictingFunctionNames.clear()
        aliasUsages.clear()
    }

    override fun afterCheckFile(context: Context) {
        val javaContext = activeContext ?: return
        aliasUsages.forEach { (aliasName, usages) ->
            val aliasImport = aliasImports[aliasName] ?: return@forEach
            if (aliasImport.functionName in conflictingFunctionNames) return@forEach

            val message = aliasImport.message
            val lintFix = aliasImport.createRemoveAliasFix(javaContext, usages)

            javaContext.report(
                ISSUE,
                aliasImport.importDirective,
                javaContext.getLocation(aliasImport.importDirective),
                message,
                lintFix
            )
            usages.forEach { usage ->
                javaContext.report(
                    ISSUE,
                    usage.callExpression,
                    javaContext.getLocation(usage.calleeExpression),
                    message,
                    lintFix
                )
            }
        }
        activeContext = null
        aliasImports.clear()
        conflictingFunctionNames.clear()
        aliasUsages.clear()
    }

    override fun createUastHandler(context: JavaContext) = context.createKotlinOnlyUastHandler(object : UElementHandler() {

        override fun visitFile(node: UFile) {
            val ktFile = node.sourcePsi as? KtFile ?: return

            ktFile.importDirectives
                .mapNotNull { it.toPerformerAliasImport() }
                .forEach { aliasImports[it.aliasName] = it }

            ktFile.importDirectives
                .asSequence()
                .filter { it.aliasName == null }
                .mapNotNull { it.toConflictingFunctionName() }
                .filter { it in aliasImports.values.map(PerformerAliasImport::functionName) }
                .mapTo(conflictingFunctionNames) { it }

            ktFile.collectDescendantsOfType<KtNamedFunction>()
                .asSequence()
                .mapNotNull { it.name }
                .filter { it in aliasImports.values.map(PerformerAliasImport::functionName) }
                .mapTo(conflictingFunctionNames) { it }
        }

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val aliasImport = aliasImports[callExpr.calleeExpression?.text] ?: return

            if (aliasImport.functionName in conflictingFunctionNames) return
            if (node.resolve()?.hasHikagable() != true) return

            val calleeExpression = callExpr.calleeExpression ?: return
            aliasUsages.getOrPut(aliasImport.aliasName) { mutableListOf() }
                .add(PerformerAliasUsage(callExpr, calleeExpression))
        }

        private fun KtImportDirective.toPerformerAliasImport(): PerformerAliasImport? {
            val aliasName = aliasName ?: return null
            val importedFqName = importedFqName?.asString() ?: return null
            if (!importedFqName.startsWith(HIKAGE_WIDGET_FUNCTION_PACKAGE_PREFIX)) return null
            val functionName = importedFqName.substringAfterLast(".")
            if (aliasName == functionName) return null

            return PerformerAliasImport(
                importDirective = this,
                aliasName = aliasName,
                functionName = functionName,
                functionImportName = importedFqName
            )
        }

        private fun KtImportDirective.toConflictingFunctionName(): String? {
            val importedFqName = importedFqName?.asString() ?: return null
            if (importedFqName.endsWith(".*")) return null
            if (importedFqName.startsWith(HIKAGE_WIDGET_FUNCTION_PACKAGE_PREFIX)) return null
            if (!importedReference?.mainReference?.resolve().isFunction()) return null

            return importedFqName.substringAfterLast(".")
        }

        private fun Any?.isFunction() = this is PsiMethod || this is KtNamedFunction
    })

    private data class PerformerAliasImport(
        val importDirective: KtImportDirective,
        val aliasName: String,
        val functionName: String,
        val functionImportName: String
    ) {

        private val importText = importDirective.text
        private val importReplacement = "import $functionImportName"

        val message = "Use `$functionName` directly."

        private fun createRemoveAliasImportFix(context: JavaContext) = LintFix.create()
            .name("Remove alias '$aliasName'")
            .replace()
            .range(context.getLocation(importDirective))
            .text(importText)
            .with(importReplacement)
            .build()

        fun createRemoveAliasFix(context: JavaContext, usages: List<PerformerAliasUsage>): LintFix {
            val fixes = buildList {
                add(createRemoveAliasImportFix(context))
                usages.forEach { usage ->
                    add(
                        LintFix.create()
                            .replace()
                            .range(context.getLocation(usage.calleeExpression))
                            .text(aliasName)
                            .with(functionName)
                            .build()
                    )
                }
            }

            return LintFix.create()
                .name("Remove alias '$aliasName'")
                .composite(fixes)
        }
    }

    private data class PerformerAliasUsage(
        val callExpression: KtCallExpression,
        val calleeExpression: KtExpression
    )
}