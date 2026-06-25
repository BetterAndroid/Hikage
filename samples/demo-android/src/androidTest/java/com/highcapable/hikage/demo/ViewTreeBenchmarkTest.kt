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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.annotation.Hikageable
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.attribute.android
import com.highcapable.hikage.core.base.Hikageable
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
import java.lang.reflect.Constructor
import java.util.concurrent.atomic.AtomicReference
import com.google.android.material.R as Material_R
import com.highcapable.hikage.demo.test.R as Test_R

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

        val textInputLayoutConstructor: Constructor<TextInputLayout> =
            TextInputLayout::class.java.getConstructor(Context::class.java)
        val textInputEditTextConstructor: Constructor<TextInputEditText> =
            TextInputEditText::class.java.getConstructor(Context::class.java)

        var viewSink = 0

        fun toMillis(value: Long) = value.toDouble() / NANOS_PER_MILLI

        fun formatMs(value: Double) = "%.3f".format(value)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val testContext get() = InstrumentationRegistry.getInstrumentation().context

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

                val xmlRoot = createXmlTree(testContext)
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
                    createXmlTree(testContext).consume()
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
                        xml = measureTreeCreation { createXmlTree(testContext) },
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
        Log.i(TAG, report.toLogString())
        println(report.toLogString())
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
            val xmlContext = ContextThemeWrapper(testContext, Test_R.style.Theme_HikageBenchmarkTest)
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
        Log.i(TAG, report.toLogString())
        println(report.toLogString())
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
        Log.i(TAG, report.toLogString())
        println(report.toLogString())
    }

    private fun createXmlTree(context: Context) =
        context.layoutInflater.inflate(Test_R.layout.benchmark_210_view_tree, null, false)

    private fun createDemoXmlLayout(context: Context) =
        context.layoutInflater.inflate(Test_R.layout.benchmark_demo_layout, null, false)

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

    private fun createHikageTree(context: Context) = Hikageable(context) {
        LinearLayout {
            repeat(SECTION_COUNT) { benchmarkSection() }
        }
    }.root

    private fun createHikageTreeWithAttrs(context: Context) = Hikageable(context) {
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

    private fun createHikageTreeWithFullyAttrs(context: Context) = Hikageable(context) {
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

    private fun createHikageTreeWithFullViewAttrs(context: Context) = Hikageable(context) {
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

    private fun createDemoHikageLayout(context: Context) = Hikageable(context) {
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

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSection() {
        LinearLayout {
            benchmarkFirstRow()
            benchmarkSecondRow()
            benchmarkThirdRow()
        }
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSectionWithFullyAttrs() {
        LinearLayout(
            attrs = { benchmarkSectionAttrs() }
        ) {
            benchmarkFirstRowWithFullyAttrs()
            benchmarkSecondRowWithFullyAttrs()
            benchmarkThirdRowWithFullyAttrs()
        }
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRow() {
        benchmarkRow {
            TextView()
            Button()
            ImageView()
            ProgressBar()
            CheckBox()
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRowWithAttrs() {
        benchmarkRowWithAttrs {
            TextView(attrs = { benchmarkViewAttrs() })
            Button(attrs = { benchmarkViewAttrs() })
            ImageView(attrs = { benchmarkViewAttrs() })
            ProgressBar(attrs = { benchmarkViewAttrs() })
            CheckBox(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkFirstRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            TextView(attrs = { benchmarkTitleAttrs() })
            Button(attrs = { benchmarkActionAttrs() })
            ImageView(attrs = { benchmarkImageAttrs() })
            ProgressBar(attrs = { benchmarkProgressAttrs(35) })
            CheckBox(attrs = { benchmarkFlagAttrs() })
        }
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRow() {
        benchmarkRow {
            EditText()
            ImageButton()
            RatingBar()
            Switch()
            TextView()
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRowWithAttrs() {
        benchmarkRowWithAttrs {
            EditText(attrs = { benchmarkViewAttrs() })
            ImageButton(attrs = { benchmarkViewAttrs() })
            RatingBar(attrs = { benchmarkViewAttrs() })
            Switch(attrs = { benchmarkViewAttrs() })
            TextView(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkSecondRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            EditText(attrs = { benchmarkInputAttrs() })
            ImageButton(attrs = { benchmarkSearchAttrs() })
            RatingBar(attrs = { benchmarkRatingAttrs() })
            Switch(attrs = { benchmarkToggleAttrs() })
            TextView(attrs = { benchmarkValueAttrs() })
        }
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRow() {
        benchmarkRow {
            Space()
            TextView()
            Button()
            ProgressBar()
            CheckBox()
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRowWithAttrs() {
        benchmarkRowWithAttrs {
            Space(attrs = { benchmarkViewAttrs() })
            TextView(attrs = { benchmarkViewAttrs() })
            Button(attrs = { benchmarkViewAttrs() })
            ProgressBar(attrs = { benchmarkViewAttrs() })
            CheckBox(attrs = { benchmarkViewAttrs() })
        }
    }

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkThirdRowWithFullyAttrs() {
        benchmarkRowWithFullyAttrs {
            Space(attrs = { benchmarkSpaceAttrs() })
            TextView(attrs = { benchmarkDetailAttrs() })
            Button(attrs = { benchmarkDisabledAttrs() })
            ProgressBar(attrs = { benchmarkProgressAttrs(70) })
            CheckBox(attrs = { benchmarkReadyAttrs() })
        }
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRow(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            performer = performer
        )
    }

    @Hikageable
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

    @Hikageable
    private fun Hikage.Performer<LinearLayout.LayoutParams>.benchmarkRowWithFullyAttrs(
        performer: Hikage.Performer<LinearLayout.LayoutParams>.() -> Unit
    ) {
        LinearLayout(
            attrs = { benchmarkRowAttrs() },
            performer = performer
        )
    }

    @Hikageable
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

    private data class BenchmarkReport(
        val viewCount: Int,
        val warmupIterations: Int,
        val measuredIterations: Int,
        val xml: BenchmarkStats,
        val hikage: BenchmarkStats,
        val hikageWithAttrs: BenchmarkStats,
        val hikageWithFullyAttrs: BenchmarkStats,
        val hikageWithFullViewAttrs: BenchmarkStats
    ) {

        fun toLogString() =
            "210-view tree benchmark: views=$viewCount, warmup=$warmupIterations, iterations=$measuredIterations\n" +
                "XML                 ${xml.toLogString()}\n" +
                "Hikage              ${hikage.toLogString()}\n" +
                "Hikage with attrs   ${hikageWithAttrs.toLogString()}\n" +
                "Hikage fully attrs  ${hikageWithFullyAttrs.toLogString()}\n" +
                "Hikage full view attrs ${hikageWithFullViewAttrs.toLogString()}\n" +
                "ratio Hikage/XML             avg=${"%.2f".format(hikage.averageMs / xml.averageMs)}x, " +
                "median=${"%.2f".format(hikage.medianMs / xml.medianMs)}x\n" +
                "ratio HikageAttrs/XML        avg=${"%.2f".format(hikageWithAttrs.averageMs / xml.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithAttrs.medianMs / xml.medianMs)}x\n" +
                "ratio HikageAttrs/Hikage     avg=${"%.2f".format(hikageWithAttrs.averageMs / hikage.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithAttrs.medianMs / hikage.medianMs)}x\n" +
                "ratio HikageFullAttrs/XML    avg=${"%.2f".format(hikageWithFullyAttrs.averageMs / xml.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithFullyAttrs.medianMs / xml.medianMs)}x\n" +
                "ratio HikageFullAttrs/Hikage avg=${"%.2f".format(hikageWithFullyAttrs.averageMs / hikage.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithFullyAttrs.medianMs / hikage.medianMs)}x\n" +
                "ratio HikageFullViewAttrs/XML    avg=${"%.2f".format(hikageWithFullViewAttrs.averageMs / xml.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithFullViewAttrs.medianMs / xml.medianMs)}x\n" +
                "ratio HikageFullViewAttrs/Hikage avg=${"%.2f".format(hikageWithFullViewAttrs.averageMs / hikage.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithFullViewAttrs.medianMs / hikage.medianMs)}x\n" +
                "ratio FullAttrs/FullViewAttrs    avg=${"%.2f".format(hikageWithFullyAttrs.averageMs / hikageWithFullViewAttrs.averageMs)}x, " +
                "median=${"%.2f".format(hikageWithFullyAttrs.medianMs / hikageWithFullViewAttrs.medianMs)}x"
    }

    private data class BenchmarkStats(
        val minMs: Double,
        val medianMs: Double,
        val p90Ms: Double,
        val averageMs: Double,
        val maxMs: Double
    ) {

        fun toLogString() =
            "min=${formatMs(minMs)}ms, median=${formatMs(medianMs)}ms, p90=${formatMs(p90Ms)}ms, avg=${formatMs(averageMs)}ms, max=${formatMs(maxMs)}ms"

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
    ) {

        fun toLogString() =
            "demo layout benchmark: views=$viewCount, warmup=$warmupIterations, iterations=$measuredIterations\n" +
                "Demo XML    ${xml.toLogString()}\n" +
                "Demo Hikage ${hikage.toLogString()}\n" +
                "ratio DemoHikage/DemoXML avg=${"%.2f".format(hikage.averageMs / xml.averageMs)}x, " +
                "median=${"%.2f".format(hikage.medianMs / xml.medianMs)}x"
    }

    private data class TextInputLayoutBenchmarkReport(
        val viewCount: Int,
        val warmupIterations: Int,
        val measuredIterations: Int,
        val rounds: List<TextInputLayoutBenchmarkRound>
    ) {

        fun toLogString(): String {
            val lines = mutableListOf(
                "TextInputLayout creation benchmark: views=$viewCount, warmup=$warmupIterations, " +
                    "iterations=$measuredIterations, rounds=${rounds.size}"
            )
            rounds.forEachIndexed { index, round ->
                lines += "round ${index + 1} direct  ${round.direct.toLogString()}"
                lines += "round ${index + 1} reflect ${round.reflect.toLogString()}"
                lines += "round ${index + 1} ratio Reflect/Direct avg=${"%.2f".format(round.reflect.averageMs / round.direct.averageMs)}x, " +
                    "median=${"%.2f".format(round.reflect.medianMs / round.direct.medianMs)}x"
            }

            val slowest = rounds.flatMapIndexed { index, round ->
                listOf(
                    TextInputLayoutBenchmarkCandidate("round ${index + 1} direct", round.direct),
                    TextInputLayoutBenchmarkCandidate("round ${index + 1} reflect", round.reflect)
                )
            }.maxBy { it.stats.averageMs }
            lines += "slowest avg ${slowest.name} ${slowest.stats.toLogString()}"

            return lines.joinToString(separator = "\n")
        }
    }

    private data class TextInputLayoutBenchmarkRound(
        val direct: BenchmarkStats,
        val reflect: BenchmarkStats
    )

    private data class TextInputLayoutBenchmarkCandidate(
        val name: String,
        val stats: BenchmarkStats
    )
}