plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.compiler.version

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.ksp.symbol.processing.api)

    ksp(libs.auto.service.ksp)

    implementation(libs.auto.service.annotations)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}