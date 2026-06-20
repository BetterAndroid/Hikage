plugins {
    `java-platform`
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.bom.version

dependencies {
    constraints {
        api(projects.hikageCore)
        api(projects.hikageCompiler)
        api(projects.hikageRuntime)
        api(projects.hikageExtension)
        api(projects.hikageExtensionBetterandroid)
        api(projects.hikageExtensionCompose)
        api(projects.hikageWidgetFoundation)
        api(projects.hikageWidgetAndroidx)
        api(projects.hikageWidgetMaterial)
    }
}