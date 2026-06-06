# hikage-bom

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-bom?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-bom%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is the BOM dependency for unified version management of `Hikage` related modules.

## Configure Dependency

You can add this module to your project using the following method.

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-bom = "<version>"

[plugins]
hikage = { id = "com.highcapable.hikage", version.ref = "hikage-bom" }

[libraries]
hikage-bom = { module = "com.highcapable.hikage:hikage-bom", version.ref = "hikage-bom" }
hikage-core = { module = "com.highcapable.hikage:hikage-core" }
hikage-compiler = { module = "com.highcapable.hikage:hikage-compiler" }
hikage-extension = { module = "com.highcapable.hikage:hikage-extension" }
hikage-extension-betterandroid = { module = "com.highcapable.hikage:hikage-extension-betterandroid" }
hikage-extension-compose = { module = "com.highcapable.hikage:hikage-extension-compose" }
hikage-widget-androidx = { module = "com.highcapable.hikage:hikage-widget-androidx" }
hikage-widget-material = { module = "com.highcapable.hikage:hikage-widget-material" }
```

Configure dependency in your project's `build.gradle.kts`.

> Apply Plugin

```kotlin
alias(libs.plugins.hikage)
```

> Apply Dependency

```kotlin
implementation(platform(libs.hikage.bom))

// Please use in the module where the KSP plugin is applied.
// If you are using the plugin,
// you will no longer need to manually include the following two dependencies.
ksp(platform(libs.hikage.bom))
ksp(libs.hikage.compiler)

implementation(libs.hikage.core)
implementation(libs.hikage.extension)
implementation(libs.hikage.extension.betterandroid)
implementation(libs.hikage.extension.compose)
implementation(libs.hikage.widget.androidx)
implementation(libs.hikage.widget.material)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

> Apply Plugin

```kotlin
id("com.highcapable.hikage") version "<version>"
```

> Apply Dependency

```kotlin
implementation(platform("com.highcapable.hikage:hikage-bom:<version>"))

// Please use in the module where the KSP plugin is applied.
// If you are using the plugin,
// you will no longer need to manually include the following two dependencies.
ksp(platform("com.highcapable.hikage:hikage-bom:<version>"))
ksp("com.highcapable.hikage:hikage-compiler")

implementation("com.highcapable.hikage:hikage-core")
implementation("com.highcapable.hikage:hikage-extension")
implementation("com.highcapable.hikage:hikage-extension-betterandroid")
implementation("com.highcapable.hikage:hikage-extension-compose")
implementation("com.highcapable.hikage:hikage-widget-androidx")
implementation("com.highcapable.hikage:hikage-widget-material")
```

Please change `<version>` to the version displayed at the top of this document.

::: warning

`ksp` is an independent Gradle dependency configuration, so `implementation(platform(...))` does not manage dependency versions declared in `ksp(...)`.
If you need to use `hikage-compiler`, also add `hikage-bom` to the `ksp` configuration.

We recommend you to use [hikage-gradle-plugin](./hikage-gradle-plugin.md) for easier configuration.

:::

## Function Introduction

`hikage-bom` does not contain actual code itself. It only serves as the BOM of Hikage modules for unified dependency version management.

It currently manages the versions of the following modules:

- [hikage-core](./hikage-core.md)
- [hikage-gradle-plugin](./hikage-gradle-plugin.md)
- [hikage-compiler](./hikage-compiler.md)
- [hikage-extension](./hikage-extension.md)
- [hikage-extension-betterandroid](./hikage-extension-betterandroid.md)
- [hikage-extension-compose](./hikage-extension-compose.md)
- [hikage-widget-androidx](./hikage-widget-androidx.md)
- [hikage-widget-material](./hikage-widget-material.md)