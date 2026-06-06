# hikage-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is the Gradle plugin for Hikage, currently mainly used for automatically wiring [hikage-compiler](./hikage-compiler.md).

## Configure Plugin

You can add this plugin to your project using the following method.

### Version Catalog (Recommended)

Add plugin in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage = "<version>"

[plugins]
hikage = { id = "com.highcapable.hikage", version.ref = "hikage" }
```

Apply plugin in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage)
}
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Apply plugin in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    id("com.highcapable.hikage") version "<version>"
}
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

The plugin automatically applies the [Google KSP](https://github.com/google/ksp) plugin and adds the matching compiler dependency of the current version to the `ksp` configuration.

If you need to temporarily disable Hikage compiler wiring or override the compiler version, you can use the following configuration.

> The following example

```kotlin
hikage {
    compiler {
        enabled = true
        version = "<version>"
    }
}
```

| Parameter Name | Description                                                                                       |
| -------------- | ------------------------------------------------------------------------------------------------- |
| `enabled`      | Whether to enable Hikage compiler wiring, enabled by default                                      |
| `version`      | The [hikage-compiler](./hikage-compiler.md)'s version, aligned with the plugin version by default |

::: warning

This plugin only supports Android projects, your project must include the complete Android Gradle plugin.

If you want to take over KSP and compiler dependencies manually, please refer to the manual configuration method of [hikage-compiler](./hikage-compiler.md).

:::