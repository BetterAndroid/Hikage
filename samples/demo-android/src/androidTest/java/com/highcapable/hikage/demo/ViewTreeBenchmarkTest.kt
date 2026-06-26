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
 * This file is created by fankes on 2026/6/15.
 */
package com.highcapable.hikage.demo

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.annotation.Hikagable
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.attribute.android
import com.highcapable.hikage.core.base.Hikagable
import com.highcapable.hikage.core.layout.LayoutParams
import com.highcapable.hikage.widget.android.widget.Button
import com.highcapable.hikage.widget.android.widget.CheckBox
import com.highcapable.hikage.widget.android.widget.EditText
import com.highcapable.hikage.widget.android.widget.ImageButton
import com.highcapable.hikage.widget.android.widget.ImageView
import com.highcapable.hikage.widget.android.widget.LinearLayout
import com.highcapable.hikage.widget.android.widget.ProgressBar
import com.highcapable.hikage.widget.android.widget.RatingBar
import com.highcapable.hikage.widget.android.widget.Space
import com.highcapable.hikage.widget.android.widget.Switch
import com.highcapable.hikage.widget.android.widget.TextView
import com.highcapable.hikage.widget.androidx.coordinatorlayout.widget.CoordinatorLayout
import com.highcapable.hikage.widget.com.google.android.material.appbar.MaterialToolbar
import com.highcapable.hikage.widget.com.google.android.material.button.MaterialButton
import com.highcapable.hikage.widget.com.google.android.material.card.MaterialCardView
import com.highcapable.hikage.widget.com.google.android.material.chip.ChipGroup
import com.highcapable.hikage.widget.com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputEditText
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputLayout
import com.highcapable.hikage.widget.com.highcapable.hikage.demo.ui.widget.CheckableChip
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.reflect.Constructor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference
import com.google.android.material.R as Material_R

/**
 * Groups the manual view-tree performance probes for XML, Hikage DSL, attrs-heavy DSL, and constructor reflection.
 */
@RunWith(AndroidJUnit4::class)
class ViewTreeBenchmarkTest {

    private companion object {

        const val SECTION_COUNT = 11
        const val VIEW_COUNT = 210
        const val DEMO_SECTION_COUNT = 11
        const val WARMUP_ITERATIONS = 10
        const val MEASURED_ITERATIONS = 80
        const val HEAVY_WARMUP_ITERATIONS = 3
        const val HEAVY_MEASURED_ITERATIONS = 30
        const val TEXT_INPUT_LAYOUT_BENCHMARK_ROUNDS = 5
        const val TEXT_INPUT_LAYOUT_SECTION_COUNT = 11
        const val NANOS_PER_MILLI = 1_000_000.0
        const val TAG = "HikageBenchmark"
        const val REPORT_FILE_PREFIX = "ViewTreeBenchmarkTest"
        const val UTC_TIME_ZONE = "UTC"

        val textInputLayoutConstructor: Constructor<TextInputLayout> =
            TextInputLayout::class.java.getConstructor(Context::class.java)
        val textInputEditTextConstructor: Constructor<TextInputEditText> =
            TextInputEditText::class.java.getConstructor(Context::class.java)

        var viewSink = 0

        fun toMillis(value: Long) = value.toDouble() / NANOS_PER_MILLI

        fun formatMs(value: Double) = "%.3f".format(value)

        fun formatRatio(value: Double) = "%.2f".format(value)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Compares the 210-view stress tree across XML, plain Hikage, and attrs-heavy Hikage variants.
     */
    @Test
    fun benchmarkXmlAndHikage210ViewTreeCreation() {
        val result = AtomicReference<BenchmarkReport>()
        val error = AtomicReference<Throwable>()
        val previousAutoProcessWithFactory2 = Hikage.isAutoProcessWithFactory2

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            try {
                Hikage.isAutoProcessWithFactory2 = false

                val xmlRoot = createXmlTree(context)
                val hikageRoot = createHikageTree(context)
                val hikageRootWithAttrs = createHikageTreeWithAttrs(context)
                val hikageRootWithFullyAttrs = createHikageTreeWithFullyAttrs(context)
                val hikageRootWithFullViewAttrs = createHikageTreeWithFullViewAttrs(context)

                assertEquals(VIEW_COUNT, xmlRoot.countViews())
                assertEquals(VIEW_COUNT, hikageRoot.countViews())
                assertEquals(VIEW_COUNT, hikageRootWithAttrs.countViews())
                assertEquals(VIEW_COUNT, hikageRootWithFullyAttrs.countViews())
                assertEquals(VIEW_COUNT, hikageRootWithFullViewAttrs.countViews())

                repeat(WARMUP_ITERATIONS) {
                    createXmlTree(context).consume()
                    createHikageTree(context).consume()
                    createHikageTreeWithAttrs(context).consume()
                    createHikageTreeWithFullyAttrs(context).consume()
                    createHikageTreeWithFullViewAttrs(context).consume()
                }

                result.set(
                    BenchmarkReport(
                        viewCount = VIEW_COUNT,
                        warmupIterations = WARMUP_ITERATIONS,
                        measuredIterations = MEASURED_ITERATIONS,
                        xml = measureTreeCreation { createXmlTree(context) },
                        hikage = measureTreeCreation { createHikageTree(context) },
                        hikageWithAttrs = measureTreeCreation { createHikageTreeWithAttrs(context) },
                        hikageWithFullyAttrs = measureTreeCreation { createHikageTreeWithFullyAttrs(context) },
                        hikageWithFullViewAttrs = measureTreeCreation { createHikageTreeWithFullViewAttrs(context) }
                    )
                )
            } catch (throwable: Throwable) {
                error.set(throwable)
            } finally {
                Hikage.isAutoProcessWithFactory2 = previousAutoProcessWithFactory2
            }
        }

        error.get()?.let { throw it }

        val report = result.get()
        writeBenchmarkReport(
            fileSuffix = "benchmarkXmlAndHikage210ViewTreeCreation",
            title = "210 View Tree Benchmark",
            body = report.toHtmlBody()
        )
    }

