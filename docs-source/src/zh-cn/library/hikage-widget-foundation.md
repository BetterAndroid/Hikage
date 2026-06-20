# hikage-widget-foundation

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-widget-foundation?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-widget-foundation%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是 Hikage 针对 Android 基础 `View` 组件的声明依赖。

[hikage-core](./hikage-core.md) 默认会自动引入此依赖，如有需要你也可以手动引入它。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

我们推荐你优先参考 [hikage-bom](./hikage-bom.md) 使用 BOM 统一管理版本。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-widget-foundation = "<version>"

[libraries]
hikage-widget-foundation = { module = "com.highcapable.hikage:hikage-widget-foundation", version.ref = "hikage-widget-foundation" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.widget.foundation)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-widget-foundation:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

这个依赖中声明了来自 Android 基础 `View` 中的可用组件，你可以直接引用它们到 Hikage 中使用。

::: tip

这个依赖需要配合 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 使用，插件会自动读取依赖中的 `View` 声明文件，并通过 [hikage-compiler](./hikage-compiler.md) 在当前模块生成对应的 Hikage 布局组件函数 (Hikage Performer)。

:::

> 示例如下

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