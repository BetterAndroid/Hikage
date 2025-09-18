plugins {
    autowire(libs.plugins.kotlin.jvm)
    autowire(libs.plugins.kotlin.ksp)
    autowire(libs.plugins.maven.publish)
}

group = property.project.groupName
version = property.project.hikage.compiler.version

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

dependencies {
    compileOnly(com.google.devtools.ksp.symbol.processing.api)

    ksp(dev.zacsweers.autoservice.auto.service.ksp)

    implementation(com.google.auto.service.auto.service.annotations)
    implementation(com.squareup.kotlinpoet)
    implementation(com.squareup.kotlinpoet.ksp)
}