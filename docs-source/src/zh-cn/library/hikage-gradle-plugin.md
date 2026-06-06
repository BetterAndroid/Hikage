# hikage-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是 Hikage 的 Gradle 插件，目前主要用于自动装配 [hikage-compiler](./hikage-compiler.md)。

## 配置插件

你可以使用如下方式将此插件添加到你的项目中。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加插件。

```toml
[versions]
hikage = "<version>"

[plugins]
hikage = { id = "com.highcapable.hikage", version.ref = "hikage" }
```

在你的项目 `build.gradle.kts` 中应用插件。

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage)
}
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中应用插件。

```kotlin
plugins {
    // ...
    id("com.highcapable.hikage") version "<version>"
}
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

插件会自动应用 [Google KSP](https://github.com/google/ksp) 插件，并向 `ksp` 配置中加入当前版本对应的编译器依赖。

如果你需要临时关闭 Hikage 编译器装配，或覆盖编译器版本，可以使用如下配置。

> 示例如下

```kotlin
hikage {
    compiler {
        enabled = true
        version = "<version>"
    }
}
```

| 参数名称  | 描述                                                                 |
| --------- | -------------------------------------------------------------------- |
| `enabled` | 是否启用 Hikage 编译器装配，默认启用                                 |
| `version` | [hikage-compiler](./hikage-compiler.md) 版本，默认与插件版本保持一致 |

::: warning

此插件仅支持在 Android 项目使用，你的项目必须包含完整的 Android Gradle 插件。

如果你希望手动接管 KSP 与编译器依赖，请参考 [hikage-compiler](./hikage-compiler.md)。

:::