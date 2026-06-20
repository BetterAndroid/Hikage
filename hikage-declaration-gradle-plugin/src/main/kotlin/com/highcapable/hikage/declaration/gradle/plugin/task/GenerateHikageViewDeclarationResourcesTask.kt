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
package com.highcapable.hikage.declaration.gradle.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * The task for generating packaged Hikage view declaration resources.
 */
@CacheableTask
abstract class GenerateHikageViewDeclarationResourcesTask : DefaultTask() {

    /** The project group name. */
    @get:Input
    abstract val groupName: Property<String>

    /** The project module name. */
    @get:Input
    abstract val moduleName: Property<String>

    /** The generated target path. */
    @get:Input
    abstract val targetPath: Property<String>

    /** The source view declaration files. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    /** The generated resources output directory. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /** Generate resources. */
    @TaskAction
    fun generate() {
        val outputDir = outputDirectory.get().asFile
        if (outputDir.exists()) outputDir.deleteRecursively()

        sourceFiles.files
            .filter { it.isFile && it.extension == "json" }
            .sortedBy { it.absolutePath }
            .forEach { source ->
                val target = outputDir.resolve(targetPath.get()).resolve(source.name)
                target.parentFile.mkdirs()
                source.copyTo(target, overwrite = true)
            }
    }
}