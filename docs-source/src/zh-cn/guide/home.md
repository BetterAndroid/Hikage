# 介绍

> `Hikage` (发音 /ˈhɪkɑːɡeɪ/) 是一个由 Kotlin DSL 驱动的 Android View 实时运行时框架。

## 背景

这是一个由 Kotlin DSL 驱动的 Android View 运行时框架，它的设计聚焦于 **实时代码构建 UI**。

项目图标由 [MaiTungTM](https://github.com/Lagrio) 设计，名称取自 「BanG Dream It's MyGO!!!!!」 中的原创歌曲《春日影》(Haru**hikage**)。

<details><summary>为什么要...</summary>
  <div align="center">
  <img src="/images/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

  **なんで春日影レイアウト使いの？**
  </div>
</details>

它的第二层寓意为「阳光下的影子」，它象征着这个框架的设计理念：Hikage 就像影子一样，能够在不干扰开发者原有习惯和项目结构的前提下，提供一套更现代、更高效的 UI 构建系统。

Hikage 与需要彻底改变方案并重写的 Jetpack Compose 不同，它专注于原生 Android View 生态系统，将简洁、声明式的 UI 体验带入经典的 View 框架，让你能够以极快的速度构建布局，并 100% 开箱即用地支持传统和标准原生组件。

Hikage 不生产 UI 组件，它只是 Android 17 年原生 View 生态的搬运工。

## 为什么是 Hikage？

Hikage 主要适用于专注原生 Android 平台开发的开发者，自从 Kotlin 作为主要开发语言后，依然没有一套比较完美的方案能够使用 DSL 实现动态代码布局，
所以没有使用 Jetpack Compose 的项目依然需要使用原始的 XML，虽然有着 ViewBinding 的支持，但是依然不是很友好。

Hikage 的核心 API 结构参考了 [Anko](https://github.com/Kotlin/anko)、[Splitties](https://github.com/LouisCAD/Splitties) 的设计方案以及借鉴了
Jetpack Compose 的 DSL 函数命名方案，并且在此基础上进行了生态重构，使得它在使用成本上更贴近原生，写法上更贴近 Jetpack Compose。

> 各种 DSL 布局方案对比

:::: code-group
::: code-group-item Hikage

```kotlin
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
```

:::
::: code-group-item Anko、Splitties

```kotlin
verticalLayout {
    gravity = Gravity.CENTER
    textView("Hello, World!") {
        textSize = 16f
        gravity = Gravity.CENTER
    }
}.lparams(
    width = matchParent,
    height = matchParent
) {
    topMargin = dip(16)
}
```

:::
::: code-group-item Jetpack Compose

```kotlin
Column(
    modifier = Modifier.padding(top = 16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = "Hello, World!",
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
}
```

:::
::::

Hikage 的基础部分**完全不需要借助外部及额外的编译插件**，它能**即插即用**并在**任何地方创建**一个可被设置到父布局以及 `Window` 上的 `View` 对象。

Hikage **全面兼容**混合式布局，你可以在 Hikage 中嵌入 XML (使用 `R.layout` 方案装载布局)、ViewBinding 甚至是 Jetpack Compose。

相比于 Anko、Splitties 这种纯 DSL 的脚手架轻量依赖库，Hikage 带来的整套生态系统天然支持**内存级别的 AAPT2 资源解析模拟器**，能够**动态构建 `AttributeSet`**，这套方案已经通过了模拟器与实机的交互测试与 Benchmark
性能压力测试，**稳定兼容 Android 5.0.2 (API 21) ~ 17 (API 37)**，这使得不支持动态设置属性的老旧自定义组件也能通过 Hikage 重获新生。

> 示例如下

```kotlin
TextView(
    attrs = {
        android {
            // 以下内容等价于 android:text="Set text in dynamic AttributeSet"
            set("text", "Set text in dynamic AttributeSet")
            set("textSize", "16sp")
            set("gravity", "center")
            set("paddingLeft", "8dp")
            // 支持动态类型投射
            set("paddingRight", 8.dp)
        }
    }
) {
    text = "Overriden text in code"
}
```

更多使用方法可以前往 [hikage-core → XML 属性集合](../library/hikage-core.md#xml-属性集合) 以继续了解。

同时，Hikage 原生适配了 Android Studio 的布局预览功能，能够在不运行应用的情况下直接在 IDE 中预览布局效果，更多使用方法可以前往 [hikage-core → 预览布局](../library/hikage-core.md#预览布局) 以继续了解。

从现在开始，放弃并忘记 XML、ViewBinding 甚至是 `findViewById`，直接来尝试使用代码布局吧！

**不会 Jetpack Compose？没关系，今天 Hikage 就是你的 Kotlin DSL 版 XML，把你最熟悉的肌肉记忆组件重构为最现代的声明式 UI，享受同样的编写体验和更高的开发效率以及更好的运行时性能。**

Hikage 配合我们的另一个项目 [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid) 使用效果更佳，同时 Hikage 自身将自动引用其 [ui-extension](https://betterandroid.github.io/BetterAndroid/zh-cn/library/ui-extension) 作为核心依赖。

## 语言要求

推荐使用 Kotlin 作为首选开发语言，本项目完全使用 Kotlin 编写，且不再有计划兼容 Java。

文档全部的 Demo 示例代码都将使用 Kotlin 进行描述，如果你完全不会使用 Kotlin，那么你将有可能无法正常使用本项目。

## 功能贡献

本项目的维护离不开各位开发者的支持和贡献，目前这个项目处于初期阶段，可能依然存在一些问题或者缺少你需要的功能，
如果可能，欢迎提交 PR 为此项目贡献你认为需要的功能或前往 [GitHub Issues](repo://issues) 向我们提出建议。