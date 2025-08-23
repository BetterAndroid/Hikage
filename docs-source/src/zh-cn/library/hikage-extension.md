# hikage-extension

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-extension?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-extension%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

这是 Hikage 针对 UI 组件相关功能的扩展依赖。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

### SweetDependency (推荐)

在你的项目 `SweetDependency` 配置文件中添加依赖。

```yaml
libraries:
  com.highcapable.hikage:
    hikage-extension:
      version: +
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(com.highcapable.hikage.hikage.extension)
```

### Version Catalog

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-extension = "<version>"

[libraries]
hikage-extension = { module = "com.highcapable.hikage:hikage-extension", version.ref = "hikage-extension" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.extension)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-extension:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

你可以 [点击这里](kdoc://hikage-extension) 查看 KDoc。

### Activity

Hikage 为 `Activity` 提供了更好用的扩展，在 `Activity` 中创建 Hikage 将会变得更加简单。

> 示例如下

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView {
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
        }
    }
}
```

借助 `Hikage` 的 `setContentView` 扩展方法，你可以像 Jetpack Compose 一样使用 `setContent` 方法来设置布局。

### Window

在 `Window` 中使用 Hikage 创建布局与 [Activity](#activity) 保持一致，你只需要使用 `setContentView` 方法传入一个 `Hikage` 布局即可。

### Dialog

如果你想直接在 `AlertDialog` 中使用 Hikage 创建布局，现在你可以使用以下方案更加简单地进行。

> 示例如下

```kotlin
// 假设这就是你的 Context
val context: Context
// 创建对话框并显示
AlertDialog.Builder(context)
    .setTitle("Hello, World!")
    .setView {
        TextView {
            text = "Hello, World!"
            textSize = 16f
        }
    }
    .show()
```

在 `AlertDialog` 中使用 Hikage 创建布局，你只需要使用 `setView` 方法传入一个 `Hikage` 布局即可。

如果你是继承于 `Dialog` 进行自定义，那么你可以和像在 [Activity](#activity) 一样使用 `setContentView` 方法。

> 示例如下

```kotlin
class CustomDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView {
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
        }
    }
}
```

### PopupWindow

你可以继承于 `PopupWindow` 进行自定义，然后使用 Hikage 创建布局，你可以和像在 [Activity](#activity) 一样使用 `setContentView` 方法。

> 示例如下

```kotlin
class CustomPopupWindow(context: Context) : PopupWindow(context) {

    init {
        setContentView(context) {
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
        }
    }
}
```

::: danger

创建 Hikage 布局的 `PopupWindow` 需要使用 `Context` 构造方法进行初始化，如果当前无法立即获取到 `Context`，请对 `setContentView` 方法传入 `Context` 实例。

:::

### ViewGroup

Hikage 对 `ViewGroup` 的 `addView` 方法进行了扩展，你可以直接使用 Hikage 布局来为当前 `ViewGroup` 快速添加新的布局。

> 示例如下

```kotlin
// 假设这就是你的 ViewGroup
val root: FrameLayout
// 添加 Hikage 布局
root.addView {
    TextView {
        text = "Hello, World!"
        textSize = 16f
    }
}
```

或者，在自定义 `View` 中使用。

> 示例如下

```kotlin
class CustomView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    init {
        addView<FrameLayout.LayoutParams> {
            TextView {
                text = "Hello, World!"
                textSize = 16f
            }
        }
    }
}
```