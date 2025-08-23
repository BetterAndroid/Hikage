# hikage-compiler

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-compiler?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-compiler%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

这是 Hikage 的自动化编译模块。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

::: warning

你需要在你的项目中集成适合于你项目当前 Kotlin 版本的 [Google KSP](https://github.com/google/ksp/releases) 插件。

:::

### SweetDependency (推荐)

在你的项目 `SweetDependency` 配置文件中添加依赖。

```yaml
plugins:
  com.google.devtools.ksp:
    version: +

libraries:
  com.highcapable.hikage:
    hikage-compiler:
      version: +
```

在你的根项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    autowire(libs.plugins.com.google.devtools.ksp) apply false
}
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
plugins {
    // ...
    autowire(libs.plugins.com.google.devtools.ksp)
}

dependencies {
    // ...
    ksp(com.highcapable.hikage.hikage.compiler)
}
```

### Version Catalog

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

Hikage 的编译模块将在运行时自动生成代码，在更新后，请重新运行 `assembleDebug` 或 `assembleRelease` Task 以生成最新的代码。

### 生成布局组件

Hikage 可以在编译时为指定的布局组件自动生成布局组件对应的 `Hikageable` 函数。

#### 自定义 View

你可以在你的自定义 `View` 上加入 `HikageView` 注解，以标记它生成为 Hikage 布局组件。

| 参数名称           | 描述                                                                                                                                                                          |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `lparams`          | 布局参数 `ViewGroup.LayoutParams` Class 对象，如果你的自定义 `View` 是 `ViewGroup` 的子类，则可以声明或留空使用默认值                                                         |
| `alias`            | 布局组件的别名，即要生成的函数名称，默认获取当前 Class 的名称                                                                                                                 |
| `requireInit`      | 是否要求填写布局的初始化方法块，默认为可省略的参数                                                                                                                            |
| `requirePerformer` | 是否要求填写布局的 `performer` 方法块，默认为可省略的参数，仅在你的自定义 `View` 是 `ViewGroup` 的子类时生效                                                                  |
| `final`            | 是否将布局声明为 “最终布局”，默认否，即此布局是否是 `ViewGroup` 还是从其继承都将不会生成 `performer` 方法块，设置为 `true` 之后，`lparams` 和 `requirePerformer` 将不再有效。 |

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
Hikageable {
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

#### 第三方组件

Hikage 同样可以为第三方提供的 `View` 组件自动生成布局组件函数，你可以使用 `HikageViewDeclaration` 注解来完成。

| 参数名称           | 描述                                                                                                                                                                          |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `view`             | 需要声明的布局组件的 Class 对象                                                                                                                                               |
| `lparams`          | 布局参数 `ViewGroup.LayoutParams` Class 对象，如果你的自定义 `View` 是 `ViewGroup` 的子类，则可以声明或留空使用默认值                                                         |
| `alias`            | 布局组件的别名，即要生成的函数名称，默认获取 `view` Class 的名称                                                                                                              |
| `requireInit`      | 是否要求填写布局的初始化方法块，默认为可省略的参数                                                                                                                            |
| `requirePerformer` | 是否要求填写布局的 `performer` 方法块，默认为可省略的参数，仅在你的自定义 `View` 是 `ViewGroup` 的子类时生效                                                                  |
| `final`            | 是否将布局声明为 “最终布局”，默认否，即此布局是否是 `ViewGroup` 还是从其继承都将不会生成 `performer` 方法块，设置为 `true` 之后，`lparams` 和 `requirePerformer` 将不再有效。 |

> 示例如下

```kotlin
@HikageViewDeclaration(ThirdPartyView::class)
object ThirdPartyViewDeclaration
```

这个注解可以声明到任意一个 `object` 类上，仅作为注解扫描器需要自动纳入的类来使用，你可以将可见性设为 `private`，但要确保被注解的类一定是使用 `object` 修饰的。

同样地，编译后，你就可以在 Hikage 布局中使用 `ThirdPartyView` 作为布局组件了。

> 示例如下

```kotlin
Hikageable {
    ThirdPartyView {
        // ...
    }
}
```

::: tip

Hikage 生成布局组件的函数包名路径为 `com.highcapable.hikage.widget` + 你的 `View` 或第三方 `View` 组件的完整包名。

:::