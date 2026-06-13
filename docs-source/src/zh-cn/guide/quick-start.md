# 快速开始

> 集成 `Hikage` 到你的项目中。

## 项目要求

项目需要使用 `Android Studio` 或 `IntelliJ IDEA` 创建且类型为 Android 或 Kotlin Multiplatform 项目并已集成 Kotlin 环境依赖。

- Android Studio (建议从 [这里](https://developer.android.com/studio) 获取最新版本)

- IntelliJ IDEA (建议从 [这里](https://www.jetbrains.com/idea) 获取最新版本)

- Kotlin 1.9.0+、Gradle 8+、Java 17+、Android Gradle Plugin 8+

### 配置存储库

`Hikage` 的依赖发布在 **Maven Central** 和我们的公共存储库中，你可以使用如下方式配置存储库。

我们推荐使用 Kotlin DSL 作为 Gradle 构建脚本语言。

在你的项目 `build.gradle.kts` 中配置存储库。

```kotlin
repositories {
    google()
    mavenCentral()
    // (可选) 你可以添加此 URL 以使用我们的公共存储库
    // 当 Sonatype-OSS 发生故障无法发布依赖时，此存储库作为备选进行添加
    // 详情请前往：https://github.com/HighCapable/maven-repository
    // 中国大陆用户请将下方的 "raw.githubusercontent.com" 修改为 "raw.gitmirror.com"
    maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
}
```

### 配置 Java 版本

在你的项目 `build.gradle.kts` 中修改 Kotlin 的 Java 版本为 17 及以上。

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```

## 极速上手

在你的项目 `gradle/libs.versions.toml` 中添加以下内容。

```toml
[versions]
# ...
hikage-plugin = "<plugin-version>"
hikage-bom = "<version>"

[plugins]
# ...
hikage = { id = "com.highcapable.hikage", version.ref = "hikage-plugin" }

[libraries]
# ...
hikage-bom = { module = "com.highcapable.hikage:hikage-bom", version.ref = "hikage-bom" }
hikage-core = { module = "com.highcapable.hikage:hikage-core" }
hikage-extension = { module = "com.highcapable.hikage:hikage-extension" }
```

- 将 `<plugin-version>` 替换为 ![hikage-plugin](https://img.shields.io/maven-central/v/com.highcapable.hikage/com.highcapable.hikage.gradle.plugin?logo=apachemaven&logoColor=orange&style=flat-square&label=hikage-plugin)
- 将 `<version>` 替换为 ![hikage-bom](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-bom?logo=apachemaven&logoColor=orange&style=flat-square&label=hikage-bom)

接下来，在你的项目 `build.gradle.kts` 中添加以下内容。

```kotlin
plugins {
    // ...
    alias(libs.plugins.hikage)
}

dependencies {
    // ...
    implementation(platform(libs.hikage.bom))
    implementation(libs.hikage.core)
    implementation(libs.hikage.extension)
}
```

点击 `Sync` 按钮同步项目后，`Hikage` 就已经成功集成到你的项目中了。

## 功能一览

整个项目分为多个模块，你可以选择你希望引入的模块作为依赖应用到你的项目中，但一定要包含 **hikage-core** 模块。

你可以点击下方对应的模块前往查看详细的功能介绍。

::: tip 版本说明

从 `1.1.0` 起，`Hikage` 开始采用统一版本进行发布，通常情况下你只需要关注同一个主版本即可，你也可以直接参考下方的 [hikage-bom](../library/hikage-bom.md) 使用 BOM 统一管理依赖版本。

详情请见 [更新日志](../about/changelog.md)。

:::

- [hikage-bom](../library/hikage-bom.md)
- [hikage-core](../library/hikage-core.md)
- [hikage-compiler](../library/hikage-compiler.md)
- [hikage-extension](../library/hikage-extension.md)
- [hikage-extension-betterandroid](../library/hikage-extension-betterandroid.md)
- [hikage-extension-compose](../library/hikage-extension-compose.md)
- [hikage-widget-foundation](../library/hikage-widget-foundation.md)
- [hikage-widget-androidx](../library/hikage-widget-androidx.md)
- [hikage-widget-material](../library/hikage-widget-material.md)

### Gradle 插件

Gradle 插件为 `Hikage` 提供了依赖装配和简化配置流程的能力，推荐优先和运行时依赖配套使用。

- [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md)
- [hikage-declaration-gradle-plugin](../plugin/hikage-declaration-gradle-plugin.md)

## Demo

你可以在 [这里](repo://tree/main/samples) 找到一些示例，查看对应的演示项目来更好地了解这些功能的运作方式，快速地挑选出你需要的功能。