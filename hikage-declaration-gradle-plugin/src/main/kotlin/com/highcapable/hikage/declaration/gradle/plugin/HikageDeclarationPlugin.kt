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
 * This file is created by fankes on 2026/6/12.
 */
@file:Suppress("unused")

package com.highcapable.hikage.declaration.gradle.plugin

import com.android.build.api.dsl.CommonExtension
import com.highcapable.hikage.declaration.gradle.plugin.task.GenerateHikageViewDeclarationResourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * The Hikage view declaration Gradle plugin.
 */
class HikageDeclarationPlugin : Plugin<Project> {

    private companion object {

        /** The Android application plugin id. */
        const val ANDROID_APPLICATION_PLUGIN_ID = "com.android.application"

        /** The Android library plugin id. */
        const val ANDROID_LIBRARY_PLUGIN_ID = "com.android.library"

        /** The Java plugin id. */
        const val JAVA_PLUGIN_ID = "java"

        /** The Java library plugin id. */
        const val JAVA_LIBRARY_PLUGIN_ID = "java-library"

        /** The Kotlin JVM plugin id. */
        const val KOTLIN_JVM_PLUGIN_ID = "org.jetbrains.kotlin.jvm"

        /** The main source set name. */
        const val MAIN_SOURCE_SET_NAME = "main"

        /** The Hikage view declaration directory name. */
        const val VIEW_DECLARATION_DIRECTORY_NAME = "hikage-view-declaration"

        /** The Hikage view declaration packaging exclude path. */
        const val VIEW_DECLARATION_PACKAGING_EXCLUDE_PATH = "/$VIEW_DECLARATION_DIRECTORY_NAME/**"

        /** The generated resources task name. */
        const val GENERATE_RESOURCES_TASK_NAME = "generateHikageViewDeclarationResources"

        /** The packaged Hikage view declaration directory name. */
        const val PACKAGED_VIEW_DECLARATION_DIRECTORY_NAME = "META-INF/hikage/view-declaration/"

        private const val GENERATED_HIKAGE_VIEW_DECLARATION_RESOURCES_FOLDER = "generated/hikage/view-declaration-resources"
    }

    private var hasSupportedPlugin = false

    override fun apply(target: Project) {
        target.plugins.withId(ANDROID_APPLICATION_PLUGIN_ID) { configureAndroidProject(target) }
        target.plugins.withId(ANDROID_LIBRARY_PLUGIN_ID) { configureAndroidProject(target) }

        target.plugins.withId(JAVA_PLUGIN_ID) { configureJavaProject(target) }
        target.plugins.withId(JAVA_LIBRARY_PLUGIN_ID) { configureJavaProject(target) }
        target.plugins.withId(KOTLIN_JVM_PLUGIN_ID) { configureJavaProject(target) }

        target.afterEvaluate {
            if (!hasSupportedPlugin) throwUnsupportedPluginException()
        }
    }

    private fun configureJavaProject(project: Project) {
        if (hasSupportedPlugin) return
        hasSupportedPlugin = true

        val resources = project.extensions.getByType<SourceSetContainer>()
            .getByName(MAIN_SOURCE_SET_NAME)
            .resources
        val declarationFiles = project.files(resources.srcDirs.map {
            project.fileTree(it.resolve(VIEW_DECLARATION_DIRECTORY_NAME)) { include("**/*.json") }
        })
        val generateTask = project.registerGenerateResourcesTask(declarationFiles)

        project.extensions.getByType<JavaPluginExtension>()
            .sourceSets
            .getByName(MAIN_SOURCE_SET_NAME)
            .resources
            .srcDir(generateTask.map { it.outputDirectory })

        // Exclude the original local declaration path from packaged resources.
        resources.exclude("$VIEW_DECLARATION_DIRECTORY_NAME/**")
    }

    private fun configureAndroidProject(project: Project) {
        if (hasSupportedPlugin) return
        hasSupportedPlugin = true

        val android = project.extensions.getByType<CommonExtension>()
        val resources = android.sourceSets.getByName(MAIN_SOURCE_SET_NAME).resources
        val declarationFiles = project.files(resources.directories.map {
            project.fileTree(File(it, VIEW_DECLARATION_DIRECTORY_NAME)) { include("**/*.json") }
        })
        val generateTask = project.registerGenerateResourcesTask(declarationFiles)

        resources.directories.add(project.layout.buildDirectory.dir(GENERATED_HIKAGE_VIEW_DECLARATION_RESOURCES_FOLDER).get().asFile.path)
        project.tasks.configureEach {
            if (name.startsWith("process") && name.endsWith("JavaRes")) dependsOn(generateTask)
        }

        // Exclude the original local declaration path from packaged resources.
        android.packaging.resources.excludes.add(VIEW_DECLARATION_PACKAGING_EXCLUDE_PATH)
    }

    private fun Project.registerGenerateResourcesTask(declarationFiles: FileCollection) =
        tasks.register<GenerateHikageViewDeclarationResourcesTask>(GENERATE_RESOURCES_TASK_NAME) {
            groupName.set(provider { this@registerGenerateResourcesTask.group.toString() })
            moduleName.set(this@registerGenerateResourcesTask.name)
            targetPath.set(groupName.map { "$PACKAGED_VIEW_DECLARATION_DIRECTORY_NAME$it/${moduleName.get()}" })
            sourceFiles.from(declarationFiles)
            outputDirectory.set(layout.buildDirectory.dir(GENERATED_HIKAGE_VIEW_DECLARATION_RESOURCES_FOLDER))
        }

    private fun throwUnsupportedPluginException(): Nothing =
        error("Hikage declaration plugin only supports Android and Java/Kotlin JVM projects.")
}