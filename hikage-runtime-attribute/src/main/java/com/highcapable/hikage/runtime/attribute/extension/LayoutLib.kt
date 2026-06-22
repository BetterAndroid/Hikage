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
 * This file is created by fankes on 2026/6/22.
 */
@file:JvmName("LayoutLibUtils")

package com.highcapable.hikage.runtime.attribute.extension

import android.content.Context
import android.content.ContextWrapper

/**
 * Whether the [Context] is provided by Android Studio layout preview.
 * @receiver the context.
 * @return [Boolean]
 */
internal fun Context.isLayoutPreview() = findLayoutPreviewContext() != null

/**
 * Find the Android Studio layout preview base context.
 * @receiver the context.
 * @return [Context] or null.
 */
internal fun Context.findLayoutPreviewContext(): Context? {
    var current: Context? = this
    while (current != null) {
        if (current.javaClass.simpleName == "BridgeContext") return current
        current = (current as? ContextWrapper)?.baseContext
    }

    return null
}