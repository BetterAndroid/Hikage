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
import com.highcapable.hikage.gradle.plugin.task.CollectHikageViewDeclarationFilesTask
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
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

        /** The KSP task name prefix. */
        const val KSP_TASK_NAME_PREFIX = "ksp"

        /** The KSP task name suffix. */
        const val KSP_TASK_NAME_SUFFIX = "Kotlin"

        /** The runtime classpath configuration name. */
        const val RUNTIME_CLASSPATH_CONFIGURATION_NAME = "runtimeClasspath"

        /** The Hikage view declaration files KSP option name. */
        const val VIEW_DECLARATION_FILES_OPTION_NAME = "hikage.viewDeclarationFiles"

        /** The optional Hikage view declaration files KSP option name. */
        const val OPTIONAL_VIEW_DECLARATION_FILES_OPTION_NAME = "hikage.optionalViewDeclarationFiles"

        /** The Hikage project group KSP option name. */
        const val PROJECT_GROUP_OPTION_NAME = "hikage.internal.projectGroup"

        /** The Hikage project name KSP option name. */
        const val PROJECT_NAME_OPTION_NAME = "hikage.internal.projectName"

        /** The main source set name. */
        const val MAIN_SOURCE_SET_NAME = "main"

        /** The Hikage view declaration directory name. */
        const val VIEW_DECLARATION_DIRECTORY_NAME = "hikage-view-declaration"

        /** The packaged Hikage metadata exclude path. */
        const val PACKAGED_HIKAGE_METADATA_EXCLUDE_PATH = "META-INF/hikage/**"

        /** The local Hikage view declaration packaging exclude path. */
        const val VIEW_DECLARATION_PACKAGING_EXCLUDE_PATH = "$VIEW_DECLARATION_DIRECTORY_NAME/**"

        /** The collect view declaration files task name. */
        const val COLLECT_VIEW_DECLARATION_FILES_TASK_NAME = "collectHikageViewDeclarationFiles"

        /** The Hikage view declaration files task input property name. */
        const val VIEW_DECLARATION_FILES_INPUT_PROPERTY_NAME = "hikageViewDeclarationFiles"

        private const val GENERATED_HIKAGE_VIEW_DECLARATION_FOLDER = "generated/hikage/view-declaration-files"

        private const val HIKAGE_GROUP_NAME = HikageProperties.PROJECT_GROUP_NAME
        private const val HIKAGE_COMPILER_MODULE_NAME = HikageProperties.PROJECT_HIKAGE_COMPILER_MODULE_NAME
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
        configurePackaging()
        configureCompiler()
    }

    private fun configurePackaging() {
        val resourcesExcludes = project.extensions.getByType<CommonExtension>()
            .packaging
            .resources
            .excludes

        // They shouldn't be packaged into the final APK / AAB.
        resourcesExcludes.add(PACKAGED_HIKAGE_METADATA_EXCLUDE_PATH)
        resourcesExcludes.add(VIEW_DECLARATION_PACKAGING_EXCLUDE_PATH)
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
        val collectTask = createCollectViewDeclarationFilesTask()
        configureKspTaskInputs(collectTask)

        arg(PROJECT_GROUP_OPTION_NAME, project.provider { project.group.toString() })
        arg(PROJECT_NAME_OPTION_NAME, project.provider { project.name })

        arg(ViewDeclarationFilesArgumentProvider(
            enabled = extension.compiler.enabled,
            viewDeclarationFiles = extension.compiler.viewDeclarationFiles,
            optionName = VIEW_DECLARATION_FILES_OPTION_NAME,
            files = project.files(collectTask.flatMap { it.strictOutputDirectory }).builtBy(collectTask)
        ))
        arg(ViewDeclarationFilesArgumentProvider(
            enabled = extension.compiler.enabled,
            viewDeclarationFiles = extension.compiler.viewDeclarationFiles,
            optionName = OPTIONAL_VIEW_DECLARATION_FILES_OPTION_NAME,
            files = project.files(collectTask.flatMap { it.optionalOutputDirectory }).builtBy(collectTask)
        ))
    }

    private fun configureKspTaskInputs(collectTask: TaskProvider<CollectHikageViewDeclarationFilesTask>) {
        val files = project.files(
            collectTask.flatMap { it.strictOutputDirectory },
            collectTask.flatMap { it.optionalOutputDirectory }
        ).builtBy(collectTask)

        project.tasks.configureEach {
            if (!name.startsWith(KSP_TASK_NAME_PREFIX) || !name.endsWith(KSP_TASK_NAME_SUFFIX)) return@configureEach

            dependsOn(collectTask)
            inputs.files(files)
                .withPropertyName(VIEW_DECLARATION_FILES_INPUT_PROPERTY_NAME)
                .withPathSensitivity(PathSensitivity.RELATIVE)
        }
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

    private fun createCollectViewDeclarationFilesTask(): TaskProvider<CollectHikageViewDeclarationFilesTask> {
        val collectTask = project.tasks.register<CollectHikageViewDeclarationFilesTask>(COLLECT_VIEW_DECLARATION_FILES_TASK_NAME) {
            localFiles.from(createLocalViewDeclarationFiles())
            outputDirectory.set(project.layout.buildDirectory.dir(GENERATED_HIKAGE_VIEW_DECLARATION_FOLDER))
        }

        project.configurations.configureEach {
            if (!isCanBeResolved || !name.endsWith(RUNTIME_CLASSPATH_CONFIGURATION_NAME.replaceFirstChar(Char::uppercaseChar)))
                return@configureEach

            collectTask.configure {
                dependencyArtifacts.from(incoming.artifactView {
                    isLenient = true
                    componentFilter {
                        it !is ProjectComponentIdentifier || it.projectPath != project.path
                    }
                }.artifacts.artifactFiles)
            }
        }

        return collectTask
    }

    private fun createLocalViewDeclarationFiles(): FileCollection {
        val android = project.extensions.getByType<CommonExtension>()
        val resources = android.sourceSets.getByName(MAIN_SOURCE_SET_NAME).resources
        val buildDir = project.layout.buildDirectory.get().asFile
        val directories = resources.directories
            .map { File(it) }
            .filterNot { it.startsWith(buildDir) }
            .map { File(it, VIEW_DECLARATION_DIRECTORY_NAME) }

        return project.files(directories.map {
            project.fileTree(it) { include("**/*.json") }
        })
    }
}