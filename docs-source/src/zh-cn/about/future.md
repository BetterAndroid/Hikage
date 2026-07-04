# 展望未来

> 未来是美好的，也是不确定的，让我们共同期待 `Hikage` 在未来的发展空间。

## 未来的计划

> 这里收录了 `Hikage` 可能会在后期添加的功能。

### 面向 IDE 的插件

Hikage 会推出针对 Android Studio 的官方插件，它将作为独立项目，计划支持以下功能。

#### 项目模板能力

IDE 插件将提供一个 Hikage 项目的模板，允许开发者快速创建一个基于 Hikage 的 Android 项目。

#### 语法树能力

IDE 插件将实现将 `@Hikagable` 注解的函数大驼峰命名识别为标准规范，不再出现 `FunctionName`、`PropertyName` 检查提示 (警告)。

支持在内存中合成虚拟 Kotlin Stubs，并对 KSP 等组件函数 (Hikage Performer) 在编辑器中提供代码补全、跳转、重构等功能。

#### 布局转换能力

IDE 插件将提供布局转换能力，允许开发者将 XML 布局文件转换为 Hikage Kotlin DSL，你可以选择复制为完整的代码或是创建 `HikageBuilder` 对象。

同时基本的属性都将被翻译为 `attrs` 块的内容。

#### 布局组件能力

在 Hikage 中，获取一个组件的 ID 需要在构建时通过 `id` 参数定义，然后通过 `Hikage.get<T>("view_id")` 获取。

IDE 插件将提供一个能力，允许开发者在编码时直接补全通过字符串生成的组件 ID 进行调用，实现类型安全，并配合 Kotlin 编译器插件/FIR 扩展或生成代码能力实现编译期访问器。

> 示例如下

```kotlin
val myLayout = Hikagable(context) {
    LinearLayout(
        id = "lin_layout",
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
    ) {
        TextView(id = "text_view") {
            text = "Hello, World!"
            textSize = 16f
            gravity = Gravity.CENTER
        }
    }
}

// 直接调用根据字符串生成的 ID
val linLayout = myLayout.linLayout
val textView = myLayout.textView
// 获取根布局，即 LinearLayout，不再需要 root<LinearLayout>()
val root = myLayout.root
```

#### 布局调试能力

IDE 插件将计划探索接入 Android Studio 的 Preview Integration 在任意地方预览 Hikage 的布局，并在后期逐渐支持 Component Tree 组件树和 Palette 组件库的可视化操作。

在初期，IDE 插件将提供 Tool Window 面板，实现基础的布局预览能力。

同时也将支持 Layout Inspector 的组件追溯能力，允许开发者在运行时追溯到 Hikage 组件的源代码位置。

#### Performer 扩展能力 (待定)

IDE 插件将提供对 Hikage Performer 的扩展能力，开发者将可以在创建函数时省略 `Hikage.Performer` 接收者和 LP 泛型，被 `@Hikagable` 注解的函数将自动被识别，实现与 Jetpack Compose 的 `@Composable` 注解类似的功能。

例如声明一个 `@Hikagable` 注解的函数，IDE 插件将自动识别它。

> 示例如下

```kotlin
@Hikagable
fun MyTextView(text: String) {
    TextView {
        this.text = text
    }
}
```

你无须在函数参数中显式定义 `performer` 参数，它是当前函数的隐式接收者，在函数中使用 `performer` 即可继续传递子布局。

> 示例如下

```kotlin
@Hikagable
fun MyLayout() {
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
    ) {
        performer()
    }
}

val myLayout = Hikagable(context) {
    MyLayout {
        MyTextView("Hello, World!")
    }
}
```