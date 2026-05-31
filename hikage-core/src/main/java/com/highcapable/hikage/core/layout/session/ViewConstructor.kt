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
 * This file is created by fankes on 2026/5/31.
 */
package com.highcapable.hikage.core.layout.session

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.highcapable.kavaref.resolver.ConstructorResolver

/**
 * The view constructor class.
 * @param resolver the constructor resolver.
 * @param parameterCount the parameter count.
 */
internal class ViewConstructor<V : View>(
    private val resolver: ConstructorResolver<V>,
    private val parameterCount: Int
) {

    /**
     * Build the view.
     * @param context the context.
     * @param attrs the attribute set.
     * @return [V] or null.
     */
    fun build(context: Context, attrs: Lazy<AttributeSet>) = when (parameterCount) {
        2 -> resolver.createQuietly(context, attrs.value)
        1 -> resolver.createQuietly(context)
        else -> null
    }
}