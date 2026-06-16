# R8 与 Proguard 混淆

> 大部分场景下应用程序安装包可通过混淆压缩体积，这里介绍了混淆规则的配置方法。

`Hikage` 自身不需要额外配置混淆规则，由于 Hikage 装载的 `View` 不需要在 XML 中被定义，它们也可以同样被混淆。

## 保留自定义构造方法

如果自定义 `View` 没在 XML 中被定义，R8 可能会移除它没有直接发现的构造方法。为了让 Hikage 能够继续通过 `(Context)` 或 `(Context, AttributeSet)` 构造方法创建自定义 `View`，你需要添加以下规则。

这条规则只保留构造方法，仍然允许类名被混淆。

```
-keep,allowobfuscation class * extends android.view.View {
    <init>(...);
}
```

::: tip

如果你使用了通过 [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) 或 [hikage-compiler](../library/hikage-compiler.md) 生成的组件函数，则不再需要上述规则。

:::

如果你提供了自定义 `ViewGroup.LayoutParams`，并且它只会通过 Hikage 的 `LayoutParams` DSL 创建，也需要保留它的构造方法。

```
-keep,allowobfuscation class * extends android.view.ViewGroup$LayoutParams {
    <init>(...);
}
```

## HikageAttribute 字符串引用

`HikageAttribute` 的字符串资源引用与 XML 写法保持一致。

```kotlin
HikageAttribute {
    app {
        set("dataSets", "@array/simple_string_data")
    }
}
```

这种写法会在运行时通过资源名称解析资源，R8 代码混淆不会影响它，但是如果你配置了 `isShrinkResources = true`，且目标资源只通过这种字符串被引用，资源压缩器可能会认为它没有被使用并将其移除。

此时你可以使用直接传递资源 ID 的方式来解决这个问题。

```kotlin
HikageAttribute {
    app {
        set("dataSets", R.array.simple_string_data)
    }
}
```

如果你必须保留 XML 风格的字符串引用，请在应用模块添加资源保留文件，例如 `res/raw/my_res_keep.xml`。

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@array/simple_string_data" />
```

同理，自定义 `attrs.xml` 的 `enum`、`flag` 和 `reference` 值如果只通过字符串动态引用，也应使用资源 ID 或通过 `tools:keep` 保留对应资源。