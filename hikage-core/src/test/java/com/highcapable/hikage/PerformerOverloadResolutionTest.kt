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
 * This file is created by fankes on 2026/8/9.
 */
package com.highcapable.hikage

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.layout.LayoutParams
import com.highcapable.hikage.core.layout.View
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Verifies the base and typed View performer overloads remain source compatible.
 */
class PerformerOverloadResolutionTest {

    @Test
    fun baseAndTypedViewCallsResolveUnambiguously() {
        val baseView: Hikage.Performer<ViewGroup.LayoutParams>.() -> View = {
            View(
                id = "divider",
                lparams = LayoutParams(widthMatchParent = true)
            )
        }
        val typedView: Hikage.Performer<ViewGroup.LayoutParams>.() -> TextView = {
            View<TextView>()
        }

        assertNotNull(baseView)
        assertNotNull(typedView)
    }
}