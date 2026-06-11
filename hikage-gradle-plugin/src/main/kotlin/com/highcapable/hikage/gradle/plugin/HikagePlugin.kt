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
 * This file is created by fankes on 2026/6/6.
 */
@file:Suppress("unused")

package com.highcapable.hikage.gradle.plugin

import com.highcapable.hikage.gradle.plugin.extension.HikageExtension
import com.highcapable.hikage.gradle.plugin.integration.AndroidIntegration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/**
 * The Hikage Gradle plugin.
 */
class HikagePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<HikageExtension>(HikageExtension.NAME)
        AndroidIntegration(target, extension).configure()
    }
}