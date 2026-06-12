plugins {
    `java-library`
    alias(libs.plugins.hikage.declaration)
    alias(libs.plugins.maven.publish)
}

group = gropify.project.groupName
version = gropify.project.hikage.bom.version