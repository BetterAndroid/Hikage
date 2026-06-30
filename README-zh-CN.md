# Hikage

[![GitHub license](https://img.shields.io/github/license/BetterAndroid/Hikage?color=blue&style=flat-square)](https://github.com/BetterAndroid/Hikage/blob/main/LICENSE)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/BetterAndroid)
[![Telegram](https://img.shields.io/badge/discussion%20dev-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/HighCapable_Dev)
[![QQ](https://img.shields.io/badge/discussion%20dev-QQ-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

<img src="img-src/icon.svg" width = "100" height = "100" alt="LOGO"/>

一个由 Kotlin DSL 驱动的 Android View 实时运行时框架。

[English](README.md) | 简体中文

| <img src="https://github.com/BetterAndroid/.github/blob/main/img-src/logo.png?raw=true" width = "30" height = "30" alt="LOGO"/> | [BetterAndroid](https://github.com/BetterAndroid) |
|---------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|

这个项目属于上述组织，**点击上方链接关注这个组织**，发现更多好项目。

## 这是什么

`Hikage` (发音 /ˈhɪkɑːɡeɪ/)，这是一个由 Kotlin DSL 驱动的 Android View 运行时框架，它的设计聚焦于 **实时代码构建 UI**。

项目图标由 [MaiTungTM](https://github.com/Lagrio) 设计，名称取自 「BanG Dream It's MyGO!!!!!」 中的原创歌曲《春日影》(Haru**hikage**)。

<details><summary>为什么要...</summary>
  <div align="center">
  <img src="img-src/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

**なんで春日影レイアウト使いの？**
  </div>
</details>

它的第二层寓意为「阳光下的影子」，它象征着这个框架的设计理念：Hikage 就像影子一样，能够在不干扰开发者原有习惯和项目结构的前提下，提供一套更现代、更高效的 UI
构建系统。

Hikage 与需要彻底改变方案并重写的 Jetpack Compose 不同，它专注于原生 Android View 生态系统，将简洁、声明式的 UI 体验带入经典的 View 框架，让你能够以极快的速度构建布局，并
100% 开箱即用地支持传统和标准原生组件。

Hikage 不生产 UI 组件，它只是 Android 17 年原生 View 生态的搬运工。

## 为什么是 Hikage？

Hikage 主要适用于专注原生 Android 平台开发的开发者，自从 Kotlin 作为主要开发语言后，依然没有一套比较完美的方案能够使用 DSL 实现动态代码布局，
所以没有使用 Jetpack Compose 的项目依然需要使用原始的 XML，虽然有着 ViewBinding 的支持，但是依然不是很友好。

Hikage 的核心 API 结构参考了 [Anko](https://github.com/Kotlin/anko)、[Splitties](https://github.com/LouisCAD/Splitties) 的设计方案以及借鉴了 Jetpack
Compose 的 DSL
函数命名方案， 并且在此基础上进行了生态重构，使得它在使用成本上更贴近原生，写法上更贴近 Jetpack Compose。

> 各种 DSL 布局方案对比

<details open><summary>Hikage</summary>

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

</details>

<details><summary>Anko、Splitties</summary>

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

</details>

<details><summary>Jetpack Compose</summary>

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

</details>

Hikage 的基础部分**完全不需要借助外部及额外的编译插件**，它能**即插即用**并在**任何地方创建**一个可被设置到父布局以及 `Window` 上的 `View` 对象。

Hikage **全面兼容**混合式布局，你可以在 Hikage 中嵌入 XML (使用 `R.layout` 方案装载布局)、ViewBinding 甚至是 Jetpack Compose。

相比于 Anko、Splitties 这种纯 DSL
的脚手架轻量依赖库，Hikage
带来的整套生态系统天然支持**内存级别的 AAPT2 资源解析模拟器**，能够**动态构建 `AttributeSet`**，这套方案已经通过了模拟器与实机的交互测试与 Benchmark
性能压力测试，**稳定兼容
Android 5.0.2 (API 21) ~ 17 (API 37)**，这使得不支持动态设置属性的老旧自定义组件也能通过 Hikage 重获新生。

> 示例如下

```kotlin
TextView(
    attrs = {
        android {
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

**同时，Hikage 原生适配了 Android Studio 的布局预览功能，能够在不运行应用的情况下直接在 IDE 中预览布局效果。**

从现在开始，放弃并忘记 XML、ViewBinding 甚至是 `findViewById`，直接来尝试使用代码布局吧！

**不会 Jetpack Compose？没关系，今天 Hikage 就是你的 Kotlin DSL 版
XML，把你最熟悉的肌肉记忆组件重构为最现代的声明式 UI，享受同样的编写体验和更高的开发效率以及更好的运行时性能。**

Hikage 配合我们的另一个项目 [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid) 使用效果更佳，同时 Hikage
自身将自动引用其 [ui-extension](https://betterandroid.github.io/BetterAndroid/zh-cn/library/ui-extension) 作为核心依赖。

## 开始使用

| <img src="img-src/icon.svg" width = "30" height = "30" alt="LOGO"/> | [Hikage 文档](https://betterandroid.github.io/Hikage/zh-cn) |
|---------------------------------------------------------------------|-----------------------------------------------------------|

你可以前往文档页面查看更多详细教程和内容。

### 下一步做什么？

1. **引入依赖**: 将 **hikage-core** 依赖和你需要的依赖添加到你的项目中。
2. **同步项目**: 在 Gradle 同步后，你就可以开始使用 `Hikage` 了。

在打开的页面中，选择侧边栏的 **快速开始** 章节以继续阅读。

## 更多项目

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>嘿，还请君留步！👋</h2>
    <h3>如果你觉得这个项目能给你提供帮助，不妨继续往下看看我的更多项目吧！</h3>
    <h3>如果这些项目能为你提供帮助，不妨为我点个关注或者 star ⭐️ 吧！</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README-zh-CN.md">→ 查看更多关于我的项目，请点击这里 ←</a></h1>
</div>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=BetterAndroid/Hikage&type=Date)

## 第三方开源使用声明

- [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass)

## 许可证

- [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Apache License Version 2.0

Copyright (C) 2019 HighCapable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

版权所有 © 2019 HighCapable