    /**
     * Compares the sample Material-style demo layout between XML inflation and equivalent Hikage construction.
     */
    @Test
    fun benchmarkDemoLayoutCreation() {
        val result = AtomicReference<DemoBenchmarkReport>()
        val error = AtomicReference<Throwable>()
        val previousAutoProcessWithFactory2 = Hikage.isAutoProcessWithFactory2

        try {
            Hikage.isAutoProcessWithFactory2 = false

            val demoContext = ContextThemeWrapper(context, R.style.Theme_DefaultAppTheme)
            val xmlContext = ContextThemeWrapper(context, R.style.Theme_DefaultAppTheme)
            val xmlViewCount = runOnMainAndCreate { createDemoXmlLayout(xmlContext) }.countViews()
            val hikageViewCount = runOnMainAndCreate { createDemoHikageLayout(demoContext) }.countViews()

            assertEquals(xmlViewCount, hikageViewCount)

            repeat(HEAVY_WARMUP_ITERATIONS) {
                measureMainTreeCreation { createDemoXmlLayout(xmlContext) }
                measureMainTreeCreation { createDemoHikageLayout(demoContext) }
            }

            result.set(
                DemoBenchmarkReport(
                    viewCount = xmlViewCount,
                    warmupIterations = HEAVY_WARMUP_ITERATIONS,
                    measuredIterations = HEAVY_MEASURED_ITERATIONS,
                    xml = measureMainTreeCreation(HEAVY_MEASURED_ITERATIONS) { createDemoXmlLayout(xmlContext) },
                    hikage = measureMainTreeCreation(HEAVY_MEASURED_ITERATIONS) { createDemoHikageLayout(demoContext) }
                )
            )
        } catch (throwable: Throwable) {
            error.set(throwable)
        } finally {
            Hikage.isAutoProcessWithFactory2 = previousAutoProcessWithFactory2
        }

        error.get()?.let { throw it }

        val report = result.get()
        writeBenchmarkReport(
            fileSuffix = "benchmarkDemoLayoutCreation",
            title = "Demo Layout Benchmark",
            body = report.toHtmlBody()
        )
    }

    /**
     * Measures direct Material TextInputLayout construction against reflective constructor invocation.
     */
    @Test
    fun benchmarkTextInputLayoutDirectAndReflectCreation() {
        val result = AtomicReference<TextInputLayoutBenchmarkReport>()
        val error = AtomicReference<Throwable>()

        try {
            val demoContext = ContextThemeWrapper(context, R.style.Theme_DefaultAppTheme)
            val viewCount = runOnMainAndCreate { createDirectTextInputLayoutBlock(demoContext) }.countViews()
            val reflectViewCount = runOnMainAndCreate { createReflectTextInputLayoutBlock(demoContext) }.countViews()

            assertEquals(viewCount, reflectViewCount)

            repeat(HEAVY_WARMUP_ITERATIONS) {
                measureMainTreeCreation { createDirectTextInputLayoutBlock(demoContext) }
                measureMainTreeCreation { createReflectTextInputLayoutBlock(demoContext) }
            }

            result.set(
                TextInputLayoutBenchmarkReport(
                    viewCount = viewCount,
                    warmupIterations = HEAVY_WARMUP_ITERATIONS,
                    measuredIterations = HEAVY_MEASURED_ITERATIONS,
                    rounds = List(TEXT_INPUT_LAYOUT_BENCHMARK_ROUNDS) {
                        TextInputLayoutBenchmarkRound(
                            direct = measureMainTreeCreation(HEAVY_MEASURED_ITERATIONS) { createDirectTextInputLayoutBlock(demoContext) },
                            reflect = measureMainTreeCreation(HEAVY_MEASURED_ITERATIONS) { createReflectTextInputLayoutBlock(demoContext) }
                        )
                    }
                )
            )
        } catch (throwable: Throwable) {
            error.set(throwable)
        }

        error.get()?.let { throw it }

        val report = result.get()
        writeBenchmarkReport(
            fileSuffix = "benchmarkTextInputLayoutDirectAndReflectCreation",
            title = "TextInputLayout Creation Benchmark",
            body = report.toHtmlBody()
        )
    }

    private fun createXmlTree(context: Context) =
        context.layoutInflater.inflate(R.layout.benchmark_210_view_tree, null, false)

