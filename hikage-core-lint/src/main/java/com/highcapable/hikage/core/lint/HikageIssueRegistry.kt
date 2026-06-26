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
 * This file is created by fankes on 2025/3/14.
 */
@file:Suppress("unused")

package com.highcapable.hikage.core.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.highcapable.hikage.core.lint.detector.GeneratedHikagePerformerDetector
import com.highcapable.hikage.core.lint.detector.HikagableBeyondScopeDetector
import com.highcapable.hikage.core.lint.detector.HikagablePropagationDetector
import com.highcapable.hikage.core.lint.detector.HikageAttributeDetector
import com.highcapable.hikage.core.lint.detector.HikageResourcesScopeDetector
import com.highcapable.hikage.core.lint.detector.HikageSafeTypeCastDetector
import com.highcapable.hikage.generated.HikageProperties

class HikageIssueRegistry : IssueRegistry() {

    override val issues get() = listOf(
        HikageAttributeDetector.MISSING_NAMESPACE_ISSUE,
        HikageAttributeDetector.DUPLICATE_ISSUE,
        HikageAttributeDetector.NAMESPACE_ISSUE,
        HikageAttributeDetector.INEFFECTIVE_LAYOUT_ATTRIBUTE_ISSUE,
        HikageAttributeDetector.INVALID_NAME_ISSUE,
        HikageAttributeDetector.INVALID_RESOURCE_REFERENCE_ISSUE,
        HikageAttributeDetector.INVALID_COLOR_VALUE_ISSUE,
        HikageAttributeDetector.TOO_LONG_STRING_ISSUE,
        HikagableBeyondScopeDetector.ISSUE,
        HikagablePropagationDetector.ISSUE,
        HikageSafeTypeCastDetector.ISSUE,
        HikageResourcesScopeDetector.ISSUE,
        GeneratedHikagePerformerDetector.ISSUE
    )

    override val minApi = HikageProperties.PROJECT_HIKAGE_CORE_LINT_MIN_API
    override val api = CURRENT_API
    override val vendor = Vendor(
        vendorName = HikageProperties.PROJECT_NAME,
        identifier = HikageProperties.PROJECT_HIKAGE_CORE_LINT_IDENTIFIER,
        feedbackUrl = "${HikageProperties.PROJECT_URL}/issues",
        contact = HikageProperties.PROJECT_URL
    )
}