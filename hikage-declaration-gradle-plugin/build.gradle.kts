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
}

gradlePlugin {
    plugins {
        create(gropify.project.hikage.declaration.gradle.plugin.moduleName) {
            id = gropify.project.hikage.declaration.gradle.plugin.pluginId
            implementationClass = gropify.project.hikage.declaration.gradle.plugin.implementationClass
        }
    }
}