# hikage-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is the Gradle plugin of Hikage, which contains the core capabilities of Hikage.

## Configure Plugin

You can add this plugin to your project using the following method.

### Version Catalog (Recommended)

Add plugin in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-plugin = "<version>"

[plugins]
hikage = { id = "com.highcapable.hikage", version.ref = "hikage-plugin" }
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

The plugin automatically completes the following work.

- Applies the [Google KSP](https://github.com/google/ksp) plugin
- Adds the matching [hikage-compiler](../library/hikage-compiler.md) dependency of the current version to the `ksp` configuration
- Reads JSON declaration files under `resources/hikage-view-declaration` from the current Android `main` source set and passes them to the compiler as strict declarations
- Reads JSON declaration files packaged by [hikage-declaration-gradle-plugin](./hikage-declaration-gradle-plugin.md) under `META-INF/hikage/view-declaration/<group>/<module>/` from runtime dependencies and passes them to the compiler as optional declarations
- Generates Hikage performer symbol information for Lint, and excludes `META-INF/hikage/**` and `hikage-view-declaration/**` from the final APK / AAB

::: tip

If you are a custom view developer, Hikage provides a [hikage-declaration-gradle-plugin](./hikage-declaration-gradle-plugin.md) to handle the view declaration files of third-party dependency libraries,
you can use this plugin to automatically package your defined view declaration files, so that users can automatically generate layout component functions (Hikage Performer).

:::

### Local View Declaration Files

You can place view declaration files in the `main` source set resources directory of an Android module,
these files are only used to generate layout component functions (Hikage Performer) for the current module and will not be packaged into the final APK / AAB.

``` :no-line-numbers
src/
└── main
    └── resources
        └── hikage-view-declaration
            ├── foo.json
            ├── bar.json
            └── ...
```

For the JSON format of the view declaration file, please refer to [hikage-compiler → View Declaration File](../library/hikage-compiler.md#view-declaration-file).

### External View Declaration Files

When your project depends on modules carrying view declaration files, the plugin automatically collects declaration files from these dependencies.

> The following example

```kotlin
dependencies {
    implementation("com.foo:some-widget:<version>")
}
```

### Plugin Configuration

A complete example of plugin configurations is as follows.

> The following example

```kotlin
hikage {
    compiler {
        enabled = true
        version = "<version>"
        viewDeclarationFiles = true
    }
}
```

| Parameter Name         | Description                                                                                                |
| ---------------------- | ---------------------------------------------------------------------------------------------------------- |
| `enabled`              | Whether to enable Hikage compiler wiring, enabled by default                                               |
| `version`              | The [hikage-compiler](../library/hikage-compiler.md)'s version, aligned with the plugin version by default |
| `viewDeclarationFiles` | Whether to generate code through `View` declaration files, enabled by default                              |

::: warning

This plugin only supports Android projects, your project must include the complete Android Gradle plugin.

If you want to take over KSP and compiler dependencies manually, please refer to the manual configuration method of [hikage-compiler](../library/hikage-compiler.md).

:::