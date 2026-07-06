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
 * This file is created by fankes on 2026/6/6.
 */
package com.highcapable.hikage.gradle.plugin.extension

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

/**
 * The Hikage compiler configuration.
 * @param objects the objects factory.
 * @param providers the providers factory.
 */
open class HikageCompilerExtension @Inject constructor(objects: ObjectFactory, providers: ProviderFactory) {

    private companion object {

        /** The enabled property name. */
        const val ENABLED_PROPERTY_NAME = "hikage.compiler.enabled"

        /** The version property name. */
        const val VERSION_PROPERTY_NAME = "hikage.compiler.version"

        /** The view declaration files property name. */
        const val VIEW_DECLARATION_FILES_PROPERTY_NAME = "hikage.compiler.viewDeclarationFiles"

        /** The use embedded KSP property name. */
        const val USE_EMBEDDED_KSP_PROPERTY_NAME = "hikage.compiler.useEmbeddedKsp"
    }

    /** Whether to enable the Hikage compiler integration. */
    val enabled = objects.property(Boolean::class.java).convention(
        providers.gradleProperty(ENABLED_PROPERTY_NAME)
            .map(String::toBoolean)
            .orElse(true)
    )

    /** The Hikage compiler version. */
    val version = objects.property(String::class.java).convention(
        providers.gradleProperty(VERSION_PROPERTY_NAME)
            .orElse("")
    )

    /** Whether to generate code from Hikage view declaration files. */
    val viewDeclarationFiles = objects.property(Boolean::class.java).convention(
        providers.gradleProperty(VIEW_DECLARATION_FILES_PROPERTY_NAME)
            .map(String::toBoolean)
            .orElse(true)
    )

    /** Whether to apply the embedded KSP plugin when the project does not apply KSP. */
    val useEmbeddedKsp = objects.property(Boolean::class.java).convention(
        providers.gradleProperty(USE_EMBEDDED_KSP_PROPERTY_NAME)
            .map(String::toBoolean)
            .orElse(true)
    )
}