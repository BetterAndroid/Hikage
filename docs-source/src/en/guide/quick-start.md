# Quick Start

> Integrate `Hikage` into your project.

## Project Requirements

The project needs to be created using `Android Studio` or `IntelliJ IDEA` and must be an Android or Kotlin Multiplatform
project with integrated Kotlin environment dependencies.

- Android Studio (It is recommended to get the latest version from [here](https://developer.android.com/studio))

- IntelliJ IDEA (It is recommended to get the latest version from [here](https://www.jetbrains.com/idea))

- Kotlin 1.9.0+, Gradle 8+, Java 17+, Android Gradle Plugin 8+

### Configure Repositories

The dependencies of `Hikage` are published in **Maven Central** and our public repository,
you can use the following method to configure repositories.

We recommend using Kotlin DSL as the Gradle build script language.

Configure dependency in your project's `build.gradle.kts`.

```kotlin
repositories {
    google()
    mavenCentral()
    // (Optional) You can add this URL to use our public repository
    // When Sonatype-OSS fails and cannot publish dependencies, this repository is added as a backup
    // For details, please visit: https://github.com/HighCapable/maven-repository
    maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
}
```

### Configure Java Version

Modify the Java version of Kotlin in your project's `build.gradle.kts` to 17 or above.

> Kotlin DSL

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```

## Quick Setup

Add the following content to your project's `gradle/libs.versions.toml`.

```toml
[versions]
# ...
hikage-plugin = "<plugin-version>"
hikage-bom = "<version>"

[plugins]
# ...
hikage = { id = "com.highcapable.hikage", version.ref = "hikage-plugin" }

[libraries]
# ...
hikage-bom = { module = "com.highcapable.hikage:hikage-bom", version.ref = "hikage-bom" }
hikage-core = { module = "com.highcapable.hikage:hikage-core" }
hikage-runtime = { module = "com.highcapable.hikage:hikage-runtime" }
# (Optional) If you want to introduce the runtime capability of XML attribute sets.
hikage-runtime-attribute = { module = "com.highcapable.hikage:hikage-runtime-attribute" }
hikage-extension = { module = "com.highcapable.hikage:hikage-extension" }
```

- Replace `<plugin-version>` with ![hikage-plugin](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square&label=hikage-plugin)
- Replace `<version>` with ![hikage-bom](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-bom?logo=apachemaven&logoColor=orange&style=flat-square&label=hikage-bom)

Next, add the following content to your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage)
}

dependencies {
    // ...
    implementation(platform(libs.hikage.bom))
    implementation(libs.hikage.core)
    implementation(libs.hikage.runtime)
    // (Optional) If you want to introduce the runtime capability of XML attribute sets.
    implementation(libs.hikage.runtime.attribute)
    implementation(libs.hikage.extension)
}
```

After clicking the `Sync` button to sync your project, `Hikage` will be successfully integrated into your project.

Then it is recommended to run a build once to ensure all layout components are generated.

## Features Overview

The project is divided into multiple modules. You can choose the module you wish to include as a dependency in your project, but be sure to include the **hikage-core** module.

Click the corresponding module below to view detailed feature descriptions.

::: tip Version Notes

Starting from `1.1.0`, `Hikage` started using unified versioning for releases.
In most cases, you only need to pay attention to the same major version.
You can also refer to the [hikage-bom](../library/hikage-bom.md) below to use BOM for unified dependency version management.

For details, please see the [changelog](../about/changelog.md).

:::

- [hikage-bom](../library/hikage-bom.md)
- [hikage-core](../library/hikage-core.md)
- [hikage-compiler](../library/hikage-compiler.md)
- [hikage-runtime](../library/hikage-runtime.md)
- [hikage-runtime-attribute](../library/hikage-runtime-attribute.md)
- [hikage-extension](../library/hikage-extension.md)
- [hikage-extension-betterandroid](../library/hikage-extension-betterandroid.md)
- [hikage-extension-compose](../library/hikage-extension-compose.md)
- [hikage-widget-foundation](../library/hikage-widget-foundation.md)
- [hikage-widget-androidx](../library/hikage-widget-androidx.md)
- [hikage-widget-material](../library/hikage-widget-material.md)

### Gradle Plugins

Gradle plugins provide dependency assembly and simplified configuration capabilities for `Hikage`.
It is recommended to use them in conjunction with runtime dependencies.

- [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md)
- [hikage-declaration-gradle-plugin](../plugin/hikage-declaration-gradle-plugin.md)

## Demo

You can find some samples [here](repo://tree/main/samples) to view the corresponding demo project to better understand how these functions work and quickly
select the functions you need.