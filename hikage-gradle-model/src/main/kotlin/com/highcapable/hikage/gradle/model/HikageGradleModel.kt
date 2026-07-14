/*
 * Hikage - A real-time Android View runtime powered by Kotlin DSL.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0 (the "License");
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
 * This file is created by fankes on 2026/7/14.
 */
package com.highcapable.hikage.gradle.model

import java.io.Serializable

/**
 * The Hikage Gradle configuration synchronized to IDE tooling.
 *
 * The declaration paths are the exact values supplied to the corresponding
 * Hikage KSP options after Gradle has finalized this project's configuration.
 */
interface HikageGradleModel : Serializable {

    /** Whether the Gradle plugin is applied to this project. */
    val isPluginApplied: Boolean

    /** Whether the KSP integration is enabled for this project. */
    val isCompilerEnabled: Boolean

    /** Strict declaration files or directories supplied through `hikage.viewDeclarationFiles`. */
    val viewDeclarationFiles: List<String>

    /** Optional declaration files or directories supplied through `hikage.optionalViewDeclarationFiles`. */
    val optionalViewDeclarationFiles: List<String>

    /** Local JSON files that the Gradle plugin collects into [viewDeclarationFiles]. */
    val strictViewDeclarationInputFiles: List<String>

    /** Runtime dependency artifacts that the Gradle plugin collects into [optionalViewDeclarationFiles]. */
    val optionalViewDeclarationInputArtifacts: List<String>
}