import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    autowire(libs.plugins.kotlin.jvm)
}

group = property.project.groupName

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs = listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Lint-Registry-V2" to property.project.hikage.core.lint.registry.v2.clazz
        )
    }
}

dependencies {
    compileOnly(org.jetbrains.kotlin.kotlin.stdlib)
    compileOnly(com.android.tools.lint.lint.api)
    compileOnly(com.android.tools.lint.lint.checks)

    testImplementation(com.android.tools.lint.lint)
    testImplementation(com.android.tools.lint.lint.tests)
}