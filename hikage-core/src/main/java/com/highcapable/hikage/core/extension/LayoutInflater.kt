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
 * This file is created by fankes on 2026/7/5.
 */
@file:JvmName("LayoutInflaterUtils")

package com.highcapable.hikage.core.extension

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.LayoutInflater.Factory2
import android.view.View
import android.view.ViewGroup
import com.highcapable.betterandroid.ui.extension.component.hostActivity
import com.highcapable.hikage.core.Hikage
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.resolver.FieldResolver
import kotlin.reflect.KClass

/** The `LayoutInflater.mPrivateFactory` field name. */
private const val PRIVATE_FACTORY_FIELD_NAME = "mPrivateFactory"

/** The `LayoutInflater.mPrivateFactory` field resolver map. */
private val privateFactoryResolvers = mutableMapOf<KClass<out LayoutInflater>, FieldResolver<LayoutInflater>?>()

/**
 * Create a new [View] from the [LayoutInflater] factory pipeline.
 *
 * This mirrors the factory part of AOSP `LayoutInflater.tryCreateView`, whose order is
 * `Factory2 -> Factory -> PrivateFactory`. [Hikage] creates views from DSL nodes instead of XML tags,
 * so it needs to explicitly replay this step before falling back to direct constructors.
 * @receiver the layout inflater.
 * @param parent the parent view group.
 * @param name the view name.
 * @param fullName the view full name.
 * @param context the inflation context.
 * @param attrs the attribute set.
 * @param processPrivateFactory whether to process the private factory.
 * @param processViews the list of [View] classes to process.
 * @return [View] or null.
 */
internal fun LayoutInflater.createViewFromInflaterOrNull(
    parent: ViewGroup?,
    name: String,
    fullName: String,
    context: Context,
    attrs: AttributeSet,
    processPrivateFactory: Boolean,
    processViews: List<String>
): View? {
    var view = factory2?.onCreateView(parent, name, context, attrs)
    if (view == null && factory2 == null) view = factory?.onCreateView(name, context, attrs)
    if (view == null && processPrivateFactory && (processViews.isEmpty() || processViews.contains(fullName))) {
        val privateFactory = privateFactoryOrNull()
        view = if (privateFactory != null)
            privateFactory.onCreateView(parent, name, context, attrs)
        else context.hostActivity?.onCreateView(parent, name, context, attrs)
    }

    return view
}

/** Get the private factory from this [LayoutInflater]. */
private fun LayoutInflater.privateFactoryOrNull(): Factory2? {
    val inflaterClass = this::class
    val resolver = privateFactoryResolvers.getOrPut(inflaterClass) {
        asResolver().optional(silent = true).firstFieldOrNull {
            name = PRIVATE_FACTORY_FIELD_NAME
        }
    }

    return resolver?.copy()?.of(this)?.getQuietly<Factory2>()
}