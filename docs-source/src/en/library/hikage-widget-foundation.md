# hikage-widget-foundation

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-widget-foundation?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-widget-foundation%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is a Hikage declaration dependency for Android foundation `View` components.

[hikage-core](./hikage-core.md) already includes this dependency by default, but you can also manually include it if needed.

## Configure Dependency

You can add this module to your project using the following method.

We recommend that you first refer to [hikage-bom](./hikage-bom.md) to use BOM for unified version management.

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-widget-foundation = "<version>"

[libraries]
hikage-widget-foundation = { module = "com.highcapable.hikage:hikage-widget-foundation", version.ref = "hikage-widget-foundation" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.widget.foundation)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-widget-foundation:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

This dependency declares the available components from Android foundation `View` that you can directly reference and use in Hikage.

::: tip

This dependency needs to be used in conjunction with [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md),
the plugin will automatically read the view declaration files in the dependency and generate corresponding
Hikage layout component functions (Hikage Performer) in the current module through [hikage-compiler](./hikage-compiler.md).

:::

> The following example

```kotlin
LinearLayout(
    lparams = LayoutParams(matchParent = true) {
        topMargin = 16.dp
    },
    init = {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    TextView {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
}
```