/*
 * Hikage - A Kotlin DSL-based Android real-time UI building framework.
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
 * This file is created by fankes on 2026/6/3.
 */
package com.highcapable.hikage.core.attrs.runtime.entity

import com.highcapable.hikage.core.attrs.entity.AttributeItem

/**
 * A parser-independent resolved attribute.
 */
internal interface ResolvedAttribute {

    /** The source attribute item. */
    val item: AttributeItem

    /** The namespace URI used by XML parsers. */
    val namespaceUri: String

    /** The attribute resource id. */
    val resourceId: Int
}