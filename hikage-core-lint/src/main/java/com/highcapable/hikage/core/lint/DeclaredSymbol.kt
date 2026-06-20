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
 * This file is created by fankes on 2025/3/17.
 */
package com.highcapable.hikage.core.lint

object DeclaredSymbol {

    const val HIKAGEABLE_ANNOTATION_CLASS = "com.highcapable.hikage.annotation.Hikageable"
    const val HIKAGE_VIEW_ANNOTATION_CLASS = "com.highcapable.hikage.annotation.HikageView"
    const val HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS = "com.highcapable.hikage.annotation.HikageViewDeclaration"
    const val HIKAGE_CLASS = "com.highcapable.hikage.core.Hikage"
    const val HIKAGE_PERFORMER_CLASS = "com.highcapable.hikage.core.Hikage.Performer"

    val HIKAGE_VIEW_REGEX = "kotlin.jvm.functions.Function1<\\?\\s*super\\s+[^,]+,kotlin.Unit>".toRegex()
}