# hikage-compiler

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-compiler?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-compiler%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

这是 Hikage 的自动化编译模块。

::: tip

通常情况下，我们推荐直接使用 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 自动装配此模块。

:::

## 配置依赖

如果你希望手动接管 KSP 与编译器依赖，可以使用如下方式将此模块添加到你的项目中。

我们推荐你优先参考 [hikage-bom](./hikage-bom.md) 使用 BOM 统一管理版本。

::: warning

你需要在你的项目中集成适合于你项目当前 Kotlin 版本的 [Google KSP](https://github.com/google/ksp/releases) 插件。

:::

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
ksp = "<ksp-version>"
hikage-compiler = "<version>"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
hikage-compiler = { module = "com.highcapable.hikage:hikage-compiler", version.ref = "hikage-compiler" }
```

在你的根项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp) apply false
}
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp)
}

dependencies {
    // ...
    ksp(libs.hikage.compiler)
}
```

请将 `<version>` 修改为此文档顶部显示的版本，并将 `<ksp-version>` 修改为你项目当前使用的 Kotlin 版本对应的 KSP 版本。

### 传统方式

在你的根项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp") version "<ksp-version>" apply false
}
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp")
}

dependencies {
    // ...
    ksp("com.highcapable.hikage:hikage-compiler:<version>")
}
```

请将 `<version>` 修改为此文档顶部显示的版本，并将 `<ksp-version>` 修改为你项目当前使用的 Kotlin 版本对应的 KSP 版本。

## 功能介绍

Hikage 的编译模块会在编译时自动生成代码，在更新后，请重新运行 `assembleDebug` 或 `assembleRelease` Task 以生成最新的代码。

Hikage 可以在编译时为指定的布局组件自动生成对应的布局组件函数 (Hikage Performer)。

### 生成自定义组件

你可以在你的自定义 `View` 上加入 `HikageView` 注解，以标记它生成为 Hikage 布局组件。

| 参数名称    | 描述                                                                                                                                             |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `lparams`   | 布局参数 `ViewGroup.LayoutParams` Class 对象，如果你的自定义 `View` 是 `ViewGroup` 的子类，则可以声明或留空使用默认值                            |
| `alias`     | 布局组件的别名，即要生成的函数名称，默认获取当前 Class 的名称                                                                                    |
| `attrs`     | 是否为生成的布局组件函数添加 `attrs` 参数，默认添加                                                                                              |
| `init`      | 是否为生成的布局组件函数添加 `init` 参数，默认添加                                                                                               |
| `performer` | 是否为生成的布局组件函数添加 `performer` 参数，默认添加，即设置为 `false` 后，此布局是否是 `ViewGroup` 还是从其继承都将不会生成 `performer` 参数 |

> 示例如下

```kotlin
@HikageView(lparams = LinearLayout.LayoutParams::class)
class MyLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    // ...
}
```

编译后，你就可以在 Hikage 布局中使用 `MyLayout` 作为布局组件了。

> 示例如下

```kotlin
Hikagable {
    MyLayout {
        TextView(
            lparams = LayoutParams {
                topMargin = 16.dp
            }
        ) {
            text = "Hello, World!"
        }
    }
}
```

### 生成第三方组件

Hikage 同样可以为第三方提供的 `View` 组件自动生成布局组件函数 (Hikage Performer)，你可以使用 `HikageViewDeclaration` 注解来完成。

| 参数名称    | 描述                                                                                                                                             |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `view`      | 需要声明的布局组件的 Class 对象                                                                                                                  |
| `lparams`   | 布局参数 `ViewGroup.LayoutParams` Class 对象，如果你的自定义 `View` 是 `ViewGroup` 的子类，则可以声明或留空使用默认值                            |
| `alias`     | 布局组件的别名，即要生成的函数名称，默认获取 `view` Class 的名称                                                                                 |
| `attrs`     | 是否为生成的布局组件函数添加 `attrs` 参数，默认添加                                                                                              |
| `init`      | 是否为生成的布局组件函数添加 `init` 参数，默认添加                                                                                               |
| `performer` | 是否为生成的布局组件函数添加 `performer` 参数，默认添加，即设置为 `false` 后，此布局是否是 `ViewGroup` 还是从其继承都将不会生成 `performer` 参数 |

> 示例如下

```kotlin
@HikageViewDeclaration(ThirdPartyView::class)
object ThirdPartyViewDeclaration
```

这个注解可以声明到任意一个 `object` 类上，仅作为注解扫描器需要自动纳入的类来使用，你可以将可见性设为 `private`，但要确保被注解的类一定是使用 `object` 修饰的。

同样地，编译后，你就可以在 Hikage 布局中使用 `ThirdPartyView` 作为布局组件了。

> 示例如下

```kotlin
Hikagable {
    ThirdPartyView {
        // ...
    }
}
```

### Maven 发布配置

KSP 生成的源码文件会被放置在 `build/generated/ksp` 目录下，这些源码文件将不会默认发布到 Maven 仓库中，你可以按照以下方案进行配置，以便在你的项目或者第三方依赖库中能够索引到 Hikage 生成的布局组件函数 (Hikage Performer)。

建议在需要发布的项目的 `build.gradle.kts` 中添加如下配置。

> 示例如下

```kotlin
android {
    // 过滤发布任务的 Task 名称，避免在 assemble 时也将 KSP 生成的源码文件添加到源集
    if (gradle.startParameter.taskNames.any { it.startsWith("publish") })
        sourceSets.configureEach {
            val kspSources = layout.buildDirectory.dir("generated/ksp/release").get().asFile
            if (kspSources.exists()) kotlin.directories += kspSources.path
        }
}
```

### View 声明文件

除了使用 `HikageViewDeclaration` 注解，你还可以通过 `View` 声明文件的方式来声明需要生成布局组件函数 (Hikage Performer) 的第三方 `View` 组件。

通常情况下，我们建议通过 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 自动收集声明文件。

如果你正在发布声明模块，推荐使用 [hikage-declaration-gradle-plugin](../plugin/hikage-declaration-gradle-plugin.md) 来完成声明文件资源的自动打包工作。

如果你希望手动接管 KSP 参数，可以通过 `hikage.viewDeclarationFiles` 传入一个或多个 `View` 声明文件或目录路径，多个路径使用 `File.pathSeparator` 分隔，当传入目录时，编译器会递归扫描其中所有 `.json` 文件。

> 示例如下

```kotlin
ksp {
    arg(
        "hikage.viewDeclarationFiles",
        listOf(
            file("path/to/widgets.json").absolutePath,
            file("path/to/hikage-view-declaration").absolutePath
        ).joinToString(File.pathSeparator)
    )
}
```

`hikage.viewDeclarationFiles` 属于严格声明入口，如果路径指向不存在的 `.json` 文件，或 JSON 中声明的 `viewClass` / `lparams` 无法解析，编译会直接失败。

此时你可以使用 `hikage.optionalViewDeclarationFiles` 来实现可选声明入口的功能，在无法解析时编译器会跳过当前项并输出 `info` 级别的日志。

> 示例如下

```kotlin
ksp {
    arg(
        "hikage.optionalViewDeclarationFiles",
        file("path/to/dependency-declarations").absolutePath
    )
}
```

声明文件必须是 JSON 数组，每一项都是一个需要生成布局组件函数 (Hikage Performer) 的 `View` 声明。

> 示例如下

```json
[
    {
        "viewClass": "com.google.android.material.button.MaterialButton",
        "alias": "MaterialButton",
        "attrs": true,
        "init": true,
        "performer": true
    },
    {
        "viewClass": "android.widget.LinearLayout",
        "lparams": "android.widget.LinearLayout.LayoutParams"
    }
]
```

| 参数名称    | 描述                                                                                                                                             |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| `viewClass` | 需要声明的布局组件完整 Class 名称，此项为必填，会在编译过程中找不到对应的类时抛出异常                                                            |
| `lparams`   | 布局参数 `ViewGroup.LayoutParams` Class 对象，如果你的自定义 `View` 是 `ViewGroup` 的子类，则可以声明或留空使用默认值                            |
| `alias`     | 布局组件的别名，即要生成的函数名称，默认获取当前 Class 的名称                                                                                    |
| `attrs`     | 是否为生成的布局组件函数添加 `attrs` 参数，默认添加                                                                                              |
| `init`      | 是否为生成的布局组件函数添加 `init` 参数，默认添加                                                                                               |
| `performer` | 是否为生成的布局组件函数添加 `performer` 参数，默认添加，即设置为 `false` 后，此布局是否是 `ViewGroup` 还是从其继承都将不会生成 `performer` 参数 |

::: tip

Hikage 生成布局组件函数 (Hikage Performer) 的包名路径为 `com.highcapable.hikage.widget` + 你的 `View` 或第三方 `View` 组件的完整包名。

同一个 `View` 存在 `HikageView` 或 `HikageViewDeclaration` 注解声明时，注解声明会优先于 `View` 声明文件。

:::

::: danger

为了确保生成的组件具有原生级别的构造性能，使用 `HikageView`、`HikageViewDeclaration` 注解和 `View` 声明文件定义的第三方组件必须完整支持 `(Context, AttributeSet?)` 构造方法，且必须满足第一位参数不可为 `null` 第二位参数可为 `null` 的条件，否则编译器将在生成过程中抛出异常。

:::