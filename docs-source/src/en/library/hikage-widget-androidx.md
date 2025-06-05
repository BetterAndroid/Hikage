# hikage-widget-androidx

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-widget-androidx?logo=apachemaven&logoColor=orange)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-widget-androidx%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android)

This is a Hikage extension dependency for Jetpack Compact component-related features.

## Configure Dependency

You can add this module to your project using the following method.

### SweetDependency (Recommended)

Add dependency in your project's `SweetDependency` configuration file.

```yaml
libraries:
  com.highcapable.hikage:
    hikage-widget-androidx:
      version: +
```

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation(com.highcapable.hikage.hikage.widget.androidx)
```

### Version Catalog

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-widget-androidx = "<version>"

[libraries]
hikage-widget-androidx = { module = "com.highcapable.hikage:hikage-widget-androidx", version.ref = "hikage-widget-androidx" }
```

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation(libs.hikage.widget.androidx)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-widget-androidx:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

This dependency inherits the available components from Jetpack Compact, which you can directly reference to use in Hikage.

> The following example

```kotlin
LinearLayoutCompact(
    lparams = LayoutParams(matchParent = true) {
        topMargin = 16.dp
    },
    init = {
        orientation = LinearLayoutCompat.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    AppCompatTextView {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
}
```