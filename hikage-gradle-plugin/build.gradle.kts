plugins {
    `kotlin-dsl`
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.plugin.version

java {
    withSourcesJar()
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    implementation(libs.ksp.symbol.processing.gradle.plugin)
    implementation(libs.zip4j)
}

gradlePlugin {
    plugins {
        create(gropify.project.hikage.gradle.plugin.moduleName) {
            id = gropify.project.groupName
            implementationClass = gropify.project.hikage.gradle.plugin.implementationClass
        }
    }
}