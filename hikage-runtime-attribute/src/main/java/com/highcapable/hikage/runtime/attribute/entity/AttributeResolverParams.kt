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
package com.highcapable.hikage.runtime.attribute.entity

import android.content.Context
import androidx.annotation.LayoutRes
import com.highcapable.hikage.runtime.attribute.resolver.XmlBlockResolver
import com.highcapable.hikage.runtime.attribute.resolver.XmlResourceParserResolver

/**
 * The parameters for creating a new [XmlResourceParserResolver].
 * @param sourceResId the source layout resource ID, available on [XmlBlockResolver] only.
 * @param resourcePackageName the package name used for resolving `app` namespaces and unqualified resource references.
 */
data class AttributeResolverParams(
    @field:LayoutRes val sourceResId: Int? = null,
    val resourcePackageName: String = ""
) {

    /**
     * Resolve the actual resource package name from [context].
     * @param context the context.
     * @return [String]
     */
    internal fun resourcePackageName(context: Context) = resourcePackageName.ifBlank { context.packageName }
}