    private fun createDemoXmlLayout(context: Context) =
        context.layoutInflater.cloneInContext(context).inflate(R.layout.benchmark_demo_layout, null, false)

    private fun createDirectTextInputLayoutBlock(context: Context) =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            repeat(TEXT_INPUT_LAYOUT_SECTION_COUNT) {
                addView(createDirectTextInputLayoutSection(context))
            }
        }

    private fun createReflectTextInputLayoutBlock(context: Context) =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            repeat(TEXT_INPUT_LAYOUT_SECTION_COUNT) {
                addView(createReflectTextInputLayoutSection(context))
            }
        }

    private fun createDirectTextInputLayoutSection(context: Context) =
        TextInputLayout(context).apply {
            hint = context.getString(R.string.text_password)
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            addView(
                TextInputEditText(context).apply {
                    isSingleLine = true
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            )
        }

    private fun createReflectTextInputLayoutSection(context: Context) =
        textInputLayoutConstructor.newInstance(context).apply {
            hint = context.getString(R.string.text_password)
            endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            addView(
                textInputEditTextConstructor.newInstance(context).apply {
                    isSingleLine = true
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
            )
        }

    private fun createHikageTree(context: Context) = Hikagable(context) {
        LinearLayout {
            repeat(SECTION_COUNT) { benchmarkSection() }
        }
    }.root

    private fun createHikageTreeWithAttrs(context: Context) = Hikagable(context) {
        LinearLayout(
            attrs = {
                android {
                    set("orientation", "vertical")
                }
            }
        ) {
            repeat(SECTION_COUNT) { benchmarkSectionWithAttrs() }
        }
    }.root

    private fun createHikageTreeWithFullyAttrs(context: Context) = Hikagable(context) {
        LinearLayout(
            attrs = {
                android {
                    set("layout_width", "match_parent")
                    set("layout_height", "wrap_content")
                    set("orientation", "vertical")
                }
            }
        ) {
            repeat(SECTION_COUNT) { benchmarkSectionWithFullyAttrs() }
        }
    }.root

    private fun createHikageTreeWithFullViewAttrs(context: Context) = Hikagable(context) {
        LinearLayout(
            lparams = LayoutParams(widthMatchParent = true),
            attrs = {
                android {
                    set("orientation", "vertical")
                }
            }
        ) {
            repeat(SECTION_COUNT) { benchmarkSectionWithFullViewAttrs() }
        }
    }.root

    private fun createDemoHikageLayout(context: Context) = Hikagable(context) {
        CoordinatorLayout(
            lparams = LayoutParams(matchParent = true)
        ) {
            MaterialToolbar(
                lparams = LayoutParams(widthMatchParent = true),
                init = {
                    title = stringResource(R.string.app_name)
                }
            )
            LinearLayout(
                lparams = LayoutParams(matchParent = true) {
                    topMargin = dimenResource(Material_R.dimen.m3_appbar_size_compact).toInt()
                },
                init = {
                    orientation = LinearLayout.VERTICAL
                    setPadding(16.dp)
                }
            ) {
                repeat(DEMO_SECTION_COUNT) { benchmarkDemoSection() }
            }
        }
    }.root

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkDemoSection() {
        LinearLayout(
            lparams = LayoutParams(widthMatchParent = true) {
                topMargin = 16.dp
            },
            init = {
                orientation = LinearLayout.VERTICAL
            }
        ) {
            TextInputLayout(
                lparams = LayoutParams(widthMatchParent = true),
                init = {
                    hint = stringResource(R.string.text_username)
                }
            ) {
                TextInputEditText(
                    lparams = LayoutParams(widthMatchParent = true),
                    init = {
                        isSingleLine = true
                    }
                )
            }
            TextInputLayout(
                lparams = LayoutParams(widthMatchParent = true) {
                    topMargin = 12.dp
                },
                init = {
                    hint = stringResource(R.string.text_password)
                    endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                }
            ) {
                TextInputEditText(
                    lparams = LayoutParams(widthMatchParent = true),
                    init = {
                        isSingleLine = true
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                )
            }
            ChipGroup(
                lparams = LayoutParams(widthMatchParent = true) {
                    topMargin = 16.dp
                },
                init = {
                    isSingleSelection = true
                }
            ) {
                repeat(2) { index ->
                    CheckableChip(
                        init = {
                            text = when (index) {
                                0 -> stringResource(R.string.text_gender_man)
                                else -> stringResource(R.string.text_gender_woman)
                            }
                        }
                    )
                }
            }
            MaterialSwitch(
                lparams = LayoutParams(widthMatchParent = true) {
                    topMargin = 16.dp
                },
                init = {
                    text = stringResource(R.string.text_enable_notification)
                }
            )
            MaterialCardView(
                lparams = LayoutParams(matchParent = true) {
                    topMargin = 16.dp
                    weight = 1f
                }
            ) {
                LinearLayout(
                    lparams = LayoutParams(matchParent = true),
                    init = {
                        orientation = LinearLayout.VERTICAL
                        setPadding(16.dp)
                    }
                ) {
                    TextView {
                        text = stringResource(R.string.text_welcome)
                    }
                    TextView(
                        lparams = LayoutParams {
                            topMargin = 8.dp
                        },
                        init = {
                            text = stringResource(R.string.text_description)
                        }
                    )
                }
            }
            MaterialButton(
                lparams = LayoutParams(widthMatchParent = true) {
                    topMargin = 20.dp
                },
                init = {
                    text = stringResource(R.string.text_submit)
                }
            )
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSection() {
        LinearLayout {
            benchmarkFirstRow()
            benchmarkSecondRow()
            benchmarkThirdRow()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSectionWithAttrs() {
        LinearLayout(
            attrs = {
                android {
                    set("orientation", "vertical")
                }
            }
        ) {
            benchmarkFirstRowWithAttrs()
            benchmarkSecondRowWithAttrs()
            benchmarkThirdRowWithAttrs()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSectionWithFullyAttrs() {
        LinearLayout(
            attrs = { benchmarkSectionAttrs() }
        ) {
            benchmarkFirstRowWithFullyAttrs()
            benchmarkSecondRowWithFullyAttrs()
            benchmarkThirdRowWithFullyAttrs()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSectionWithFullViewAttrs() {
        LinearLayout(
            lparams = LayoutParams(widthMatchParent = true),
            attrs = { benchmarkSectionNoLayoutAttrs() }
        ) {
            benchmarkFirstRowWithFullViewAttrs()
            benchmarkSecondRowWithFullViewAttrs()
            benchmarkThirdRowWithFullViewAttrs()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRow() {
        benchmarkRow {
            TextView()
            Button()
            ImageView()
            ProgressBar()
            CheckBox()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRowWithAttrs() {
        benchmarkRowWithAttrs {
            TextView(attrs = { benchmarkViewAttrs() })
            Button(attrs = { benchmarkViewAttrs() })
            ImageView(attrs = { benchmarkViewAttrs() })
            ProgressBar(attrs = { benchmarkViewAttrs() })
            CheckBox(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            TextView(attrs = { benchmarkTitleAttrs() })
            Button(attrs = { benchmarkActionAttrs() })
            ImageView(attrs = { benchmarkImageAttrs() })
            ProgressBar(attrs = { benchmarkProgressAttrs(35) })
            CheckBox(attrs = { benchmarkFlagAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRowWithFullViewAttrs() {
        benchmarkRowWithFullViewAttrs {
            TextView(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 2f },
                attrs = { benchmarkTitleNoLayoutAttrs() }
            )
            Button(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkActionNoLayoutAttrs() }
            )
            ImageView(
                lparams = LayoutParams(width = 32.dp, height = 32.dp),
                attrs = { benchmarkImageNoLayoutAttrs() }
            )
            ProgressBar(
                lparams = LayoutParams(width = 0) { weight = 1f },
                attrs = { benchmarkProgressNoLayoutAttrs(35) }
            )
            CheckBox(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkFlagNoLayoutAttrs() }
            )
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRow() {
        benchmarkRow {
            EditText()
            ImageButton()
            RatingBar()
            Switch()
            TextView()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRowWithAttrs() {
        benchmarkRowWithAttrs {
            EditText(attrs = { benchmarkViewAttrs() })
            ImageButton(attrs = { benchmarkViewAttrs() })
            RatingBar(attrs = { benchmarkViewAttrs() })
            Switch(attrs = { benchmarkViewAttrs() })
            TextView(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            EditText(attrs = { benchmarkInputAttrs() })
            ImageButton(attrs = { benchmarkSearchAttrs() })
            RatingBar(attrs = { benchmarkRatingAttrs() })
            Switch(attrs = { benchmarkToggleAttrs() })
            TextView(attrs = { benchmarkValueAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRowWithFullViewAttrs() {
        benchmarkRowWithFullViewAttrs {
            EditText(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 2f },
                attrs = { benchmarkInputNoLayoutAttrs() }
            )
            ImageButton(
                lparams = LayoutParams(width = 40.dp, height = 40.dp),
                attrs = { benchmarkSearchNoLayoutAttrs() }
            )
            RatingBar(
                lparams = LayoutParams(),
                attrs = { benchmarkRatingNoLayoutAttrs() }
            )
            Switch(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkToggleNoLayoutAttrs() }
            )
            TextView(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkValueNoLayoutAttrs() }
            )
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRow() {
        benchmarkRow {
            Space()
            TextView()
            Button()
            ProgressBar()
            CheckBox()
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRowWithAttrs() {
        benchmarkRowWithAttrs {
            Space(attrs = { benchmarkViewAttrs() })
            TextView(attrs = { benchmarkViewAttrs() })
            Button(attrs = { benchmarkViewAttrs() })
            ProgressBar(attrs = { benchmarkViewAttrs() })
            CheckBox(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            Space(attrs = { benchmarkSpaceAttrs() })
            TextView(attrs = { benchmarkDetailAttrs() })
            Button(attrs = { benchmarkDisabledAttrs() })
            ProgressBar(attrs = { benchmarkProgressAttrs(70) })
            CheckBox(attrs = { benchmarkReadyAttrs() })
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRowWithFullViewAttrs() {
        benchmarkRowWithFullViewAttrs {
            Space(
                lparams = LayoutParams(width = 8.dp, heightMatchParent = true)
            )
            TextView(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 2f },
                attrs = { benchmarkDetailNoLayoutAttrs() }
            )
            Button(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkDisabledNoLayoutAttrs() }
            )
            ProgressBar(
                lparams = LayoutParams(width = 0) { weight = 1f },
                attrs = { benchmarkProgressNoLayoutAttrs(70) }
            )
            CheckBox(
                lparams = LayoutParams(width = 0, heightMatchParent = true) { weight = 1f },
                attrs = { benchmarkReadyNoLayoutAttrs() }
            )
        }
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRow(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            performer = performer
        )
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRowWithAttrs(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            attrs = {
                android {
                    set("orientation", "horizontal")
                }
            },
            performer = performer
        )
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRowWithFullyAttrs(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            attrs = { benchmarkRowAttrs() },
            performer = performer
        )
    }

    @Hikagable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRowWithFullViewAttrs(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            lparams = LayoutParams(widthMatchParent = true, height = 40.dp),
            attrs = { benchmarkRowNoLayoutAttrs() },
            performer = performer
        )
    }

    private fun Hikage.Attribute.benchmarkViewAttrs() {
        android {
            set("enabled", true)
        }
    }

    private fun Hikage.Attribute.benchmarkSectionAttrs() {
        android {
            set("layout_width", "match_parent")
            set("layout_height", "wrap_content")
            set("background", "@color/background_day")
            set("orientation", "vertical")
            set("padding", "2dp")
        }
    }

    private fun Hikage.Attribute.benchmarkSectionNoLayoutAttrs() {
        android {
            set("background", "@color/background_day")
            set("orientation", "vertical")
            set("padding", "2dp")
        }
    }

    private fun Hikage.Attribute.benchmarkRowAttrs() {
        android {
            set("layout_width", "match_parent")
            set("layout_height", "40dp")
            set("gravity", "center_vertical")
            set("orientation", "horizontal")
        }
    }

    private fun Hikage.Attribute.benchmarkRowNoLayoutAttrs() {
        android {
            set("gravity", "center_vertical")
            set("orientation", "horizontal")
        }
    }

    private fun Hikage.Attribute.benchmarkTitleAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "2")
            set("gravity", "center_vertical")
            set("paddingStart", "4dp")
            set("paddingEnd", "4dp")
            set("singleLine", true)
            set("text", "Benchmark title")
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkTitleNoLayoutAttrs() {
        android {
            set("gravity", "center_vertical")
            set("paddingStart", "4dp")
            set("paddingEnd", "4dp")
            set("singleLine", true)
            set("text", "Benchmark title")
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkActionAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("text", "Action")
            set("textAllCaps", false)
        }
    }

    private fun Hikage.Attribute.benchmarkActionNoLayoutAttrs() {
        android {
            set("text", "Action")
            set("textAllCaps", false)
        }
    }

    private fun Hikage.Attribute.benchmarkImageAttrs() {
        android {
            set("layout_width", "32dp")
            set("layout_height", "32dp")
            set("contentDescription", "Benchmark image")
            set("src", "@android:drawable/ic_menu_gallery")
        }
    }

    private fun Hikage.Attribute.benchmarkImageNoLayoutAttrs() {
        android {
            set("contentDescription", "Benchmark image")
            set("src", "@android:drawable/ic_menu_gallery")
        }
    }

    private fun Hikage.Attribute.benchmarkProgressAttrs(progress: Int) {
        android {
            set("layout_width", "0dp")
            set("layout_height", "wrap_content")
            set("layout_weight", "1")
            set("indeterminate", false)
            set("max", 100)
            set("progress", progress)
        }
    }

    private fun Hikage.Attribute.benchmarkProgressNoLayoutAttrs(progress: Int) {
        android {
            set("indeterminate", false)
            set("max", 100)
            set("progress", progress)
        }
    }

    private fun Hikage.Attribute.benchmarkFlagAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("checked", false)
            set("text", "Flag")
        }
    }

    private fun Hikage.Attribute.benchmarkFlagNoLayoutAttrs() {
        android {
            set("checked", false)
            set("text", "Flag")
        }
    }

    private fun Hikage.Attribute.benchmarkInputAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "2")
            set("hint", "Input")
            set("singleLine", true)
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkInputNoLayoutAttrs() {
        android {
            set("hint", "Input")
            set("singleLine", true)
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkSearchAttrs() {
        android {
            set("layout_width", "40dp")
            set("layout_height", "40dp")
            set("contentDescription", "Benchmark button")
            set("src", "@android:drawable/ic_menu_search")
        }
    }

    private fun Hikage.Attribute.benchmarkSearchNoLayoutAttrs() {
        android {
            set("contentDescription", "Benchmark button")
            set("src", "@android:drawable/ic_menu_search")
        }
    }

    private fun Hikage.Attribute.benchmarkRatingAttrs() {
        android {
            set("layout_width", "wrap_content")
            set("layout_height", "wrap_content")
            set("isIndicator", true)
            set("numStars", 3)
            set("rating", "2")
        }
    }

    private fun Hikage.Attribute.benchmarkRatingNoLayoutAttrs() {
        android {
            set("isIndicator", true)
            set("numStars", 3)
            set("rating", "2")
        }
    }

    private fun Hikage.Attribute.benchmarkToggleAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("checked", true)
            set("text", "Toggle")
        }
    }

    private fun Hikage.Attribute.benchmarkToggleNoLayoutAttrs() {
        android {
            set("checked", true)
            set("text", "Toggle")
        }
    }

    private fun Hikage.Attribute.benchmarkValueAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("gravity", "center")
            set("text", "Value")
            set("textColor", "@color/theme")
        }
    }

    private fun Hikage.Attribute.benchmarkValueNoLayoutAttrs() {
        android {
            set("gravity", "center")
            set("text", "Value")
            set("textColor", "@color/theme")
        }
    }

    private fun Hikage.Attribute.benchmarkSpaceAttrs() {
        android {
            set("layout_width", "8dp")
            set("layout_height", "match_parent")
        }
    }

    private fun Hikage.Attribute.benchmarkDetailAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "2")
            set("gravity", "center_vertical")
            set("text", "Detail")
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkDetailNoLayoutAttrs() {
        android {
            set("gravity", "center_vertical")
            set("text", "Detail")
            set("textColor", "@color/black")
        }
    }

    private fun Hikage.Attribute.benchmarkDisabledAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("enabled", false)
            set("text", "Disabled")
            set("textAllCaps", false)
        }
    }

    private fun Hikage.Attribute.benchmarkDisabledNoLayoutAttrs() {
        android {
            set("enabled", false)
            set("text", "Disabled")
            set("textAllCaps", false)
        }
    }

    private fun Hikage.Attribute.benchmarkReadyAttrs() {
        android {
            set("layout_width", "0dp")
            set("layout_height", "match_parent")
            set("layout_weight", "1")
            set("checked", true)
            set("text", "Ready")
        }
    }

    private fun Hikage.Attribute.benchmarkReadyNoLayoutAttrs() {
        android {
            set("checked", true)
            set("text", "Ready")
        }
    }

    private fun measureTreeCreation(block: () -> View): BenchmarkStats {
        val costs = LongArray(MEASURED_ITERATIONS)

        repeat(MEASURED_ITERATIONS) { index ->
            val start = SystemClock.elapsedRealtimeNanos()
            block().consume()
            costs[index] = SystemClock.elapsedRealtimeNanos() - start
        }

        return BenchmarkStats.from(costs)
    }

    private fun measureMainTreeCreation(iterations: Int = 1, block: () -> View): BenchmarkStats {
        val costs = LongArray(iterations)

        repeat(iterations) { index ->
            val cost = AtomicReference<Long>()
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val start = SystemClock.elapsedRealtimeNanos()
                block().consume()
                cost.set(SystemClock.elapsedRealtimeNanos() - start)
            }
            costs[index] = cost.get()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }

        return BenchmarkStats.from(costs)
    }

    private fun runOnMainAndCreate(block: () -> View): View {
        val view = AtomicReference<View>()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view.set(block())
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        return view.get()
    }

    private fun View.countViews(): Int {
        if (this !is ViewGroup) return 1

        var count = 1
        repeat(childCount) { index ->
            count += getChildAt(index).countViews()
        }

        return count
    }

    private fun View.consume() {
        viewSink = viewSink xor ((this as? ViewGroup)?.childCount ?: 0) xor id
    }

    private fun writeBenchmarkReport(fileSuffix: String, title: String, body: String) {
        val createdAt = Date()
        val deviceName = deviceName()
        val androidVersionName = androidVersionName()
        val displayDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone(UTC_TIME_ZONE)
        }.format(createdAt)
        val fileName = "${REPORT_FILE_PREFIX}_$fileSuffix.html"

        val html = buildString {
            appendLine("<!doctype html>")
            appendLine("<html lang=\"en\">")
            appendLine("<head>")
            appendLine("<meta charset=\"utf-8\">")
            appendLine("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
            appendLine("<title>${title.escapeHtml()}</title>")
            appendLine("<style>")
            appendLine("""
                :root {
                    color-scheme: light dark;
                    --bg: #f6f7f9;
                    --surface: #ffffff;
                    --text: #1f2328;
                    --muted: #667085;
                    --line: #d9dee7;
                    --accent: #006d77;
                    --accent-soft: #e1f3f1;
                }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --bg: #111418;
                        --surface: #1a1f25;
                        --text: #ecf0f4;
                        --muted: #aab4c0;
                        --line: #303844;
                        --accent: #7dd3c7;
                        --accent-soft: #173d3b;
                    }
                }
                * { box-sizing: border-box; }
                body {
                    margin: 0;
                    background: var(--bg);
                    color: var(--text);
                    font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                    line-height: 1.5;
                }
                main {
                    width: min(1180px, calc(100vw - 32px));
                    margin: 0 auto;
                    padding: 32px 0 48px;
                }
                header {
                    margin-bottom: 24px;
                }
                h1 {
                    margin: 0 0 8px;
                    font-size: 30px;
                    font-weight: 720;
                    letter-spacing: 0;
                }
                h2 {
                    margin: 28px 0 12px;
                    font-size: 18px;
                    letter-spacing: 0;
                }
                .meta {
                    color: var(--muted);
                    font-size: 14px;
                }
                .device-meta {
                    display: flex;
                    align-items: baseline;
                    gap: 12px;
                    flex-wrap: wrap;
                }
                .meta-subtle {
                    color: color-mix(in srgb, var(--muted) 78%, transparent);
                    font-size: 13px;
                }
                .panel {
                    background: var(--surface);
                    border: 1px solid var(--line);
                    border-radius: 8px;
                    padding: 18px;
                    margin: 16px 0;
                    overflow-x: auto;
                }
                .summary {
                    display: grid;
                    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
                    gap: 12px;
                }
                .summary div {
                    background: var(--accent-soft);
                    border: 1px solid color-mix(in srgb, var(--accent) 35%, transparent);
                    border-radius: 8px;
                    padding: 12px;
                }
                .summary strong {
                    display: block;
                    font-size: 22px;
                    color: var(--accent);
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 14px;
                }
                th, td {
                    padding: 10px 12px;
                    border-bottom: 1px solid var(--line);
                    text-align: right;
                    white-space: nowrap;
                }
                th:first-child, td:first-child {
                    text-align: left;
                }
                th {
                    color: var(--muted);
                    font-weight: 650;
                }
                tr:last-child td {
                    border-bottom: 0;
                }
            """.trimIndent())
            appendLine("</style>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("<main>")
            appendLine("<header>")
            appendLine("<h1>${title.escapeHtml()}</h1>")
            appendLine("<div class=\"device-meta\">")
            appendLine("<span class=\"meta\">${deviceName.escapeHtml()}</span>")
            appendLine("<span class=\"meta-subtle\">${androidVersionName.escapeHtml()}</span>")
            appendLine("</div>")
            appendLine("<div class=\"meta\">Generated at ${displayDateTime.escapeHtml()}</div>")
            appendLine("</header>")
            appendLine(body)
            appendLine("</main>")
            appendLine("</body>")
            appendLine("</html>")
        }

        writeBenchmarkReportFile(fileName, html)
        Log.i(TAG, "Benchmark HTML report written: $fileName")
    }

    private fun writeBenchmarkReportFile(fileName: String, html: String) {
        try {
            PlatformTestStorageRegistry.getInstance().openOutputFile(fileName).bufferedWriter().use {
                it.write(html)
            }
        } catch (exception: RuntimeException) {
            if (!exception.hasCause<SecurityException>()) throw exception
            Log.w(TAG, "Platform test storage is not writable, fallback to target app cache.", exception)
            writeBenchmarkReportFileToTargetCache(fileName, html)
        }
    }

    private fun writeBenchmarkReportFileToTargetCache(fileName: String, html: String) {
        val outputRoot = File(context.externalCacheDir ?: context.cacheDir, "additionalTestOutputDir")
        if (!outputRoot.exists() && !outputRoot.mkdirs())
            error("Failed to create benchmark report output directory: ${outputRoot.absolutePath}")
        File(outputRoot, fileName).bufferedWriter().use {
            it.write(html)
        }
    }

    private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is T) return true
            current = current.cause
        }

        return false
    }

    private fun deviceName() = "${Build.BRAND} ${Build.MODEL}".trim().ifBlank { "Unknown Device" }
    private fun androidVersionName() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    private fun statsTable(rows: List<Pair<String, BenchmarkStats>>) = buildString {
        appendLine("<section class=\"panel\">")
        appendLine("<table>")
        appendLine("<thead><tr><th>Case</th><th>Min</th><th>Median</th><th>P90</th><th>Average</th><th>Max</th></tr></thead>")
        appendLine("<tbody>")
        rows.forEach { (name, stats) ->
            appendLine(
                "<tr><td>${name.escapeHtml()}</td><td>${formatMs(stats.minMs)} ms</td>" +
                    "<td>${formatMs(stats.medianMs)} ms</td><td>${formatMs(stats.p90Ms)} ms</td>" +
                    "<td>${formatMs(stats.averageMs)} ms</td><td>${formatMs(stats.maxMs)} ms</td></tr>"
            )
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</section>")
    }

    private fun ratioTable(rows: List<Triple<String, Double, Double>>) = buildString {
        appendLine("<section class=\"panel\">")
        appendLine("<table>")
        appendLine("<thead><tr><th>Ratio</th><th>Average</th><th>Median</th></tr></thead>")
        appendLine("<tbody>")
        rows.forEach { (name, average, median) ->
            appendLine("<tr><td>${name.escapeHtml()}</td><td>${formatRatio(average)}x</td><td>${formatRatio(median)}x</td></tr>")
        }
        appendLine("</tbody>")
        appendLine("</table>")
        appendLine("</section>")
    }

    private fun summaryPanel(vararg rows: Pair<String, String>) = buildString {
        appendLine("<section class=\"summary\">")
        rows.forEach { (label, value) ->
            appendLine("<div><span>${label.escapeHtml()}</span><strong>${value.escapeHtml()}</strong></div>")
        }
        appendLine("</section>")
    }

    private fun String.escapeHtml() =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")

    private fun BenchmarkReport.toHtmlBody() = buildString {
        appendLine(summaryPanel("Views" to "$viewCount", "Warmup" to "$warmupIterations", "Iterations" to "$measuredIterations"))
        appendLine("<h2>Timing</h2>")
        appendLine(
            statsTable(
                listOf(
                    "XML" to xml,
                    "Hikage" to hikage,
                    "Hikage with attrs" to hikageWithAttrs,
                    "Hikage fully attrs" to hikageWithFullyAttrs,
                    "Hikage full view attrs" to hikageWithFullViewAttrs
                )
            )
        )
        appendLine("<h2>Ratios</h2>")
        appendLine(
            ratioTable(
                listOf(
                    Triple("Hikage / XML", hikage.averageMs / xml.averageMs, hikage.medianMs / xml.medianMs),
                    Triple("HikageAttrs / XML", hikageWithAttrs.averageMs / xml.averageMs, hikageWithAttrs.medianMs / xml.medianMs),
                    Triple("HikageAttrs / Hikage", hikageWithAttrs.averageMs / hikage.averageMs, hikageWithAttrs.medianMs / hikage.medianMs),
                    Triple("HikageFullAttrs / XML", hikageWithFullyAttrs.averageMs / xml.averageMs, hikageWithFullyAttrs.medianMs / xml.medianMs),
                    Triple("HikageFullAttrs / Hikage", hikageWithFullyAttrs.averageMs / hikage.averageMs, hikageWithFullyAttrs.medianMs / hikage.medianMs),
                    Triple(
                        "HikageFullViewAttrs / XML",
                        hikageWithFullViewAttrs.averageMs / xml.averageMs,
                        hikageWithFullViewAttrs.medianMs / xml.medianMs
                    ),
                    Triple(
                        "HikageFullViewAttrs / Hikage",
                        hikageWithFullViewAttrs.averageMs / hikage.averageMs,
                        hikageWithFullViewAttrs.medianMs / hikage.medianMs
                    ),
                    Triple(
                        "FullAttrs / FullViewAttrs",
                        hikageWithFullyAttrs.averageMs / hikageWithFullViewAttrs.averageMs,
                        hikageWithFullyAttrs.medianMs / hikageWithFullViewAttrs.medianMs
                    )
                )
            )
        )
    }

    private fun DemoBenchmarkReport.toHtmlBody() = buildString {
        appendLine(summaryPanel("Views" to "$viewCount", "Warmup" to "$warmupIterations", "Iterations" to "$measuredIterations"))
        appendLine("<h2>Timing</h2>")
        appendLine(statsTable(listOf("Demo XML" to xml, "Demo Hikage" to hikage)))
        appendLine("<h2>Ratios</h2>")
        appendLine(ratioTable(listOf(Triple("Demo Hikage / Demo XML", hikage.averageMs / xml.averageMs, hikage.medianMs / xml.medianMs))))
    }

    private fun TextInputLayoutBenchmarkReport.toHtmlBody() = buildString {
        appendLine(
            summaryPanel(
                "Views" to "$viewCount",
                "Warmup" to "$warmupIterations",
                "Iterations" to "$measuredIterations",
                "Rounds" to "${rounds.size}"
            )
        )
        appendLine("<h2>Timing</h2>")
        appendLine(
            statsTable(
                rounds.flatMapIndexed { index, round ->
                    listOf(
                        "Round ${index + 1} direct" to round.direct,
                        "Round ${index + 1} reflect" to round.reflect
                    )
                }
            )
        )
        appendLine("<h2>Ratios</h2>")
        appendLine(
            ratioTable(
                rounds.mapIndexed { index, round ->
                    Triple("Round ${index + 1} reflect / direct", round.reflect.averageMs / round.direct.averageMs, round.reflect.medianMs / round.direct.medianMs)
                }
            )
        )
    }

    private data class BenchmarkReport(
        val viewCount: Int,
        val warmupIterations: Int,
        val measuredIterations: Int,
        val xml: BenchmarkStats,
        val hikage: BenchmarkStats,
        val hikageWithAttrs: BenchmarkStats,
        val hikageWithFullyAttrs: BenchmarkStats,
        val hikageWithFullViewAttrs: BenchmarkStats
    )

    private data class BenchmarkStats(
        val minMs: Double,
        val medianMs: Double,
        val p90Ms: Double,
        val averageMs: Double,
        val maxMs: Double
    ) {

        companion object {

            fun from(costsNanos: LongArray): BenchmarkStats {
                val sorted = costsNanos.sorted()

                return BenchmarkStats(
                    minMs = toMillis(sorted.first()),
                    medianMs = toMillis(sorted[sorted.size / 2]),
                    p90Ms = toMillis(sorted[(sorted.lastIndex * 0.9f).toInt()]),
                    averageMs = costsNanos.average() / NANOS_PER_MILLI,
                    maxMs = toMillis(sorted.last())
                )
            }
        }
    }

    private data class DemoBenchmarkReport(
        val viewCount: Int,
        val warmupIterations: Int,
        val measuredIterations: Int,
        val xml: BenchmarkStats,
        val hikage: BenchmarkStats
    )

    private data class TextInputLayoutBenchmarkReport(
        val viewCount: Int,
        val warmupIterations: Int,
        val measuredIterations: Int,
        val rounds: List<TextInputLayoutBenchmarkRound>
    )

    private data class TextInputLayoutBenchmarkRound(
        val direct: BenchmarkStats,
        val reflect: BenchmarkStats
    )
}