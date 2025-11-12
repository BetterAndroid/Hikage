enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
    }
}

plugins {
    id("com.highcapable.gropify") version "1.0.0"
}

gropify {
    rootProject {
        common {
            isEnabled = false
        }
    }

    projects(
        ":hikage-core",
        ":hikage-core-lint",
        ":hikage-extension",
        ":hikage-extension-compose",
        ":hikage-extension-betterandroid",
        ":hikage-compiler",
        ":hikage-widget-androidx",
        ":hikage-widget-material"
    ) {
        android {
            isRestrictedAccessEnabled = true
        }
        jvm {
            isRestrictedAccessEnabled = true
        }
    }
    projects(":samples:app") {
        android {
            isEnabled = false
        }
    }
}

rootProject.name = "Hikage"

include(":samples:app")
include(
    ":hikage-core",
    ":hikage-core-lint",
    ":hikage-extension",
    ":hikage-extension-compose",
    ":hikage-extension-betterandroid",
    ":hikage-compiler",
    ":hikage-widget-androidx",
    ":hikage-widget-material"
)