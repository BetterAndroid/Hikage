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
    id("com.highcapable.gropify") version "1.0.1"
}

gropify {
    rootProject {
        common {
            isEnabled = false
        }
    }

    projects(":samples:demo-android") {
        android {
            isEnabled = false
        }
    }

    projects(":hikage-bom") {
        jvm {
            isEnabled = false
        }
    }

    projects(
        ":hikage-core",
        ":hikage-core-lint",
        ":hikage-compiler",
        ":hikage-extension",
        ":hikage-extension-compose",
        ":hikage-extension-betterandroid",
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
}

rootProject.name = "Hikage"

include(":samples:demo-android")
include(":hikage-bom")
include(
    ":hikage-core",
    ":hikage-core-lint",
    ":hikage-compiler",
    ":hikage-extension",
    ":hikage-extension-compose",
    ":hikage-extension-betterandroid",
    ":hikage-widget-androidx",
    ":hikage-widget-material"
)