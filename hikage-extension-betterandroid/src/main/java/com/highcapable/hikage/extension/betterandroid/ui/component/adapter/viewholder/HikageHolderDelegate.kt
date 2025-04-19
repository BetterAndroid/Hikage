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
 * This file is created by fankes on 2025/3/10.
 */
package com.highcapable.hikage.extension.betterandroid.ui.component.adapter.viewholder

import android.content.Context
import android.view.ViewGroup
import com.highcapable.betterandroid.ui.component.adapter.viewholder.delegate.base.ViewHolderDelegate
import com.highcapable.hikage.core.Hikage

/**
 * [Hikage.Delegate] type view holder delegate.
 * @param delegate the [Hikage.Delegate] instance.
 */
class HikageHolderDelegate internal constructor(private val delegate: Hikage.Delegate<*>) : ViewHolderDelegate<Hikage>() {

    override fun create(context: Context, parent: ViewGroup?) = delegate.create(context, parent, attachToParent = false)
    override fun getView(instance: Hikage) = instance.root
}