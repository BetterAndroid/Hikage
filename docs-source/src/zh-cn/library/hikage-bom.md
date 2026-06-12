# hikage-bom

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-bom?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-bom%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是针对 `Hikage` 相关模块统一版本管理的 BOM 依赖。

::: warning

Gradle 插件版本不由 BOM 模块管理。

如果你需要使用 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 或 [hikage-declaration-gradle-plugin](../plugin/hikage-declaration-gradle-plugin.md)，请在 `plugins` 中单独声明插件版本。

:::

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-bom = "<version>"

[libraries]
hikage-bom = { module = "com.highcapable.hikage:hikage-bom", version.ref = "hikage-bom" }
hikage-core = { module = "com.highcapable.hikage:hikage-core" }
hikage-compiler = { module = "com.highcapable.hikage:hikage-compiler" }
hikage-extension = { module = "com.highcapable.hikage:hikage-extension" }
hikage-extension-betterandroid = { module = "com.highcapable.hikage:hikage-extension-betterandroid" }
hikage-extension-compose = { module = "com.highcapable.hikage:hikage-extension-compose" }
hikage-widget-foundation = { module = "com.highcapable.hikage:hikage-widget-foundation" }
hikage-widget-androidx = { module = "com.highcapable.hikage:hikage-widget-androidx" }
hikage-widget-material = { module = "com.highcapable.hikage:hikage-widget-material" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(platform(libs.hikage.bom))

// 请在应用了 KSP 插件的模块中使用
// 如果你使用了插件，将不再需要手动引入以下两行依赖
ksp(platform(libs.hikage.bom))
ksp(libs.hikage.compiler)

implementation(libs.hikage.core)
implementation(libs.hikage.extension)
implementation(libs.hikage.extension.betterandroid)
implementation(libs.hikage.extension.compose)
implementation(libs.hikage.widget.foundation)
implementation(libs.hikage.widget.androidx)
implementation(libs.hikage.widget.material)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(platform("com.highcapable.hikage:hikage-bom:<version>"))

// 请在应用了 KSP 插件的模块中使用
// 如果你使用了插件，将不再需要手动引入以下两行依赖
ksp(platform("com.highcapable.hikage:hikage-bom:<version>"))
ksp("com.highcapable.hikage:hikage-compiler")

implementation("com.highcapable.hikage:hikage-core")
implementation("com.highcapable.hikage:hikage-extension")
implementation("com.highcapable.hikage:hikage-extension-betterandroid")
implementation("com.highcapable.hikage:hikage-extension-compose")
implementation("com.highcapable.hikage:hikage-widget-foundation")
implementation("com.highcapable.hikage:hikage-widget-androidx")
implementation("com.highcapable.hikage:hikage-widget-material")
```

请将 `<version>` 修改为此文档顶部显示的版本。

::: warning

`ksp` 是独立的 Gradle 依赖配置，`implementation(platform(...))` 不会自动管理 `ksp(...)` 中的依赖版本。
如果你需要使用 `hikage-compiler`，请同时在 `ksp` 配置中引入 `hikage-bom`。

我们推荐你优先使用 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 来进行更简便的配置。

:::

## 功能介绍

`hikage-bom` 本身不包含实际代码，它仅作为 Hikage 子模块的 BOM 用于统一管理依赖版本。

目前它会管理以下模块的版本：

- [hikage-core](./hikage-core.md)
- [hikage-compiler](./hikage-compiler.md)
- [hikage-extension](./hikage-extension.md)
- [hikage-extension-betterandroid](./hikage-extension-betterandroid.md)
- [hikage-extension-compose](./hikage-extension-compose.md)
- [hikage-widget-foundation](./hikage-widget-foundation.md)
- [hikage-widget-androidx](./hikage-widget-androidx.md)
- [hikage-widget-material](./hikage-widget-material.md)