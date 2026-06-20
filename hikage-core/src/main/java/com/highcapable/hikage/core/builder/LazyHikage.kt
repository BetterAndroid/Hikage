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
 * This file is created by fankes on 2026/6/16.
 */
@file:Suppress("unused")
@file:JvmName("LazyHikageUtils")

package com.highcapable.hikage.core.builder

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.base.HikageFactory
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.Hikageable

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
 * Lazy initialize a [Hikage] layout.
 *
 * Usage:
 *
 * ```kotlin
 * class MainActivity : AppCompatActivity() {
 *
 *     private val hikage by lazyHikage {
 *         LinearLayout(
 *              lparams = LayoutParams(matchParent = true),
 *              init = {
 *                  orientation = LinearLayout.VERTICAL
 *                  gravity = Gravity.CENTER
 *              }
 *         ) {
 *              TextView {
 *                  text = "Hello, Hikage!"
 *                  textSize = 20f
 *              }
 *         }
 *     }
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(hikage)
 *     }
 * }
 * ```
 * @receiver the context to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Lazy]<[Hikage]>
 */
@JvmSynthetic
fun Context.lazyHikage(
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) = lazyHikage<ViewGroup.LayoutParams>(parent, attachToParent, factory, performer)

/**
 * Lazy initialize a [Hikage] layout.
 * @receiver the context to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Lazy]<[Hikage]>
 */
@JvmName("lazyHikageTyped")
inline fun <reified LP : ViewGroup.LayoutParams> Context.lazyHikage(
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    noinline factory: HikageFactoryBuilder.() -> Unit = {},
    noinline performer: HikagePerformer<LP>
) = lazy {
    Hikageable<LP>(
        context = this,
        parent = parent,
        attachToParent = attachToParent,
        factory = factory,
        performer = performer
    )
}

/**
 * Lazy initialize a [Hikage] layout.
 *
 * Usage:
 *
 * ```kotlin
 * class MainFragment : Fragment() {
 *
 *     private val hikage by lazyHikage {
 *         LinearLayout(
 *              lparams = LayoutParams(matchParent = true),
 *              init = {
 *                  orientation = LinearLayout.VERTICAL
 *                  gravity = Gravity.CENTER
 *              }
 *         ) {
 *              TextView {
 *                  text = "Hello, Hikage!"
 *                  textSize = 20f
 *              }
 *         }
 *     }
 *
 *    override fun onCreateView(
 *        inflater: LayoutInflater,
 *        container: ViewGroup?,
 *        savedInstanceState: Bundle?
 *    ) = hikage.root
 * }
 * ```
 * @receiver the [Fragment] to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Lazy]<[Hikage]>
 */
@JvmSynthetic
fun Fragment.lazyHikage(
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    factory: HikageFactoryBuilder.() -> Unit = {},
    performer: HikagePerformer<ViewGroup.LayoutParams>
) = lazyHikage<ViewGroup.LayoutParams>(parent, attachToParent, factory, performer)

/**
 * Lazy initialize a [Hikage] layout.
 * @receiver the [Fragment.requireContext] to create the layout.
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
 * @param factory the [HikageFactory] builder.
 * @param performer the performer body.
 * @return [Lazy]<[Hikage]>
 */
@JvmName("lazyHikageTyped")
inline fun <reified LP : ViewGroup.LayoutParams> Fragment.lazyHikage(
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null,
    noinline factory: HikageFactoryBuilder.() -> Unit = {},
    noinline performer: HikagePerformer<LP>
) = lazy {
    Hikageable<LP>(
        context = requireContext(),
        parent = parent,
        attachToParent = attachToParent,
        factory = factory,
        performer = performer
    )
}