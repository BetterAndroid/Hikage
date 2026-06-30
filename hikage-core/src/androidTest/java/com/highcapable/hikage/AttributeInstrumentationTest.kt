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
 * This file is created by fankes on 2026/6/9.
 */
package com.highcapable.hikage

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.highcapable.betterandroid.ui.extension.component.base.toPx
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.attribute.android
import com.highcapable.hikage.core.attribute.app
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.layout.LayoutParams
import com.highcapable.hikage.core.layout.View
import com.highcapable.hikage.core.layout.ViewGroup
import com.highcapable.hikage.widget.AttrProbeView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import com.highcapable.hikage.core.test.R as Test_R

/**
 * Verifies Hikage runtime attributes against framework attrs, custom attrs, and parent LayoutParams generation.
 */
@RunWith(AndroidJUnit4::class)
class AttributeInstrumentationTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val testContext get() = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun nativeCommonAttrsAreParsedByFramework() {
        val hikage = createHikage {
            View<TextView>(
                id = "text",
                attrs = {
                    android {
                        set("text", "Hikage")
                        set("textColor", Color.RED)
                        set("background", Color.YELLOW)
                        set("gravity", "center")
                        set("visibility", "invisible")
                        set("textStyle", "bold|italic")
                        set("inputType", "textMultiLine|textNoSuggestions")
                        set("imeOptions", "actionDone|flagNoFullscreen")
                        set("scrollbars", "horizontal|vertical")
                    }
                }
            )
        }

