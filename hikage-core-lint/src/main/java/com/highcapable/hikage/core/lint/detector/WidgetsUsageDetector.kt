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
import com.highcapable.hikage.core.lint.detector.extension.hasHikageable
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UImportStatement
import org.jetbrains.uast.toUElement

class WidgetsUsageDetector : Detector(), Detector.UastScanner {

    companion object {

        val ISSUE = Issue.create(
            id = "ReplaceWithHikageWidgets",
            briefDescription = "Hikage built-in widget usability.",
            explanation = "Use the built-in widget function component provided by Hikage like `TextView(...)` " +
                "without using a form like `View<TextView>(...)` to use the component.",
            category = Category.USABILITY,
            priority = 5,
            severity = Severity.WARNING,
            implementation = Implementation(
                WidgetsUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        private const val BUILT_IN_WIDGETS_PACKAGE_PREFIX = "android.widget"
        private const val WIDGET_FUNCTION_PREFIX = "com.highcapable.hikage.widget.$BUILT_IN_WIDGETS_PACKAGE_PREFIX"
        private const val VIEW_CLASS_NAME = "android.view.View"
        private const val VIEW_GROUP_CLASS_NAME = "android.view.ViewGroup"

        private val viewExpressionRegex = "(?:View|ViewGroup)<.*?>".toRegex()

        private val builtInWidgets = listOf(
            "SeekBar",
            "LinearLayout",
            "ScrollView",
            "TextView",
            "EditText",
            "AutoCompleteTextView",
            "ExpandableListView",
            "ListView",
            "RatingBar",
            "ViewSwitcher",
            "ActionMenuView",
            "ImageView",
            "ViewAnimator",
            "HorizontalScrollView",
            "MediaController",
            "RelativeLayout",
            "TextClock",
            "CalendarView",
            "ToggleButton",
            "RadioGroup",
            "VideoView",
            "GridView",
            "QuickContactBadge",
            "TableLayout",
            "NumberPicker",
            "Toolbar",
            "ViewFlipper",
            "Chronometer",
            "ImageSwitcher",
            "Button",
            "CheckBox",
            "TabWidget",
            "TabHost",
            "SearchView",
            "Spinner",
            "TimePicker",
            "ImageButton",
            "TextSwitcher",
            "DatePicker",
            "RadioButton",
            "CheckedTextView",
            "FrameLayout",
            "Space",
            "GridLayout",
            "Switch",
            "ProgressBar",
            "TableRow",
            "SurfaceView",
            "WebView"
        )
    }

    data class ImportReference(val packagePrefix: String, val name: String, val alias: String? = null)

    override fun getApplicableUastTypes(): List<Class<out UElement>> = listOf(UImportStatement::class.java, UCallExpression::class.java)

    override fun createUastHandler(context: JavaContext) = object : UElementHandler() {

        private val importReferences = mutableSetOf<ImportReference>()

        override fun visitImportStatement(node: UImportStatement) {
            val imported = node.asSourceString().replace("import", "").let {
                when {
                    it.contains("//") -> it.split("//")[0]
                    it.contains("/*") -> it.split("/*")[0]
                    else -> it
                }
            }.trim()
            val importRefs = imported.split(" as ")
            val alias = if (importRefs.size > 1)
                importRefs.getOrNull(1)?.trim()
            else null
            val importPrefix = importRefs[0]

            val hasPrefix = importPrefix.contains(".")

            val name = if (hasPrefix)
                importPrefix.split(".").last()
            else importPrefix

            val packagePrefix = importPrefix.replace(if (hasPrefix) ".$name" else name, "")
            val reference = ImportReference(packagePrefix, name, alias)

            importReferences.add(reference)
        }

        override fun visitCallExpression(node: UCallExpression) {
            val callExpr = node.sourcePsi as? KtCallExpression ?: return
            val method = node.resolve() ?: return

            startLint(callExpr, method)
        }

        private fun startLint(callExpr: KtCallExpression, method: PsiMethod) {
            val hasHikageable = method.hasHikageable()
            if (hasHikageable) visitAndReport(callExpr, method)
        }

        private fun visitAndReport(callExpr: KtCallExpression, method: PsiMethod) {
            val typeParameters = method.typeParameterList?.typeParameters ?: emptyArray()
            val typeArguments = callExpr.typeArgumentList?.arguments ?: emptyList()
            val typeArgumentsText = typeArguments.mapNotNull { it.text }
            val typedViewFunctionIndex = typeParameters.indexOfFirst {
                it.extendsListTypes.any { type ->
                    type.canonicalText == VIEW_CLASS_NAME ||
                        type.canonicalText == VIEW_GROUP_CLASS_NAME 
                }
            }

            val isTypedViewFunction = typedViewFunctionIndex >= 0

            val imports = typeArgumentsText.mapNotNull { typeName ->
                when {
                    // Like `TextView`.
                    !typeName.contains(".") -> importReferences.firstOrNull {
                        it.packagePrefix == BUILT_IN_WIDGETS_PACKAGE_PREFIX &&
                            (it.alias == typeName || it.name == typeName)
                    }
                    // Like `android.widget.TextView`.
                    typeName.startsWith(BUILT_IN_WIDGETS_PACKAGE_PREFIX) ->
                        ImportReference(BUILT_IN_WIDGETS_PACKAGE_PREFIX, typeName.replace("$BUILT_IN_WIDGETS_PACKAGE_PREFIX.", ""))
                    else -> null
                }
            }

            val matchedIndex = builtInWidgets.indexOfFirst { imports.any { e -> e.alias == it || e.name == it } }
            val isBuiltInWidget = matchedIndex >= 0

            if (isTypedViewFunction && isBuiltInWidget) {
                val widgetName = builtInWidgets[matchedIndex]

                val sourceLocation = context.getLocation(callExpr)
                val sourceText = callExpr.toUElement()?.asSourceString() ?: return
                val callExprElement = callExpr.toUElement() ?: return

                // Matches '>' and like `View<TextView`'s length + 1.
                val callExprLength = sourceText.split(">")[0].trim().length + 1
                val nameLocation = context.getRangeLocation(callExprElement, fromDelta = 0, callExprLength)

                // Only replace the first one, because there may be multiple sub-functions in DSL.
                val replacement = sourceText.replaceFirst(viewExpressionRegex, widgetName)

                val lintFix = LintFix.create()
                    .name("Replace with '$widgetName'")
                    .replace()
                    .range(sourceLocation)
                    .with(replacement)
                    .imports("$WIDGET_FUNCTION_PREFIX.$widgetName")
                    .reformat(true)
                    .build()

                val message = "Can be simplified to `$widgetName`."
                context.report(ISSUE, callExpr, nameLocation, message, lintFix)
            }
        }
    }
}