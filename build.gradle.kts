import com.android.build.gradle.LibraryExtension
import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    autowire(libs.plugins.android.application) apply false
    autowire(libs.plugins.android.library) apply false
    autowire(libs.plugins.kotlin.android) apply false
    autowire(libs.plugins.kotlin.jvm) apply false
    autowire(libs.plugins.kotlin.ksp) apply false
    autowire(libs.plugins.compose.compiler) apply false
    autowire(libs.plugins.kotlin.dokka) apply false
    autowire(libs.plugins.maven.publish) apply false
}

libraryProjects {
    afterEvaluate {
        configure<PublishingExtension> {
            repositories {
                val repositoryDir = gradle.gradleUserHomeDir
                    .resolve("highcapable-maven-repository")
                    .resolve("repository")
                maven {
                    name = "HighCapableMavenReleases"
                    url = repositoryDir.resolve("releases").toURI()
                }
                maven {
                    name = "HighCapableMavenSnapShots"
                    url = repositoryDir.resolve("snapshots").toURI()
                }
            }
        }
        configure<MavenPublishBaseExtension> {
            if (name != Libraries.HIKAGE_COMPILER)
                configure(AndroidSingleVariantLibrary(publishJavadocJar = false))
        }
        // Only apply to publishable tasks.
        if (gradle.startParameter.taskNames.any { it.startsWith("publish") })
            if (name != Libraries.HIKAGE_COMPILER)
                configure<LibraryExtension> {
                    sourceSets.forEach {
                        // Add KSP generated sources to the source set.
                        val kspSources = file(layout.buildDirectory.dir("generated/ksp/release").get())
                        if (kspSources.exists()) it.kotlin.srcDir(kspSources)
                    }
                }
    }
    tasks.withType<DokkaTask>().configureEach {
        val configuration = """{ "footerMessage": "Hikage | Apache-2.0 License | Copyright (C) 2019 HighCapable" }"""
        pluginsMapConfiguration.set(mapOf("org.jetbrains.dokka.base.DokkaBase" to configuration))
    }
    tasks.register("publishKDoc") {
        group = "documentation"
        dependsOn("dokkaHtml")
        doLast {
            val docsDir = rootProject.projectDir
                .resolve("docs-source")
                .resolve("dist")
                .resolve("KDoc")
                .resolve(project.name)
            if (docsDir.exists()) docsDir.deleteRecursively() else docsDir.mkdirs()
            layout.buildDirectory.dir("dokka/html").get().asFile.copyRecursively(docsDir)
        }
    }
}

allprojects {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-Xno-param-assertions",
                "-Xno-call-assertions",
                "-Xno-receiver-assertions"
            )
        }
    }
}

fun libraryProjects(action: Action<in Project>) {
    val libraries = listOf(
        Libraries.HIKAGE_CORE,
        Libraries.HIKAGE_EXTENSION,
        Libraries.HIKAGE_EXTENSION_COMPOSE,
        Libraries.HIKAGE_EXTENSION_BETTERANDROID,
        Libraries.HIKAGE_COMPILER,
        Libraries.HIKAGE_WIDGET_ANDROIDX,
        Libraries.HIKAGE_WIDGET_MATERIAL
    )
    allprojects { if (libraries.contains(name)) action.execute(this) }
}

object Libraries {
    const val HIKAGE_CORE = "hikage-core"
    const val HIKAGE_EXTENSION = "hikage-extension"
    const val HIKAGE_EXTENSION_COMPOSE = "hikage-extension-compose"
    const val HIKAGE_EXTENSION_BETTERANDROID = "hikage-extension-betterandroid"
    const val HIKAGE_COMPILER = "hikage-compiler"
    const val HIKAGE_WIDGET_ANDROIDX = "hikage-widget-androidx"
    const val HIKAGE_WIDGET_MATERIAL = "hikage-widget-material"
}