        val view = hikage.get<TextView>("text")
        assertEquals("Hikage", view.text.toString())
        assertEquals(Color.RED, view.currentTextColor)
        assertEquals(Color.YELLOW, (view.background as ColorDrawable).color)
        assertEquals(Gravity.CENTER, view.gravity)
        assertEquals(View.INVISIBLE, view.visibility)
        assertEquals(Typeface.BOLD_ITALIC, view.typeface.style)
        assertEquals(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS, view.inputType)
        assertEquals(EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_FULLSCREEN, view.imeOptions)
        assertTrue(view.isHorizontalScrollBarEnabled)
        assertTrue(view.isVerticalScrollBarEnabled)
    }

    @Test
    fun nativeFrameworkSymbolsCoverBuiltInAndBagFallback() {
        val hikage = createHikage(testContext) {
            View<AutoCompleteTextView>(
                id = "autoComplete",
                attrs = {
                    android {
                        set("dropDownWidth", "match_parent")
                        set("dropDownHeight", "wrap_content")
                    }
                }
            )
            View<GridView>(
                id = "grid",
                attrs = {
                    android {
                        set("numColumns", "auto_fit")
                    }
                }
            )
            ViewGroup<LinearLayout, LinearLayout.LayoutParams>(
                id = "linear",
                attrs = {
                    android {
                        set("orientation", "vertical")
                        set("showDividers", "middle")
                    }
                }
            )
            View<TextView>(
                id = "marquee",
                attrs = {
                    android {
                        set("marqueeRepeatLimit", "marquee_forever")
                    }
                }
            )
            View<AttrProbeView>(
                id = "frameworkProbe",
                attrs = {
                    android {
                        set("layout_width", "match_parent")
                        set("layout_height", "wrap_content")
                        set("scrollbarStyle", "outsideInset")
                        set("requiresFadingEdge", "horizontal|vertical")
                        set("overScrollMode", "never")
                        set("verticalScrollbarPosition", "left")
                    }
                }
            )
        }

        val autoComplete = hikage.get<AutoCompleteTextView>("autoComplete")
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, autoComplete.dropDownWidth)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, autoComplete.dropDownHeight)
        assertEquals(GridView.AUTO_FIT, hikage.get<GridView>("grid").numColumns)
        assertEquals(LinearLayout.VERTICAL, hikage.get<LinearLayout>("linear").orientation)
        assertEquals(LinearLayout.SHOW_DIVIDER_MIDDLE, hikage.get<LinearLayout>("linear").showDividers)
        assertEquals(-1, hikage.get<TextView>("marquee").marqueeRepeatLimit)

        val probe = hikage.get<AttrProbeView>("frameworkProbe")
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, probe.androidLayoutWidth)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, probe.androidLayoutHeight)
        assertEquals(0x03000000, probe.androidScrollbarStyle)
        assertEquals(0x00003000, probe.androidRequiresFadingEdge)
        assertEquals(2, probe.androidOverScrollMode)
        assertEquals(1, probe.androidVerticalScrollbarPosition)
    }

    @Test
    fun customAttrsCoverRawValuesAndResourceBagSymbols() {
        val hikage = createHikage(testContext) {
            View<AttrProbeView>(
                id = "custom",
                attrs = {
                    app {
                        set("hikageProbeMode", "secondary")
                        set("hikageProbeFlags", "alpha|gamma")
                        set("hikageProbeInt", "0x2a")
                        set("hikageProbeDimension", "12dp")
                        set("hikageProbeColor", "#123456")
                        set("hikageProbeBoolean", true)
                        set("hikageProbeString", Test_R.string.hikage_probe_text)
                        set("hikageProbeReference", "@array/hikage_probe_strings")
                        set("hikageProbeFraction", "50%")
                        set("hikageProbeFloat", "1.5")
                    }
                }
            )
            View<AttrProbeView>(
                id = "customRawEnum",
                attrs = {
                    app {
                        set("hikageProbeMode", -1)
                        set("hikageProbeFlags", 7)
                    }
                }
            )
        }

        val probe = hikage.get<AttrProbeView>("custom")
        assertEquals(2, probe.mode)
        assertEquals(1 or 4, probe.flags)
        assertEquals(42, probe.intValue)
        assertEquals(12.toPx(context), probe.dimensionPx)
        assertEquals(Color.rgb(0x12, 0x34, 0x56), probe.color)
        assertTrue(probe.boolean)
        assertEquals(testContext.getString(Test_R.string.hikage_probe_text), probe.string)
        assertEquals(Test_R.array.hikage_probe_strings, probe.reference)
        assertEquals(0.5f, probe.fraction, 0.001f)
        assertEquals(1.5f, probe.floatValue, 0.001f)

        val rawEnumProbe = hikage.get<AttrProbeView>("customRawEnum")
        assertEquals(-1, rawEnumProbe.mode)
        assertEquals(7, rawEnumProbe.flags)
    }

    @Test
    fun layoutParamsAreGeneratedFromAttrsByParentViewGroup() {
        val hikage = createHikage {
            View<TextView>(
                id = "frameChild",
                attrs = {
                    android {
                        set("layout_width", "match_parent")
                        set("layout_height", "48dp")
                        set("layout_gravity", "center")
                    }
                }
            )
            View<TextView>(
                id = "dynamicSizeChild",
                lparams = LayoutParams(),
                attrs = {
                    android {
                        set("layout_width", "match_parent")
                        set("layout_height", "48dp")
                        set("layout_gravity", "center")
                    }
                }
            )
            View<TextView>(
                id = "bodyChild",
                lparams = LayoutParams {
                    gravity = Gravity.BOTTOM
                },
                attrs = {
                    android {
                        set("layout_width", "match_parent")
                        set("layout_height", "48dp")
                        set("layout_gravity", "center")
                    }
                }
            )
            ViewGroup<LinearLayout, LinearLayout.LayoutParams>(
                id = "linear",
                init = { orientation = LinearLayout.VERTICAL }
            ) {
                View<TextView>(
                    id = "linearChild",
                    attrs = {
                        android {
                            set("layout_width", "match_parent")
                            set("layout_height", "0dp")
                            set("layout_weight", "1")
                            set("layout_gravity", "center_horizontal")
                        }
                    }
                )
            }
            ViewGroup<LinearLayoutCompat, LinearLayoutCompat.LayoutParams>(
                id = "linearCompat",
                init = { orientation = LinearLayoutCompat.VERTICAL }
            ) {
                View<TextView>(
                    id = "linearCompatChild",
                    lparams = LayoutParams(widthMatchParent = true, height = 0) {
                        weight = 1f
                    }
                )
            }
        }

        val frameChildParams = hikage.get<TextView>("frameChild").layoutParams as FrameLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, frameChildParams.width)
        assertEquals(48.toPx(context), frameChildParams.height)
        assertEquals(Gravity.CENTER, frameChildParams.gravity)

        val dynamicSizeParams = hikage.get<TextView>("dynamicSizeChild").layoutParams as FrameLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, dynamicSizeParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, dynamicSizeParams.height)
        assertEquals(-1, dynamicSizeParams.gravity)

        val bodyParams = hikage.get<TextView>("bodyChild").layoutParams as FrameLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, bodyParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, bodyParams.height)
        assertEquals(Gravity.BOTTOM, bodyParams.gravity)

        val linearChildParams = hikage.get<TextView>("linearChild").layoutParams as LinearLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, linearChildParams.width)
        assertEquals(0, linearChildParams.height)
        assertEquals(1f, linearChildParams.weight, 0.001f)
        assertEquals(Gravity.CENTER_HORIZONTAL, linearChildParams.gravity)

        val linearCompatChildParams = hikage.get<TextView>("linearCompatChild").layoutParams
        assertTrue(linearCompatChildParams is LinearLayoutCompat.LayoutParams)
        linearCompatChildParams as LinearLayoutCompat.LayoutParams
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, linearCompatChildParams.width)
        assertEquals(0, linearCompatChildParams.height)
        assertEquals(1f, linearCompatChildParams.weight, 0.001f)
    }

    @Test
    fun coordinatorLayoutParamsAreGeneratedAndOverridden() {
        val themedContext = ContextThemeWrapper(testContext, Test_R.style.Theme_HikageCoreTest)
        val hikage = createHikage(themedContext) {
            ViewGroup<CoordinatorLayout, CoordinatorLayout.LayoutParams>(
                id = "coordinator",
                attrs = {
                    android {
                        set("layout_width", "match_parent")
                        set("layout_height", "match_parent")
                    }
                }
            ) {
                View<TextView>(
                    id = "attrsFab",
                    attrs = {
                        android {
                            set("layout_width", "wrap_content")
                            set("layout_height", "wrap_content")
                            set("layout_gravity", "bottom|end")
                            set("layout_marginEnd", "16dp")
                            set("layout_marginBottom", "16dp")
                        }
                    }
                )
                ViewGroup<AppBarLayout, AppBarLayout.LayoutParams>(
                    id = "attrsAppBar",
                    attrs = {
                        android {
                            set("layout_width", "match_parent")
                            set("layout_height", "wrap_content")
                        }
                    }
                ) {
                    View<MaterialToolbar>(
                        id = "attrsToolbar",
                        attrs = {
                            android {
                                set("layout_width", "match_parent")
                                set("layout_height", "56dp")
                            }
                        }
                    )
                }
                View<FloatingActionButton>(
                    id = "materialAttrsFab",
                    attrs = {
                        android {
                            set("layout_width", "wrap_content")
                            set("layout_height", "wrap_content")
                            set("layout_gravity", "bottom|end")
                            set("layout_marginEnd", "16dp")
                            set("layout_marginBottom", "16dp")
                        }
                    }
                )
                View<TextView>(
                    id = "bodyFab",
                    lparams = LayoutParams {
                        gravity = Gravity.BOTTOM or Gravity.END
                        marginEnd = 16.dp
                        bottomMargin = 16.dp
                    }
                )
                View<TextView>(
                    id = "mixedFab",
                    lparams = LayoutParams {
                        gravity = Gravity.BOTTOM or Gravity.END
                        marginEnd = 16.dp
                        bottomMargin = 16.dp
                    },
                    attrs = {
                        android {
                            set("layout_width", "wrap_content")
                            set("layout_height", "wrap_content")
                        }
                    }
                )
                View<TextView>(
                    id = "mixedIgnoredAttrsFab",
                    lparams = LayoutParams(width = 50.dp),
                    attrs = {
                        android {
                            set("layout_width", "match_parent")
                            set("layout_height", "match_parent")
                            set("layout_gravity", "bottom|end")
                            set("layout_marginEnd", "16dp")
                            set("layout_marginBottom", "16dp")
                        }
                    }
                )
            }
        }

        val attrsParams = hikage.get<TextView>("attrsFab").layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, attrsParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, attrsParams.height)
        assertEquals(Gravity.BOTTOM or Gravity.END, attrsParams.gravity)
        assertEquals(16.toPx(context), attrsParams.marginEnd)
        assertEquals(16.toPx(context), attrsParams.bottomMargin)

        val appBar = hikage.get<AppBarLayout>("attrsAppBar")
        val appBarParams = appBar.layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, appBarParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, appBarParams.height)

        val toolbarParams = hikage.get<MaterialToolbar>("attrsToolbar").layoutParams as AppBarLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, toolbarParams.width)
        assertEquals(56.toPx(context), toolbarParams.height)

        val materialAttrsParams = hikage.get<FloatingActionButton>("materialAttrsFab").layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, materialAttrsParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, materialAttrsParams.height)
        assertEquals(Gravity.BOTTOM or Gravity.END, materialAttrsParams.gravity)
        assertEquals(16.toPx(context), materialAttrsParams.marginEnd)
        assertEquals(16.toPx(context), materialAttrsParams.bottomMargin)

        val bodyParams = hikage.get<TextView>("bodyFab").layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, bodyParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, bodyParams.height)
        assertEquals(Gravity.BOTTOM or Gravity.END, bodyParams.gravity)
        assertEquals(16.toPx(context), bodyParams.marginEnd)
        assertEquals(16.toPx(context), bodyParams.bottomMargin)

        val mixedParams = hikage.get<TextView>("mixedFab").layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, mixedParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, mixedParams.height)
        assertEquals(Gravity.BOTTOM or Gravity.END, mixedParams.gravity)
        assertEquals(16.toPx(context), mixedParams.marginEnd)
        assertEquals(16.toPx(context), mixedParams.bottomMargin)

        val mixedIgnoredAttrsParams = hikage.get<TextView>("mixedIgnoredAttrsFab").layoutParams as CoordinatorLayout.LayoutParams
        assertEquals(50.toPx(context), mixedIgnoredAttrsParams.width)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, mixedIgnoredAttrsParams.height)
        assertEquals(0, mixedIgnoredAttrsParams.gravity)
        assertEquals(0, mixedIgnoredAttrsParams.marginEnd)
        assertEquals(0, mixedIgnoredAttrsParams.bottomMargin)
    }

    @Test
    fun childViewsUseParentWrappedContextDuringRender() {
        val themedContext = ContextThemeWrapper(testContext, Test_R.style.Theme_HikageCoreTest)
        val hikage = createHikage(themedContext) {
            ViewGroup<TextInputLayout, LinearLayout.LayoutParams>(id = "textInputLayout") {
                View<TextInputEditText>(id = "textInputEditText")
            }
        }

        val textInputLayout = hikage.get<TextInputLayout>("textInputLayout")
        val textInputEditText = hikage.get<TextInputEditText>("textInputEditText")
        assertSame(textInputLayout.context, textInputEditText.context)
    }

    private fun createHikage(
        context: Context = this.context,
        performer: HikagePerformer<FrameLayout.LayoutParams>
    ): Hikage {
        val parent = FrameLayout(context)
        return Hikage.create(context, parent, attachToParent = true, performer = performer)
    }
}