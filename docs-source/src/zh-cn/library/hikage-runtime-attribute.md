# hikage-runtime-attribute

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-runtime-attribute?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-runtime-attribute%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

这是 Hikage 的 XML 属性集合运行时模块，其提供了内存级别的 AAPT2 资源解析模拟器。

::: warning Play 合规风险提示

由于此模块涉及对 `XmlBlock` 的反射操作，可能存在与 Google Play 合规相关的风险。如果你的应用需要上架 Google Play，请务必仔细评估使用此模块可能带来的风险，并确保你的应用符合 Google Play 的政策要求。

Hikage 不为使用此模块可能引起的任何合规问题承担责任。

:::

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

我们推荐你优先参考 [hikage-bom](./hikage-bom.md) 使用 BOM 统一管理版本。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-runtime-attribute = "<version>"

[libraries]
hikage-runtime-attribute = { module = "com.highcapable.hikage:hikage-runtime-attribute", version.ref = "hikage-runtime-attribute" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.runtime.attribute)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-runtime-attribute:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

你可以 [点击这里](kdoc://hikage-runtime-attribute) 查看 KDoc。

### 兼容性报告

以下是通过 Hikage 测试的系统版本列表和其对应的设备信息，以供参考。

| Android 版本 / API 版本 | 可用性 |      测试设备       |
| :---------------------: | :----: | :-----------------: |
|       5.0.2 (21)        |   ✅    |    Redmi Note 2     |
|        6.0 (23)         |   ✅    |      OPPO A53       |
|        7.0 (24)         |   ✅    |    Xiaomi Mi-4c     |
|       7.1.2 (25)        |   ✅    |  Samsung Galaxy S4  |
|       8.0.0 (26)        |   ✅    |      Huawei P9      |
|         9 (28)          |   ✅    |   Huawei P10 Plus   |
|         10 (29)         |   ✅    |     Hisense A9      |
|         12 (31)         |   ✅    |    Huawei nova 8    |
|         13 (33)         |   ✅    |    Xiaomi MIX 2S    |
|         14 (34)         |   ✅    |   ARM64 Emulator    |
|         15 (35)         |   ✅    | Redmi Note 12 Turbo |
|         16 (36)         |   ✅    |   Xiaomi 15 Ultra   |
|         17 (37)         |   ✅    |   ARM64 Emulator    |

### 在 Hikage 中使用

通常情况下，此模块的能力将作为 [hikage-core](./hikage-core.md) 的底座使用，你无需直接使用此模块的 API 来实现对 XML 属性集合的解析。

具体的使用方法请参考 [hikage-core → XML 属性集合](../library/hikage-core.md#xml-属性集合)。

### 在任意项目中使用

Hikage 对此模块提供了解耦的 API 能力，你可以开箱即用地在任何项目中使用此模块提供的 XML 属性集合解析能力。

#### AttributeSetResolver

我们为 `TextView` 创建一段 `android:text` 属性的 XML 资源，并将其解析为 `AttributeSet` 对象。

> 示例如下

```kotlin
// 假设这就是你的 Context 实例
val context: Context
// 创建 AttributeSet 解析器实例
val resolver = AttributeSetResolver.from(context)
// 返回一个 XmlResourceParser 对象，即 AttributeSet
val attrs = resolver.newParser(
    listOf(
        AttributeItem.from(
            name = "android:text", 
            value = AttributeItem.Value.Str("Hello dynamic attribute!")
        )
    )
)
// 创建 TextView，并传入解析后的 AttributeSet
val tv = TextView(context, attrs)
```

此时 `tv` 在装载到一个父布局或者在布局中被渲染时，就会被正确地设置上 `android:text` 属性，并显示 "Hello dynamic attribute!"。

`AttributeSetResolver` 支持传入 `AttributeResolverParams` 参数来配置其解析行为。

> 示例如下

```kotlin
// 假设这就是你的 Context 实例
val context: Context
// 假设这就是你需要构造的 AttributeItem 列表
val items: List<AttributeItem>
// 创建参数对象
val params = AttributeResolverParams(
    // 指定伪造的来源 XML 布局资源 ID，仅用于提供上下文信息，不会实际进行 inflate 操作
    sourceResId = R.layout.my_layout
)
// 创建 AttributeSet 解析器实例
val resolver = AttributeSetResolver.from(context)
// 传入参数对象来解析 AttributeSet
val attrs = resolver.newParser(items, params)
```

`AttributeSetResolver` 实现了 `Closeable` 接口，你可以在不需要使用它时调用 `close()` 方法来释放其占用的资源，或使用 `use` 来自动管理资源的释放。

> 示例如下

```kotlin
// 假设这就是你的 Context 实例
val context: Context
// 假设这就是你需要构造的 AttributeItem 列表
val items: List<AttributeItem>
// 创建 AttributeSet 解析器实例，并在 use 块中使用它
val attrs = AttributeSetResolver.from(context).use { resolver ->
    // 返回一个 XmlResourceParser 对象，即 AttributeSet
    resolver.newParser(items)
}
// 创建 TextView，并传入解析后的 AttributeSet
val tv = TextView(context, attrs)
```

`AttributeSetResolver` 还提供了一个 `release(parser)` 方法，其主要针对于 Android Studio 预览功能的 `layoutlib`，在实机运行中无需调用此方法。

#### AttributeItem

`AttributeItem` 是用于创建 `AttributeSet` 的数据类，你可以通过它来构造任意的 XML 属性集合。

`AttributeItem.Value` 提供了在构造过程中涉及到的三种类型：`Str`、`Raw` 和 `Bool`。

> 示例如下

```kotlin
val items = listOf(
    AttributeItem.from(
        name = "android:text", 
        // 最常用，直接传入字符串即可
        value = AttributeItem.Value.Str("This is a text")
    ),
    AttributeItem.from(
        name = "android:textSize", 
        // 字符串可被解析为最终的原始数值
        value = AttributeItem.Value.Str("16sp")
    ),
    AttributeItem.from(
        name = "android:padding", 
        // 字符串也可被解析为属性引用
        value = AttributeItem.Value.Str("?attr/actionBarSize")
    ),
    AttributeItem.from(
        name = "android:enabled", 
        // 只支持 true 或 false
        value = AttributeItem.Value.Bool(true)
    ),
    AttributeItem.from(
        name = "android:layout_weight", 
        // 原始数值或 ID、Enum 等类型的属性值
        value = AttributeItem.Value.Raw(1)
    )
)
```

如果你拿不准什么场景用什么，直接选 `AttributeItem.Value.Str` 就对了，XML 里面有什么，它就支持什么。

当然你也可以给定 `namespace` 来指定属性的命名空间而不是每次都需要使用 `:` 来指定。

> 示例如下

```kotlin
val item = AttributeItem.from(
    namespace = "android",
    name = "text", 
    value = AttributeItem.Value.Str("This is a text")
)
```

`AttributeItem` 实现了 `Serializable` 接口，你可以将其序列化为任意对象例如 JSON 进行传输，并在需要时反序列化回对象。