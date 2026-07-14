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
package com.highcapable.hikage.gradle.plugin.model

import com.highcapable.hikage.generated.HikageProperties
import com.highcapable.hikage.gradle.model.DefaultHikageGradleModel
import com.highcapable.hikage.gradle.model.HikageGradleModel
import com.highcapable.hikage.gradle.plugin.extension.HikageExtension
import com.highcapable.hikage.gradle.plugin.integration.AndroidIntegration
import com.highcapable.hikage.gradle.plugin.task.CollectHikageViewDeclarationFilesTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

/**
 * Builds [HikageGradleModel] from the finalized Hikage Gradle extension.
 */
class HikageGradleModelBuilder internal constructor() : ToolingModelBuilder {

    internal companion object {

        private const val REGISTERED_KEY = HikageProperties.PROJECT_HIKAGE_GRADLE_PLUGIN_TOOLING_MODEL_BUILDER_REGISTERED_KEY

        /**
         * Registers the [HikageGradleModelBuilder] with the given [project] and [toolingModelBuilders].
         * @param project the Gradle project.
         * @param toolingModelBuilders the tooling model builder registry.
         */
        fun register(project: Project, toolingModelBuilders: ToolingModelBuilderRegistry) {
            val extraProperties = project.rootProject.extensions.extraProperties
            if (extraProperties.has(REGISTERED_KEY)) return

            // Register the tooling model builder only once per project.
            toolingModelBuilders.register(HikageGradleModelBuilder())
            extraProperties.set(REGISTERED_KEY, true)
        }
    }

    override fun canBuild(modelName: String) = modelName == HikageGradleModel::class.qualifiedName

    override fun buildAll(modelName: String, project: Project): HikageGradleModel {
        val extension = project.extensions.findByType<HikageExtension>() ?: return DefaultHikageGradleModel.Empty

        val collectTask = project.tasks.findByName(AndroidIntegration.COLLECT_VIEW_DECLARATION_FILES_TASK_NAME)
            as? CollectHikageViewDeclarationFilesTask
        val isCompilerEnabled = extension.compiler.enabled.get() && 
            project.pluginManager.hasPlugin(AndroidIntegration.KSP_PLUGIN_ID) &&
            collectTask != null
        val declarationTask = collectTask?.takeIf {
            isCompilerEnabled && extension.compiler.viewDeclarationFiles.get()
        }

        return DefaultHikageGradleModel(
            isPluginApplied = true,
            isCompilerEnabled = isCompilerEnabled,
            viewDeclarationFiles = declarationTask?.strictOutputDirectory?.get()?.asFile?.absolutePath?.let(::listOf).orEmpty(),
            optionalViewDeclarationFiles = declarationTask?.optionalOutputDirectory?.get()?.asFile?.absolutePath?.let(::listOf).orEmpty(),
            strictViewDeclarationInputFiles = declarationTask?.localFiles?.files
                ?.filter { file -> file.isFile && file.extension == "json" }
                ?.map { file -> file.absolutePath }
                .orEmpty(),
            optionalViewDeclarationInputArtifacts = declarationTask?.dependencyArtifacts?.files
                ?.filter { file -> file.isFile }
                ?.map { file -> file.absolutePath }
                .orEmpty()
        )
    }
}