enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
        mavenLocal()
    }
}

plugins {
    id("com.highcapable.gropify") version "1.0.2"
}

gropify {
    global {
        sourceCode {
            includeKeys("^project\\..*$".toRegex())
            className = rootProject.name
            isRestrictedAccessEnabled = true
        }
    }

    rootProject {
        common {
            isEnabled = false
        }
    }

    projects(
        ":samples:demo-android",
        ":samples:demo-benchmark"
    ) {
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
        ":hikage-widget-foundation",
        ":hikage-widget-androidx",
        ":hikage-widget-material"
    ) {
        jvm {
            isEnabled = false
        }
    }
}

rootProject.name = "Hikage"

include(
    ":samples:demo-android",
    ":samples:demo-benchmark"
)
include(":hikage-bom")
include(
    ":hikage-compiler",
    ":hikage-gradle-plugin",
    ":hikage-declaration-gradle-plugin"
)
include(
    ":hikage-core",
    ":hikage-core-lint",
    ":hikage-runtime",
    ":hikage-runtime-attribute",
    ":hikage-extension",
    ":hikage-extension-compose",
    ":hikage-extension-betterandroid",
    ":hikage-widget-foundation",
    ":hikage-widget-androidx",
    ":hikage-widget-material"
)