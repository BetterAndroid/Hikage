# hikage-declaration-gradle-plugin

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.declaration.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fcom.highcapable.hikage.declaration.gradle.plugin%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是 Hikage 用于自动打包 `View` 声明文件的 Gradle 插件。

## 配置插件

你可以使用如下方式将此插件添加到你的项目中。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加插件。

```toml
[versions]
hikage-plugin = "<version>"

[plugins]
hikage-declaration = { id = "com.highcapable.hikage.declaration", version.ref = "hikage-plugin" }
```

在你的项目 `build.gradle.kts` 中应用插件。

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage.declaration)
}
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中应用插件。

```kotlin
plugins {
    // ...
    id("com.highcapable.hikage.declaration") version "<version>"
}
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

插件会读取当前模块 `main` 源集资源目录下的 `hikage-view-declaration`，并在打包时复制到 `META-INF`。

``` :no-line-numbers
src/
└── main
    └── resources
        └── hikage-view-declaration
            ├── widget.json
            ├── view.json
            └── ...
```

打包后的资源路径如下。

``` :no-line-numbers
META-INF/
└── hikage
    └── view-declaration
        └── <group>
            └── <module>
                ├── widget.json
                ├── view.json
                └── ...
```

原始的 `hikage-view-declaration/**` 不会继续保留在发布产物根目录中。

关于 `View` 声明文件的 JSON 格式，请参考 [hikage-compiler → View 声明文件](../library/hikage-compiler.md#view-声明文件)。

::: tip

此插件可被应用于 Android、Java 或 Kotlin JVM 项目，你可以灵活选择发布产物和发布形式。

插件仅负责打包 `View` 声明文件，不负责生成布局组件函数 (Hikage Performer)，也不会自动应用 Maven 发布插件。

在需要使用的模块中应用 [hikage-gradle-plugin](./hikage-gradle-plugin.md) 后，会自动收集依赖中的声明文件并交由 [hikage-compiler](../library/hikage-compiler.md) 生成布局组件函数 (Hikage Performer)。

:::