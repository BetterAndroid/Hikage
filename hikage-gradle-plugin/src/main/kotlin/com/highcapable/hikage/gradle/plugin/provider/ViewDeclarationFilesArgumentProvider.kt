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
 * This file is created by fankes on 2026/6/11.
 */
package com.highcapable.hikage.gradle.plugin.provider

import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.process.CommandLineArgumentProvider
import java.io.File

/**
 * The command line argument provider for view declaration files.
 * @param enabled the provider for whether the plugin is enabled.
 * @param viewDeclarationFiles the provider for whether view declaration files should be included.
 * @param optionName the KSP option name.
 * @param files the file collection of view declaration files or directories.
 */
internal class ViewDeclarationFilesArgumentProvider(
    private val enabled: Provider<Boolean>,
    private val viewDeclarationFiles: Provider<Boolean>,
    private val optionName: String,
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val files: FileCollection
) : CommandLineArgumentProvider {

    override fun asArguments(): Iterable<String> {
        if (!enabled.get() || !viewDeclarationFiles.get()) return emptyList()

        val value = files.files
            .sortedBy { it.absolutePath }
            .joinToString(File.pathSeparator) { it.absolutePath }

        return if (value.isEmpty()) emptyList()
        else listOf("$optionName=$value")
    }
}