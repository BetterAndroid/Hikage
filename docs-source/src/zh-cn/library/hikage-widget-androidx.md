# hikage-widget-androidx

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-widget-androidx?logo=apachemaven&logoColor=orange)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-widget-androidx%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android)

这是 Hikage 针对 Jetpack Compact 组件相关功能的扩展依赖。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

### SweetDependency (推荐)

在你的项目 `SweetDependency` 配置文件中添加依赖。

```yaml
libraries:
  com.highcapable.hikage:
    hikage-widget-androidx:
      version: +
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(com.highcapable.hikage.hikage.widget.androidx)
```

### Version Catalog

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-widget-androidx = "<version>"

[libraries]
hikage-widget-androidx = { module = "com.highcapable.hikage:hikage-widget-androidx", version.ref = "hikage-widget-androidx" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.widget.androidx)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-widget-androidx:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

这个依赖中继承了来自 Jetpack Compact 中的可用组件，你可以直接引用它们到 Hikage 中使用。

> 示例如下

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