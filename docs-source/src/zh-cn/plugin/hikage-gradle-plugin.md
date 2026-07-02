# hikage-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是 Hikage 的 Gradle 插件，包含了 Hikage 的核心能力。

## 配置插件

你可以使用如下方式将此插件添加到你的项目中。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加插件。

```toml
[versions]
hikage-plugin = "<version>"

[plugins]
hikage = { id = "com.highcapable.hikage", version.ref = "hikage-plugin" }
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

插件会自动完成以下工作。

- 自动应用 [Google KSP](https://github.com/google/ksp) 插件
- 自动向 `ksp` 配置加入当前版本对应的 [hikage-compiler](./hikage-compiler.md) 依赖
- 自动读取当前 Android `main` 源集 `resources/hikage-view-declaration` 目录下的 JSON 声明文件，并作为严格声明传递给编译器
- 自动读取运行时依赖中由 [hikage-declaration-gradle-plugin](./hikage-declaration-gradle-plugin.md) 打包到 `META-INF/hikage/view-declaration/<group>/<module>/` 的 JSON 声明文件，并作为可选声明传递给编译器
- 自动生成 Hikage Performer 符号信息，并在最终 APK / AAB 中排除 `META-INF/hikage/**` 与 `hikage-view-declaration/**`

::: tip

如果你是自定义 `View` 开发者，Hikage 提供了一个 [hikage-declaration-gradle-plugin](./hikage-declaration-gradle-plugin.md) 来处理第三方依赖库的 `View` 声明文件，你可以使用这个插件来自动将你定义的 `View` 声明文件进行打包，供使用者自动生成布局组件函数 (Hikage Performer)。

:::

### 本地 View 声明文件

你可以在 Android 模块的 `main` 源集资源目录中放置 `View` 声明文件，这些文件只用于当前模块生成布局组件函数 (Hikage Performer)，不会被打包到最终 APK / AAB 中。

``` :no-line-numbers
src/
└── main
    └── resources
        └── hikage-view-declaration
            ├── foo.json
            ├── bar.json
            └── ...
```

关于 `View` 声明文件的 JSON 格式，请参考 [hikage-compiler → View 声明文件](../library/hikage-compiler.md#view-声明文件)。

### 外部 View 声明文件

当你的项目依赖了携带 `View` 声明文件的模块时，插件会自动收集这些依赖中的声明文件。

> 示例如下

```kotlin
dependencies {
    implementation("com.foo:some-widget:<version>")
}
```

### 插件配置

完整的插件配置项示例如下。

> 示例如下

```kotlin
hikage {
    compiler {
        enabled = true
        version = "<version>"
        viewDeclarationFiles = true
        useEmbeddedKsp = true
    }
}
```

| 参数名称               | 描述                                                                          |
| ---------------------- | ----------------------------------------------------------------------------- |
| `enabled`              | 是否启用 Hikage 编译器装配，默认启用                                          |
| `version`              | [hikage-compiler](../library/hikage-compiler.md) 版本，默认与插件版本保持一致 |
| `viewDeclarationFiles` | 是否通过 `View` 声明文件生成代码，默认启用                                    |
| `useEmbeddedKsp`       | 当前项目没有应用 KSP 插件时，是否使用插件内置的 KSP 插件自动装配，默认启用    |

### 高级用法

如果你的项目需要自行控制 Kotlin 与 KSP 的版本，可以在根项目或当前模块声明 KSP 插件，并关闭内置 KSP 自动装配。

> 示例如下

```kotlin
plugins {
    id("com.google.devtools.ksp") version "<ksp-version>" apply false
}
```

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp")
    id("com.highcapable.hikage")
}

hikage {
    compiler {
        useEmbeddedKsp = false
    }
}
```

::: warning

此插件仅支持在 Android 项目使用，你的项目必须包含完整的 Android Gradle 插件。

如果你希望手动接管 KSP 与编译器依赖，请参考 [hikage-compiler](../library/hikage-compiler.md)。

:::