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
package com.highcapable.hikage.gradle.plugin.task

import net.lingala.zip4j.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * The task for collecting Hikage view declaration files from local resources and dependencies.
 */
@CacheableTask
abstract class CollectHikageViewDeclarationFilesTask : DefaultTask() {

    private companion object {

        /** The packaged Hikage view declaration directory name. */
        const val PACKAGED_VIEW_DECLARATION_DIRECTORY_NAME = "META-INF/hikage/view-declaration/"

        /** The AAR classes jar entry name. */
        const val AAR_CLASSES_JAR_ENTRY_NAME = "classes.jar"
    }

    /** The local view declaration files. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localFiles: ConfigurableFileCollection

    /** The dependency artifacts. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val dependencyArtifacts: ConfigurableFileCollection

    /** The collected output directory. */
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    /** The strict declaration files output directory. */
    @get:Internal
    val strictOutputDirectory get() = outputDirectory.dir("strict")

    /** The optional declaration files output directory. */
    @get:Internal
    val optionalOutputDirectory get() = outputDirectory.dir("optional")

    /** Collect declaration files. */
    @TaskAction
    fun collect() {
        val outputDir = outputDirectory.get().asFile
        if (outputDir.exists()) outputDir.deleteRecursively()
        val strictOutputDir = strictOutputDirectory.get().asFile
        val optionalOutputDir = optionalOutputDirectory.get().asFile

        localFiles.files
            .filter { it.isFile && it.extension == "json" }
            .sortedBy { it.absolutePath }
            .forEachIndexed { index, file ->
                strictOutputDir.resolve("local/$index/${file.name}").also { target ->
                    target.parentFile.mkdirs()
                    file.copyTo(target, overwrite = true)
                }
            }

        dependencyArtifacts.files
            .filter { it.isFile }
            .sortedBy { it.absolutePath }
            .forEachIndexed { index, artifact ->
                collectArtifact(artifact = artifact, outputDir = optionalOutputDir.resolve("dependencies/$index"))
            }
    }

    private fun collectArtifact(artifact: File, outputDir: File) {
        runCatching {
            ZipFile(artifact).use { zip ->
                val entries = zip.fileHeaders
                entries
                    .filter { !it.isDirectory && it.fileName.startsWith(PACKAGED_VIEW_DECLARATION_DIRECTORY_NAME) && it.fileName.endsWith(".json") }
                    .forEach { entry ->
                        zip.getInputStream(entry).use { input ->
                            outputDir.resolve(entry.fileName).also { target ->
                                target.parentFile.mkdirs()
                                target.outputStream().use(input::copyTo)
                            }
                        }
                    }

                entries.firstOrNull { !it.isDirectory && it.fileName == AAR_CLASSES_JAR_ENTRY_NAME }?.let { entry ->
                    zip.getInputStream(entry).use { input ->
                        val classesJar = temporaryDir.resolve("${artifact.nameWithoutExtension}-classes.jar")
                        classesJar.outputStream().use(input::copyTo)
                        collectArtifact(classesJar, outputDir)
                    }
                }
            }
        }
    }
}