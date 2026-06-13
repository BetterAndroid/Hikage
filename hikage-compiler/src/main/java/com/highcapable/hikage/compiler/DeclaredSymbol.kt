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
 * This file is created by fankes on 2025/3/23.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.hikage.compiler

object DeclaredSymbol {

    const val ANNOTATION_PACKAGE_NAME = "com.highcapable.hikage.annotation"

    const val HIKAGEABLE_ANNOTATION_CLASS_NAME = "Hikageable"
    const val HIKAGEABLE_ANNOTATION_CLASS = "$ANNOTATION_PACKAGE_NAME.$HIKAGEABLE_ANNOTATION_CLASS_NAME"
    const val HIKAGE_VIEW_ANNOTATION_CLASS_NAME = "HikageView"
    const val HIKAGE_VIEW_ANNOTATION_CLASS = "$ANNOTATION_PACKAGE_NAME.$HIKAGE_VIEW_ANNOTATION_CLASS_NAME"
    const val HIKAGE_HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS_NAME = "HikageViewDeclaration"
    const val HIKAGE_HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS = "$ANNOTATION_PACKAGE_NAME.$HIKAGE_HIKAGE_VIEW_DECLARATION_ANNOTATION_CLASS_NAME"

    const val ANDROID_VIEW_PACKAGE_NAME = "android.view"

    const val ANDROID_VIEW_CLASS = "$ANDROID_VIEW_PACKAGE_NAME.View"
    const val ANDROID_VIEW_GROUP_CLASS_NAME = "ViewGroup"
    const val ANDROID_VIEW_GROUP_CLASS = "$ANDROID_VIEW_PACKAGE_NAME.$ANDROID_VIEW_GROUP_CLASS_NAME"

    const val ANDROID_LAYOUT_PARAMS_CLASS_NAME = "ViewGroup.LayoutParams"
    const val ANDROID_LAYOUT_PARAMS_CLASS = "$ANDROID_VIEW_PACKAGE_NAME.$ANDROID_LAYOUT_PARAMS_CLASS_NAME"

    const val HIKAGE_CORE_PACKAGE_NAME = "com.highcapable.hikage.core"

    const val HIKAGE_CLASS_NAME = "Hikage"
    const val HIKAGE_CLASS = "$HIKAGE_CORE_PACKAGE_NAME.$HIKAGE_CLASS_NAME"

    const val HIKAGE_LAYOUT_PACKAGE_NAME = "$HIKAGE_CORE_PACKAGE_NAME.layout"
    const val HIKAGE_ATTRS_PACKAGE_NAME = "$HIKAGE_CORE_PACKAGE_NAME.attrs"

    const val HIKAGE_PERFORMER_CLASS_NAME = "Hikage.Performer"
    const val HIKAGE_PERFORMER_CLASS = "$HIKAGE_CORE_PACKAGE_NAME.$HIKAGE_PERFORMER_CLASS_NAME"

    const val HIKAGE_LAYOUT_PARAMS_CLASS_NAME = "LayoutParams"
    const val HIKAGE_LAYOUT_PARAMS_CLASS = "$HIKAGE_LAYOUT_PACKAGE_NAME.$HIKAGE_LAYOUT_PARAMS_CLASS_NAME"

    const val HIKAGE_BASE_PACKAGE_NAME = "com.highcapable.hikage.core.base"

    const val HIKAGE_VIEW_LAMBDA_CLASS_NAME = "HikageView"
    const val HIKAGE_VIEW_LAMBDA_CLASS = "$HIKAGE_BASE_PACKAGE_NAME.$HIKAGE_VIEW_LAMBDA_CLASS_NAME"

    const val HIKAGE_PERFORMER_LAMBDA_CLASS_NAME = "HikagePerformer"
    const val HIKAGE_PERFORMER_LAMBDA_CLASS = "$HIKAGE_BASE_PACKAGE_NAME.$HIKAGE_PERFORMER_LAMBDA_CLASS_NAME"

    const val HIKAGE_ATTRIBUTE_LAMBDA_CLASS_NAME = "HikageAttribute"
    const val HIKAGE_ATTRIBUTE_LAMBDA_CLASS = "$HIKAGE_ATTRS_PACKAGE_NAME.$HIKAGE_ATTRIBUTE_LAMBDA_CLASS_NAME"
}