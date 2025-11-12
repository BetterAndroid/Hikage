# hikage-extension-compose

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-extension-compose?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-extension-compose%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is a Hikage extension dependency for Jetpack Compose component-related features.

## Configure Dependency

You can add this module to your project using the following method.

::: warning

This module relies on the Jetpack Compose compiler plugin.
Please make sure that your project has integrated Jetpack Compose-related dependencies.
Please refer to [here](https://developer.android.com/develop/ui/compose/compiler) for details.

:::

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-extension-compose = "<version>"

[libraries]
hikage-extension-compose = { module = "com.highcapable.hikage:hikage-extension-compose", version.ref = "hikage-extension-compose" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.extension.compose)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-extension-compose:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-extension-compose).

### Use Jetpack Compose in Hikage

You can use the following methods to embed Jetpack Compose components in a Hikage layout.

> The following example

```kotlin
Hikageable {
   ComposeView(
       lparams = LayoutParams(matchParent = true)
   ) {
       Text("Hello, World!")
   }
}
```

### Use Hikage in Jetpack Compose

You can use the following methods to embed Hikage components in a Jetpack Compose layout.

> The following example

```kotlin
Column(
   modifier = Modifier.fillMaxSize()
) {
    HikageView {
        TextView(
            lparams = LayoutParams(matchParent = true)
        ) {
            text = "Hello, World!"
            textSize = 20f
        }
    }
}
```