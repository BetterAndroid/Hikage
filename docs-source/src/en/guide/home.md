# Introduction

> `Hikage` (Pronunciation /ˈhɪkɑːɡeɪ/) is an Android responsive UI building tool.

## Background

This is an Android responsive UI build tool designed to focus on **real-time code-based UI building**.

The project icon was designed by [MaiTungTM](https://github.com/Lagrio),
the name is taken from the original song "Haru**hikage**" in "BanG Dream It's MyGO!!!!!".

<details><summary>Why...</summary>
  <div align="center">
  <img src="/images/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

  **なんで春日影レイアウト使いの？**
  </div>
</details>

Unlike Jetpack Compose's declarative UI, Hikage focuses on Android native platforms,
and its design goal is to enable developers to quickly build UIs and directly support Android native components.

**<u>Hikage is just a UI build tool and does not provide any UI components themselves</u>**.

Rejecting duplicate wheels, our solution is always compatible and efficient. Now you can abandon ViewBinding and XML, and even `findViewById`, and try
using code layout directly.

`Hikage` works better with another project [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid), and
`Hikage` itself will automatically reference the `BetterAndroid`-related dependencies as the core content.

## Usage

Hikage is mainly suitable for developers focusing on native Android platform development.
Since Kotlin became the primary development language, there hasn't been a perfect tool to implement dynamic code layouts using DSL.
Therefore, projects that do not use Jetpack Compose still need to use the original XML. Although ViewBinding provides support, it is still not very user-friendly.

Hikage inherits the design schemes of [Anko](https://github.com/Kotlin/anko) and [Splitties](https://github.com/LouisCAD/Splitties),
and draws on the DSL function naming scheme of Jetpack Compose. On this basis, it has made many improvements,
making it closer to native in terms of usage cost and closer to Jetpack Compose in terms of writing style.

> Comparison of various DSL layout schemes

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

The basic part of Hikage **does not require any external or additional compilation plugins**.
It can be **plug-and-play** and **create a View object anywhere** that can be set to the parent layout and `Window`.

Hikage **fully supports** hybrid layouts. You can embed XML (using the `R.layout` scheme to load layouts), ViewBinding, and even Jetpack Compose within Hikage.

## Language Requirement

It is recommended to use Kotlin as the preferred development language. This project is entirely written in Kotlin, and there are no plans to support Java compatibility.

All demo examples in the documentation will be described using Kotlin. If you are not familiar with Kotlin, you may encounter difficulties in using this project effectively.

## Contribution

The maintenance of this project is inseparable from the support and contributions of all developers.

This project is currently in its early stages, and there may still be some problems or lack of functions you need.

If possible, feel free to submit a PR to contribute features you think are needed to this project, or go to [GitHub Issues](repo://issues)
to make suggestions to us.