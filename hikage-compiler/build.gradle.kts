plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.bom.version

dependencies {
    compileOnly(libs.ksp.symbol.processing.api)

    ksp(libs.auto.service.ksp)

    implementation(libs.auto.service.annotations)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}