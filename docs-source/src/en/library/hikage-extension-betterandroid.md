# hikage-extension-betterandroid

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-extension-betterandroid?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-extension-betterandroid%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-release&style=flat-squares)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is a Hikage extension dependency for [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid) UI component-related features.

## Configure Dependency

You can add this module to your project using the following method.

### SweetDependency (Recommended)

Add dependency in your project's `SweetDependency` configuration file.

```yaml
libraries:
  com.highcapable.hikage:
    hikage-extension-betterandroid:
      version: +
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(com.highcapable.hikage.hikage.extension.betterandroid)
```

### Version Catalog

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-extension-betterandroid = "<version>"

[libraries]
hikage-extension-betterandroid = { module = "com.highcapable.hikage:hikage-extension-betterandroid", version.ref = "hikage-extension-betterandroid" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.extension.betterandroid)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-extension-betterandroid:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-extension-betterandroid).

### Adapter Extension

Hikage provides layout extension function for BetterAndroid's [Adapter](https://betterandroid.github.io/BetterAndroid/en/library/ui-component#adapter),
you can use the Hikage layout directly on the original extension method of the adapter.

It uses the `ViewHolderDelegate` provided by BetterAndroid to create extension methods.

Here is a simple example based on `RecyclerView`.

> The following example

```kotlin
// Assume this is the dataset you need to bind to.
val listData = ArrayList<CustomBean>()
// Create and bind to a custom RecyclerView.Adapter.
val adapter = recyclerView.bindAdapter<CustomBean> {
    onBindData { listData }
    onBindItemView(
        Hikageable = {
            TextView(id = "text_view") {
                text = "Hello, World!"
                textSize = 16f
            }
        }
    ) { hikage, bean, position ->
        hikage.get<TextView>("text_view").text = bean.name
    }
}
```