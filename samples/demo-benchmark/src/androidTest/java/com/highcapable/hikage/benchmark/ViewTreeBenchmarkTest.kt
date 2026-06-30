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
 * This file is created by fankes on 2026/6/30.
 */
package com.highcapable.hikage.benchmark

import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeatedOnMainThread
import androidx.core.view.setPadding
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
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
import com.highcapable.hikage.widget.com.google.android.material.chip.Chip
import com.highcapable.hikage.widget.com.google.android.material.chip.ChipGroup
import com.highcapable.hikage.widget.com.google.android.material.materialswitch.MaterialSwitch
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputEditText
import com.highcapable.hikage.widget.com.google.android.material.textfield.TextInputLayout
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.google.android.material.R as Material_R

/**
 * Measures XML inflation and Hikage view-tree construction with AndroidX Benchmark.
 */
@RunWith(AndroidJUnit4::class)
class ViewTreeBenchmarkTest {

    private companion object {

        const val SECTION_COUNT = 11
        const val VIEW_COUNT = 210
        const val DEMO_SECTION_COUNT = 11

        var viewSink = 0
    }

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Measures XML inflation for the 210-view stress tree.
     */
    @Test
    fun xml210ViewTreeCreation() = benchmarkViewTreeCreation(
        expectedViewCount = VIEW_COUNT
    ) {
        createXmlTree(context)
    }

    /**
     * Measures Hikage construction for the 210-view stress tree without dynamic attrs.
     */
    @Test
    fun hikage210ViewTreeCreation() = benchmarkViewTreeCreation(
        expectedViewCount = VIEW_COUNT
    ) {
        createHikageTree(context)
    }

    /**
     * Measures Hikage construction for the 210-view stress tree with light dynamic attrs.
     */
    @Test
    fun hikage210ViewTreeWithAttrsCreation() = benchmarkViewTreeCreation(
        expectedViewCount = VIEW_COUNT
    ) {
        createHikageTreeWithAttrs(context)
    }

    /**
     * Measures Hikage construction for the 210-view stress tree with all XML-equivalent attrs.
     */
    @Test
    fun hikage210ViewTreeWithFullyAttrsCreation() = benchmarkViewTreeCreation(
        expectedViewCount = VIEW_COUNT
    ) {
        createHikageTreeWithFullyAttrs(context)
    }

    /**
     * Measures Hikage construction for the 210-view stress tree with LayoutParams DSL plus view attrs.
     */
    @Test
    fun hikage210ViewTreeWithFullViewAttrsCreation() = benchmarkViewTreeCreation(
        expectedViewCount = VIEW_COUNT
    ) {
        createHikageTreeWithFullViewAttrs(context)
    }

    /**
     * Measures XML inflation for the Material-style demo layout.
     */
    @Test
    fun demoXmlLayoutCreation() = benchmarkViewTreeCreation(
        expectedViewCount = createViewOnMain { createDemoXmlLayout(demoContext()) }.countViews()
    ) {
        createDemoXmlLayout(demoContext())
    }

    /**
     * Measures Hikage construction for the Material-style demo layout.
     */
    @Test
    fun demoHikageLayoutCreation() {
        val demoContext = demoContext()
        val xmlViewCount = createViewOnMain { createDemoXmlLayout(demoContext) }.countViews()
        val hikageViewCount = createViewOnMain { createDemoHikageLayout(demoContext) }.countViews()

        assertEquals(xmlViewCount, hikageViewCount)

        benchmarkRule.measureRepeatedOnMainThread {
            createDemoHikageLayout(demoContext).consume()
        }
    }

    private fun benchmarkViewTreeCreation(expectedViewCount: Int, block: () -> View) {
        assertEquals(expectedViewCount, createViewOnMain(block).countViews())

        benchmarkRule.measureRepeatedOnMainThread {
            block().consume()
        }
    }

    private fun createViewOnMain(block: () -> View): View {
        var view: View? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            view = block()
        }

        return view ?: error("View creation on main thread failed.")
    }

    private fun demoContext() = ContextThemeWrapper(context, R.style.Theme_DefaultAppTheme)

    private fun createXmlTree(context: Context) =
        LayoutInflater.from(context).inflate(R.layout.benchmark_210_view_tree, null, false)

    private fun createDemoXmlLayout(context: Context) =
        LayoutInflater.from(context).cloneInContext(context).inflate(R.layout.benchmark_demo_layout, null, false)

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
                    Chip(
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
}