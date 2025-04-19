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
 * This file is created by fankes on 2025/3/6.
 */
@file:Suppress("unused")
@file:JvmName("HikageBuilderUtils")

package com.highcapable.hikage.core.builder

import android.content.Context
import android.view.ViewGroup
import com.highcapable.hikage.core.Hikage

/**
 * Lazy initialize a [Hikage] layout.
 *
 * Usage:
 *
 * ```kotlin
 * object MainLayout : HikageBuilder {
 *
 *    override fun build() = Hikageable {
 *        LinearLayout(
 *            lparams = LayoutParams(matchParent = true),
 *            init = {
 *                orientation = LinearLayout.VERTICAL
 *                gravity = Gravity.CENTER
 *            }
 *       ) {
 *            TextView {
 *                text = "Hello, Hikage!"
 *                textSize = 20f
 *            }
 *       }
 *    }
 * }
 *
 * class MainActivity : AppCompatActivity() {
 *
 *     private val hikage by lazyHikage(MainLayout)
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(hikage)
 *     }
 * }
 * ```
 * @receiver the context to create the layout.
 * @param builder the [HikageBuilder] instance.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @return [Lazy]<[Hikage]>
 */
@JvmSynthetic
fun Context.lazyHikage(
    builder: HikageBuilder,
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null
) = lazy { builder.build().create(context = this, parent, attachToParent) }

/**
 * A layout builder interface for [Hikage].
 *
 * Checking [build] for usage.
 */
interface HikageBuilder {

    /**
     * Provide a [Hikage] for builder.
     *
     * Usage:
     *
     * ```kotlin
     * override fun build() = Hikageable {
     *     TextView(
     *         lparams = LayoutParams(
     *             width = 100.dp,
     *             height = 100.dp
     *         )
     *     ) {
     *         text = "Hello, Hikage!"
     *         textSize = 20f
     *     }
     * }
     * ```
     * @return [Hikage.Delegate]
     */
    fun build(): Hikage.Delegate<*>
}