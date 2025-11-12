# hikage-extension-compose

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-extension-compose?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-extension-compose%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

这是 Hikage 针对 Jetpack Compose 组件相关功能的扩展依赖。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

::: warning

此模块依赖于 Jetpack Compose 编译插件，请确保你的项目中已经集成了 Jetpack Compose 相关依赖，详情请参考 [这里](https://developer.android.com/develop/ui/compose/compiler)。

:::

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-extension-compose = "<version>"

[libraries]
hikage-extension-compose = { module = "com.highcapable.hikage:hikage-extension-compose", version.ref = "hikage-extension-compose" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.extension.compose)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-extension-compose:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

你可以 [点击这里](kdoc://hikage-extension-compose) 查看 KDoc。

### 在 Hikage 中使用 Jetpack Compose

你可以使用以下方式在一个 Hikage 布局中嵌入 Jetpack Compose 组件。

> 示例如下

```kotlin
Hikageable {
   ComposeView(
       lparams = LayoutParams(matchParent = true)
   ) {
       Text("Hello, World!")
   }
}
```

### 在 Jetpack Compose 中使用 Hikage

你可以使用以下方式在一个 Jetpack Compose 布局中嵌入 Hikage 组件。

> 示例如下

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