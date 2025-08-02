# hikage-widget-material

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-widget-material?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-widget-material%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is a Hikage extension dependency for Google Material (MDC) component-related features.

## Configure Dependency

You can add this module to your project using the following method.

### SweetDependency (Recommended)

Add dependency in your project's `SweetDependency` configuration file.

```yaml
libraries:
  com.highcapable.hikage:
    hikage-widget-material:
      version: +
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(com.highcapable.hikage.hikage.widget.material)
```

### Version Catalog

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-widget-material = "<version>"

[libraries]
hikage-widget-material = { module = "com.highcapable.hikage:hikage-widget-material", version.ref = "hikage-widget-material" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.widget.material)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-widget-material:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

This dependency inherits the available components from Google Material (MDC), which you can directly reference to use in Hikage.

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
    MaterialTextView {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
    MaterialButton {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
    TextInputLayout(
        lparams = LayoutParams {
            topMargin = 16.dp
        },
        init = {
            minWidth = 200.dp
            hint = "Enter your text"
        }
    ) {
        TextInputEditText()
    }
}
```