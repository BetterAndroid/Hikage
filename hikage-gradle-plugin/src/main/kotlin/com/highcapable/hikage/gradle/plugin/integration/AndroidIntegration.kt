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
package com.highcapable.hikage.gradle.plugin.integration

import com.android.build.api.dsl.CommonExtension
import com.google.devtools.ksp.gradle.KspExtension
import com.highcapable.hikage.generated.HikageProperties
import com.highcapable.hikage.gradle.plugin.debug.HikagePluginException
import com.highcapable.hikage.gradle.plugin.extension.HikageExtension
import com.highcapable.hikage.gradle.plugin.provider.ViewDeclarationFilesArgumentProvider
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import java.io.File

/**
 * The Android integration for Hikage Gradle plugin.
 * @param project the current project.
 * @param extension the Hikage extension.
 */
internal class AndroidIntegration(private val project: Project, private val extension: HikageExtension) {

    companion object {

        /** The Android application plugin id. */
        const val ANDROID_APPLICATION_PLUGIN_ID = "com.android.application"

        /** The Android library plugin id. */
        const val ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

        /** The Google KSP plugin id. */
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"

        /** The KSP dependency configuration name. */
        const val KSP_CONFIGURATION_NAME = "ksp"

        /** The Hikage view declaration files KSP option name. */
        const val VIEW_DECLARATION_FILES_OPTION_NAME = "hikage.viewDeclarationFiles"

        /** The main source set name. */
        const val MAIN_SOURCE_SET_NAME = "main"

        /** The Hikage view declaration directory name. */
        const val VIEW_DECLARATION_DIRECTORY_NAME = "hikage-view-declaration"

        const val HIKAGE_GROUP_NAME = HikageProperties.PROJECT_GROUP_NAME
        const val HIKAGE_COMPILER_MODULE_NAME = HikageProperties.PROJECT_HIKAGE_COMPILER_MODULE_NAME
    }

    private var hasAndroidPlugin = false

    /** Configure Android integration. */
    fun configure() {
        project.plugins.withId(ANDROID_APPLICATION_PLUGIN_ID) { configureAndroidProject() }
        project.plugins.withId(ANDROID_LIBRARY_PLUGIN_ID) { configureAndroidProject() }

        project.afterEvaluate {
            if (!hasAndroidPlugin) throw HikagePluginException("Only support Android projects.")
        }
    }

    private fun configureAndroidProject() {
        if (hasAndroidPlugin) return

        hasAndroidPlugin = true
        configureCompiler()
    }

    private fun configureCompiler() {
        val compiler = extension.compiler
        if (compiler.enabled.get()) project.pluginManager.apply(KSP_PLUGIN_ID)

        project.plugins.withId(KSP_PLUGIN_ID) {
            configureCompilerOptions()
            configureCompilerDependency()
        }
    }

    private fun configureCompilerOptions() = project.extensions.configure<KspExtension> {
        arg(ViewDeclarationFilesArgumentProvider(
            enabled = extension.compiler.enabled,
            viewDeclarationFiles = extension.compiler.viewDeclarationFiles,
            files = createViewDeclarationFiles()
        ))
    }

    private fun configureCompilerDependency() = project.dependencies.addProvider(
        KSP_CONFIGURATION_NAME,
        project.providers.provider {
            val compiler = extension.compiler
            if (!compiler.enabled.get()) return@provider null

            val version = compiler.version.get().trim()
            if (version.isEmpty()) throw HikagePluginException("Hikage compiler version cannot be empty.")

            "$HIKAGE_GROUP_NAME:$HIKAGE_COMPILER_MODULE_NAME:$version"
        }
    )

    private fun createViewDeclarationFiles(): FileCollection {
        val android = project.extensions.getByType<CommonExtension>()
        val resources = android.sourceSets.getByName(MAIN_SOURCE_SET_NAME).resources
        val directories = resources.directories.map { File(it, VIEW_DECLARATION_DIRECTORY_NAME) }

        return project.files(directories.map {
            project.fileTree(it) { include("**/*.json") }
        })
    }
}