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
package com.highcapable.hikage

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.layout.View
import com.highcapable.hikage.core.layout.ViewGroup
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies [Hikage] runtime behavior on a real Android device.
 */
@RunWith(AndroidJUnit4::class)
class CoreInstrumentationTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun attachedParentIsNotHikageRoot() {
        val parent = FrameLayout(context)
        val hikage = Hikage.create(context, parent, attachToParent = true) {
            ViewGroup<LinearLayout, ViewGroup.LayoutParams> {
                View<TextView> {
                    text = "root"
                }
            }
        }

        assertNotSame(parent, hikage.root)
        assertTrue(hikage.root is LinearLayout)
        assertSame(parent, hikage.root.parent)
    }
}