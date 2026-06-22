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
 * This file is created by fankes on 2026/6/1.
 */
@file:JvmName("LayoutLibUtils")

package com.highcapable.hikage.core.extension

import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.LayoutInflater.Factory2
import android.view.View
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.resolver.MethodResolver
import kotlin.reflect.KClass

/** The Android Studio layout preview inflater class name. */
private const val BRIDGE_INFLATER_CLASS_NAME = "BridgeInflater"

/** The `BridgeInflater.onCreateView` method resolver map. */
private val bridgeOnCreateViewResolvers = mutableMapOf<KClass<out LayoutInflater>, MethodResolver<LayoutInflater>?>()

/**
 * Create a new [View] from the Android Studio layout preview inflater.
 *
 * - Note: `BridgeInflater` does not always expose its custom inflater through [Factory2].
 * @receiver the layout inflater.
 * @param name the view name.
 * @param attrs the attribute set.
 * @return [View] or null.
 */
internal fun LayoutInflater.createViewFromBridgeInflaterOrNull(name: String, attrs: AttributeSet): View? {
    if (this::class.simpleName != BRIDGE_INFLATER_CLASS_NAME) return null

    val inflaterClass = this::class
    val resolver = if (bridgeOnCreateViewResolvers.containsKey(inflaterClass))
        bridgeOnCreateViewResolvers[inflaterClass]
    else run {
        val resolved = asResolver().optional(silent = true).firstMethodOrNull {
            this.name = "onCreateView"
            parameters(String::class, AttributeSet::class)
        }
        bridgeOnCreateViewResolvers[inflaterClass] = resolved

        resolved
    }

    return resolver?.copy()?.of(this)?.invokeQuietly<View>(name, attrs)
}