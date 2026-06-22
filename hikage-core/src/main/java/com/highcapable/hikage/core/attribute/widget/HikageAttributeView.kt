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
package com.highcapable.hikage.core.attribute.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.highcapable.betterandroid.ui.extension.view.inflateOrNull
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.R
import com.highcapable.hikage.core.attribute.exception.AttributeResolvingException
import com.highcapable.hikage.core.extension.identityKey
import java.util.concurrent.ConcurrentHashMap

/**
 * A [View] used to resolve a simple [AttributeSet] from the layout.
 *
 * This view is used internally by the [Hikage] to resolve attributes from the layout.
 */
internal class HikageAttributeView(
    context: Context,
    val attrs: AttributeSet
) : View(context, attrs) {

    internal companion object {

        private var cachedAttributeSets = ConcurrentHashMap<String, AttributeSet>()

        /**
         * Resolve a simple [AttributeSet] from the layout.
         * @param context the context.
         * @return [AttributeSet]
         */
        fun resolveSimpleAttributeSet(context: Context) = cachedAttributeSets.getOrPut(context.identityKey) {
            context.layoutInflater.inflateOrNull<HikageAttributeView>(
                R.layout.layout_hikage_attribute_node
            )?.attrs ?: throw AttributeResolvingException("Failed to resolve simple AttributeSet.")
        } ?: error("Internal error.")
    }
}