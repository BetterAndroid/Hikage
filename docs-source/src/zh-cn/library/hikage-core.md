# hikage-core

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-core?logo=apachemaven&logoColor=orange)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-core%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android)

这是 Hikage 的核心依赖，你需要引入此模块才能使用 Hikage 的基本功能。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

### SweetDependency (推荐)

在你的项目 `SweetDependency` 配置文件中添加依赖。

```yaml
libraries:
  com.highcapable.hikage:
    hikage-core:
      version: +
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(com.highcapable.hikage.hikage.core)
```

### Version Catalog

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-core = "<version>"

[libraries]
hikage-core = { module = "com.highcapable.hikage:hikage-core", version.ref = "hikage-core" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.core)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-core:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

你可以 [点击这里](kdoc://hikage-core) 查看 KDoc。

### 基本用法

使用下方的代码创建你的第一个 Hikage 布局。

首先，使用 `Hikageable` 创建一个 `Hikage.Delegate` 对象。

> 示例如下

```kotlin
val myLayout = Hikageable {
    LinearLayout {
        TextView {
            text = "Hello, World!"
        }
    }
}
```

然后，将其设置到你想要显示的父布局或根布局上。

> 示例如下

```kotlin
// 假设这就是你的 Activity
val activity: Activity
// 实例化 Hikage 对象
val hikage = myLayout.create(activity)
// 得到根布局
val root = hikage.root
// 设置为 Activity 的内容视图
activity.setContentView(root)
```

这样我们就完成了一个简单的布局创建与设置。

### 布局约定

Hikage 的布局基本元素基于 Android 原生的 `View` 组件，所有的布局元素都可以直接使用 Android 原生的 `View` 组件进行创建。

所有布局的创建过程都会被限定在指定的作用域 `Hikage.Performer` 中，它被称为布局的 “演奏者”，即饰演布局的角色对象，这个对象可以通过以下几种方式创建并维护。

#### Hikageable

正如 [基本用法](#基本用法) 所示，`Hikageable` 可以直接创建一个 `Hikage.Delegate` 或 `Hikage` 对象，在 DSL 中，你可以得到 `Hikage.Performer` 对象对布局内容进行创建。

第一种方案，在任意地方创建。

> 示例如下

```kotlin
// myLayout 是 Hikage.Delegate 对象
val myLayout = Hikageable {
    // ...
}
// 假设这就是你的 Context
val context: Context
// 在需要 Context 的地方实例化 Hikage 对象
val hikage = myLayout.create(context)
```

第二种方案，在存在 `Context` 的地方直接创建。

> 示例如下

```kotlin
// 假设这就是你的 Context
val context: Context
// 创建布局，myLayout 是 Hikage 对象
val myLayout = Hikageable(context) {
    // ...
}
```

#### HikageBuilder

除了上述的方式以外，你还可以维护一个 `HikageBuilder` 对象来预创建布局。

首先，我们需要创建一个 `HikageBuilder` 对象并定义为单例。

> 示例如下

```kotlin
object MyLayout : HikageBuilder {

    override fun build() = Hikageable {
        // ...
    }
}
```

然后，在需要的地方使用它，可以有如下两种方案。

第一种方案，直接使用 `build` 创建 `Hikage.Delegate` 对象。

> 示例如下

```kotlin
// myLayout 是 Hikage.Delegate 对象
val myLayout = MyLayout.build()
// 假设这就是你的 Context
val context: Context
// 在需要 Context 的地方实例化 Hikage 对象
val hikage = myLayout.create(context)
```

第二种方案，使用 `Context.lazyHikage` 创建 `Hikage` 委托对象。

例如，我们可以在 `Activity` 中像 `ViewBinding` 一样使用它。

> 示例如下

```kotlin
class MyActivity : AppCompatActivity() {

    private val myLayout by lazyHikage(MyLayout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 得到根布局
        val root = myLayout.root
        // 设置为 Activity 的内容视图
        setContentView(root)
    }
}
```

### 基本布局组件

Hikage 采用与 Jetpack Compose 一致的函数式创建组件方案，它的布局使用两种基础组件完成，`View` 和 `ViewGroup` 函数，
它们分别对应于 Android 原生基于 `View` 和 `ViewGroup` 的组件。

#### View

`View` 函数的基础参数为以下三个，使用泛型定义创建的 `View` 对象类型。

如果不声明泛型类型，默认使用 `android.view.View` 作为创建的对象类型。

| 参数名称  | 描述                                                                |
| --------- | ------------------------------------------------------------------- |
| `lparams` | 布局参数，即 `ViewGroup.LayoutParams`，使用 `LayoutParams` 进行创建 |
| `id`      | 用于查找已创建对象的 ID，使用字符串定义                             |
| `init`    | `View` 的初始化方法体，作为最后一位 DSL 参数传入                    |

> 示例如下

```kotlin
View<TextView>(
    lparams = LayoutParams(),
    id = "my_text_view"
) {
    text = "Hello, World!"
    textSize = 16f
    gravity = Gravity.CENTER
}
```

#### ViewGroup

`ViewGroup` 函数的基础参数为四个，比较于 `View` 函数多了一个 `performer` 参数。

它必须声明一个泛型类型，因为 `ViewGroup` 是抽象类，需要一个具体的实现类。

`ViewGroup` 额外提供一个基于 `ViewGroup.LayoutParams` 的泛型参数，用于为子布局提供布局参数，不声明时默认使用 `ViewGroup.LayoutParams`。

| 参数名称    | 描述                                                                |
| ----------- | ------------------------------------------------------------------- |
| `lparams`   | 布局参数，即 `ViewGroup.LayoutParams`，使用 `LayoutParams` 进行创建 |
| `id`        | 用于查找已创建对象的 ID，使用字符串定义                             |
| `init`      | `ViewGroup` 的初始化方法体，作为 DSL 参数传入                       |
| `performer` | `Hikage.Performer` 对象，作为最后一位 DSL 参数传入                  |

`performer` 参数的作用是向下传递新的 `Hikage.Performer` 对象，作为子布局的创建者。

> 示例如下

```kotlin
ViewGroup<LinearLayout, LinearLayout.LayoutParams>(
    lparams = LayoutParams(),
    id = "my_linear_layout",
    // 初始化方法体将在这里使用 `init` 体现
    init = {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    // 可在这里继续创建子布局
    View()
}
```

#### LayoutParams

Hikage 中的布局均可使用 `LayoutParams` 函数设置布局参数，你可以使用以下参数创建它。

| 参数名称            | 描述                                              |
| ------------------- | ------------------------------------------------- |
| `width`             | 手动指定布局宽度                                  |
| `height`            | 手动指定布局高度                                  |
| `matchParent`       | 是否使用 `MATCH_PARENT` 作为布局宽度和高度        |
| `wrapContent`       | 是否使用 `WRAP_CONTENT` 作为布局宽度和高度        |
| `widthMatchParent`  | 仅设置宽度为 `MATCH_PARENT`                       |
| `heightMatchParent` | 仅设置高度为 `MATCH_PARENT`                       |
| `body`              | 布局参数的初始化方法体，作为最后一位 DSL 参数传入 |

在你不设置 `LayoutParams` 对象或不指定 `width` 和 `height` 时，Hikage 会自动使用 `WRAP_CONTENT` 作为布局参数。

`body` 方法体的类型来源于上层 [ViewGroup](#viewgroup) 提供的第二位泛型参数。

> 示例如下

```kotlin
View(
    // 假设上层提供的布局参数类型为 LinearLayout.LayoutParams
    lparams = LayoutParams(width = 100.dp) {
        topMargin = 20.dp
    }
)
```

如果你只需要一个横向填充的布局，可以直接使用 `widthMatchParent = true`。

> 示例如下

```kotlin
View(
    lparams = LayoutParams(widthMatchParent = true)
)
```

#### Layout

Hikage 支持引用第三方布局，你可以传入 XML 布局资源 ID、其它 Hikage 对象以及 `View` 对象，甚至是 `ViewBinding`。

> 示例如下

```kotlin
ViewGroup<...> {
    // 引用 XML 布局资源 ID
    Layout(R.layout.my_layout)
    // 引用 ViewBinding
    Layout<MyLayoutBinding>()
    // 引用另一个 Hikage 或 Hikage.Delegate 对象
    Layout(myLayout)
}
```

### 定位布局组件

Hikage 支持使用 `id` 定位组件，在上面的示例中，我们使用了 `id` 参数设置了组件的 ID。

在设置 ID 后，你可以使用 `Hikage.get` 方法获取它们。

> 示例如下

```kotlin
val myLayout = Hikageable {
    View<TextView>(id = "my_text_view") {
        text = "Hello, World!"
    }
}
// 假设这就是你的 Context
val context: Context
// 在需要 Context 的地方实例化 Hikage 对象
val hikage = myLayout.create(context)
// 获取指定的组件，返回 View 类型
val textView = hikage["my_text_view"]
// 获取指定的组件并声明组件类型
val textView = hikage.get<TextView>("my_text_view")
// 如果不确定 ID 是否存在，可以使用 `getOrNull` 方法
val textView = hikage.getOrNull<TextView>("my_text_view")
```

### 自定义布局组件

Hikage 为 Android 基础的布局组件提供了组件类名对应的函数，你可以直接使用这些函数创建组件，而无需再使用泛型声明它们，如果你需要 Jetpack 或者 Material 提供的组件，
可以引入 [hikage-widget-androidx](../library/hikage-widget-androidx.md) 或 [hikage-widget-material](../library/hikage-widget-material.md) 模块。

> 示例如下

```kotlin
LinearLayout(
    lparams = LayoutParams(),
    id = "my_linear_layout",
    init = {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    TextView(
        lparams = LayoutParams(),
        id = "my_text_view"
    ) {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
}
```

初始化后的 `View` 或 `ViewGroup` 对象会返回其自身对象类型的实例，你可以在接下来的布局中使用它们。

> 示例如下

```kotlin
val textView = TextView {
    text = "Hello, World!"
    textSize = 16f
    gravity = Gravity.CENTER
}
Button {
    text = "Click Me!"
    setOnClickListener {
        // 直接使用 textView 对象
        textView.text = "Clicked!"
    }
}
```

如果提供的组件不满足你的需求，你可以手动创建自己的组件。

> 示例如下

```kotlin
// 假设你已经定义好了你的自定义组件
class MyCustomView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    // ...
}

// 下面，创建组件对应的函数
// 自定义组件必须声明此注解
// 声明组件的注解具有传染性，在每个用于构建布局的作用域中，都需要存在此注解
@Hikageable
// 函数的命名可以随意，但是建议使用大驼峰命名
// 函数的签名部分需要固定声明为 `inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>`
inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>.MyCustomView(
    lparams: Hikage.LayoutParams? = null,
    id: String? = null,
    init: HikageView<MyCustomView> = {},
    // 如果此组件是容器，可以声明一个 `performer` 参数
    // performer: HikagePerformer<LP> = {}
) = View<MyCustomView>(lparams, id, init)
```

每次都手动实现这样复杂的函数看起来会很繁琐，如果你希望能够自动生成组件函数，可以引入并参考 [hikage-compiler](../library/hikage-compiler.md) 模块。

### 组合与拆分布局

在搭建 UI 时，我们通常会将可复用的布局作为组件来使用，如果你不想每一个部分都使用原生的自定义 `View` 将其分别定制，你可以直接将布局逻辑部分进行拆分。

Hikage 支持将布局拆分为多个部分进行组合，你可以在任何地方使用 `Hikageable` 函数创建一个新的 `Hikage.Delegate` 对象。

> 示例如下

```kotlin
// 假设这是你的主布局
val mainLayout = Hikageable {
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
        }
    ) {
        TextView {
            text = "Hello, World!"
        }
        // 组合子布局
        Layout(subLayout)
    }
}
// 假设这是你的布局子模块
// 由于上层布局使用了 LinearLayout，所以你可以为子布局声明 LinearLayout.LayoutParams
val subLayout = Hikageable<LinearLayout.LayoutParams> {
    TextView(
        lparams = LayoutParams {
            topMargin = 16.dp
        }
    ) {
        text = "Hello, Sub World!"
    }
}
```

### 状态管理

Hikage 拥有与 Jetpack Compose 类似的状态管理解决方法，它可以轻松地设置布局组件的状态监听。

Hikage 提供了两种状态，`NonNullState` 和 `NullableState`，分为持有非空和可空两种状态。

不同于 Jetpack Compose 的重组 (Recompose)，Hikage 不会重组，状态通过监听与回调生效。

你可以在如下场景中使用这两种状态。

> 示例如下

```kotlin
val myLayout = Hikageable {
    // 声明一个非空可变状态
    val mTextState = mutableStateOf("Hello, World!")
    // 声明一个可空可变状态
    val mDrawState = mutableStateOfNull<Drawable>()
    // 你可以将状态委托给一个变量
    var mText by mTextState
    var mDraw by mDrawState
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
        }
    ) {
        TextView {
            textSize = 16f
            gravity = Gravity.CENTER
            // 设置 (绑定) 状态到文本
            setState(mTextState) {
                text = it
            }
        }
        ImageView {
            // 设置 (绑定) 状态到 Drawable
            setState(mDrawState) {
                setImageDrawable(it)
            }
        }
        Button {
            text = "Click Me!"
            setOnClickListener {
                // 修改非空状态的值
                mText = "Hello, Hikage!"
                // 修改可空状态的值
                mDraw = drawableResource(R.drawable.ic_my_drawable)
            }
        }
    }
}
```

在上面的示例中，我们使用 `mutableStateOf` 声明了一个非空状态 `mTextState`，它的初始值为 `"Hello, World!"`，
然后继续使用 `mutableStateOfNull` 声明了一个可空状态 `mDrawState`，它的初始值为 `null`。

在点击按钮时，我们修改 `mTextState` 的值为 `"Hello, Hikage!"`，`mDrawState` 的值为属性资源 `R.drawable.ic_my_drawable`，
这时 `TextView` 和 `ImageView` 的文本和图片将会自动更新。

### 自定义布局装载器

Hikage 支持自定义布局装载器并同时兼容 `LayoutInflater.Factory2`，你可以通过以下方式自定义在 Hikage 布局装载过程中的事件和监听。

> 示例如下

```kotlin
val factory = HikageFactory { parent, base, context, params ->
    // 你可以在这里自定义布局装载器的行为
    // 例如，使用你自己的方式创建一个新的 View 对象
    // `parent` 为当前组件要添加到的 ViewGroup 对象，如果没有则为 `null`
    // `base` 为上一个 HikageFactory 创建的 View 对象，如果没有则为 `null`
    // `params` 对象中包含了组件 ID、AttributeSet 以及 View 的 Class 对象
    val view = MyLayoutFactory.createView(context, params)
    // 你还可以在这里对创建的 View 对象进行初始化和设置
    view.setBackgroundColor(Color.RED)
    // 返回创建的 View 对象
    // 返回 `null` 将会使用默认的组件装载方式
    view
}
```

你还可以直接传入 `LayoutInflater` 对象以自动装载并使用其中的 `LayoutInflater.Factory2`。

> 示例如下

```kotlin
// 假设这就是你的 LayoutInflater 对象
val layoutInflater: LayoutInflater
// 通过 LayoutInflater 创建 HikageFactory 对象
val factory = HikageFactory(layoutInflater)
```

然后使用以下方式将其设置到你需要装载的 Hikage 布局上。

> 示例如下

```kotlin
// 假设这就是你的 Context
val context: Context
// 创建 Hikage 对象
val hikage = Hikageable(
    context = context,
    factory = {
        // 添加自定义的 HikageFactory 对象
        add(factory)
        // 直接添加
        add { parent, base, context, params ->
            // ...
            null
        }
        // 连续添加多个
        addAll(factories)
    }
) {
    LinearLayout {
        TextView {
            text = "Hello, World!"
        }
    }
}
```

::: tip

Hikage 在默认装载时将会根据传入 `Context` 对象的 `LayoutInflater.Factory2` 对布局进行装载，如果你正在使用 `AppCompatActivity`，
布局中的组件将会自动被替换为对应的 Compat 组件或 Material 组件，与 XML 布局的特性保持一致。

如果你不需要默认生效此特性，可以使用以下方式全局关闭。

> 示例如下

```kotlin
Hikage.isAutoProcessWithFactory2 = false
```

:::

### 预览布局

Hikage 支持在 Android Studio 中预览布局，借助于 Android Studio 自带的自定义 `View` 预览插件，你可以使用以下方式预览布局。

你只需要定义一个预览布局的自定义 `View` 并继承于 `HikagePreview`。

> 示例如下

```kotlin
class MyLayoutPreview(context: Context, attrs: AttributeSet?) : HikagePreview(context, attrs) {

    override fun build() = Hikageable {
        LinearLayout {
            TextView {
                text = "Hello, World!"
            }
        }
    }
}
```

然后在你当前的窗口右侧应该会出现预览窗格，打开后点击 “Build & Refresh”，等待编译完成后将会自动显示预览。

::: tip

`HikagePreview` 实现了 `HikageBuilder` 接口，你可以在 `build` 方法中返回任意的 Hikage 布局以进行预览。

:::

::: danger

`HikagePreview` 仅支持在 Android Studio 中预览布局，请勿在运行时使用它或将其添加到任何 XML 布局中。

:::