# hikage-declaration-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.declaration.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.declaration.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is the Hikage Gradle plugin for automatically packaging `View` declaration files.

## Configure Plugin

You can add this plugin to your project using the following method.

### Version Catalog (Recommended)

Add plugin in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-plugin = "<version>"

[plugins]
hikage-declaration = { id = "com.highcapable.hikage.declaration", version.ref = "hikage-plugin" }
```

Apply plugin in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage.declaration)
}
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Apply plugin in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    id("com.highcapable.hikage.declaration") version "<version>"
}
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

The plugin reads `hikage-view-declaration` under the current module's `main` source set resources directory and copies it to `META-INF` when packaging.

``` :no-line-numbers
src/
└── main
    └── resources
        └── hikage-view-declaration
            ├── widget.json
            ├── view.json
            └── ...
```

The published resource path is as follows.

``` :no-line-numbers
META-INF/
└── hikage
    └── view-declaration
        └── <group>
            └── <module>
                ├── widget.json
                ├── view.json
                └── ...
```

The original `hikage-view-declaration/**` path is not kept at the root of the published artifact.

For the JSON format of the `View` declaration file, please refer to [hikage-compiler → View Declaration File](../library/hikage-compiler.md#view-declaration-file).

::: tip

This plugin can be applied to Android, Java, or Kotlin JVM projects, and you can flexibly choose the published artifacts and publishing formats.

The plugin is only responsible for packaging `View` declaration files, not for generating component functions, nor will it automatically apply the Maven Publish plugin.

After applying [hikage-gradle-plugin](./hikage-gradle-plugin.md) in the consumer module, the declaration files from dependencies are collected automatically and passed to [hikage-compiler](../library/hikage-compiler.md) to generate component functions.

:::