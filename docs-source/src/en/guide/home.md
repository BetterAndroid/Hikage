# Introduction

> `Hikage` (Pronunciation /ˈhɪkɑːɡeɪ/) is a Kotlin DSL-based Android real-time UI building framework.

## Background

This is a Kotlin DSL-based Android UI building framework that focuses on **real-time code-based UI construction**.

The project icon was designed by [MaiTungTM](https://github.com/Lagrio),
the name is taken from the original song "Haru**hikage**" in "BanG Dream It's MyGO!!!!!".

<details><summary>Why...</summary>
  <div align="center">
  <img src="/images/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

  **なんで春日影レイアウト使いの？**
  </div>
</details>

Its second layer of meaning is "Shadow under the Sun",
which symbolizes the design philosophy of this framework: Hikage is like a shadow,
able to provide a more modern and efficient way of building UI without interfering with developers' existing habits and project structure.

Unlike Jetpack Compose which demands a complete paradigm shift and rewrite,
Hikage is laser-focused on the native Android view ecosystem.
It brings the sleek, declarative UI DX to the classic view framework,
allowing you to build layouts blazing fast with 100% out-of-the-box support for legacy and standard native components.

Hikage does not produce UI components, it is just a transporter of the Android 17-year-old native view ecosystem.

## Why Hikage?

Hikage is mainly suitable for developers focusing on native Android platform development.
Since Kotlin became the primary development language, there hasn't been a perfect solution to implement dynamic code layouts using DSL.
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

Compared with Anko and Splitties, Hikage supports an **in-memory AAPT2 resource parsing emulator**,
capable of **dynamically constructing an `AttributeSet`**. This solution has been tested on emulators and real devices,
ensuring **stable compatibility with Android 5.0.2 (API 21) ~ 17 (API 37)**.
This empowers legacy custom views that lack programmatic setters to be revitalized via Hikage.

> The following example

```kotlin
TextView(
    attrs = {
        android {
            // The following is equivalent to android:text="Set text in dynamic AttributeSet".
            set("text", "Set text in dynamic AttributeSet")
            set("textSize", "16sp")
            set("gravity", "center")
            set("paddingLeft", "8dp")
            // Supports dynamic type conversion.
            set("paddingRight", 8.dp)
        }
    }
) {
    text = "Overridden text in code"
}
```

For more usage details, please refer to [hikage-core → XML Attribute Sets](../library/hikage-core.md#xml-attribute-sets).

From now on, forget about ViewBinding, XML, and even `findViewById`, and just try using code-based layouts!

**Don't know Jetpack Compose? No worries, today Hikage is your Kotlin DSL version of XML,
refactoring your most familiar muscle-memory components into the most modern declarative UI,
enjoying the same writing experience with higher development efficiency and better runtime performance.**

Hikage works best when used in conjunction with our other project [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid), and
Hikage itself will automatically reference its [ui-extension](https://betterandroid.github.io/BetterAndroid/en/library/ui-extension) as a core dependency.

## Language Requirement

It is recommended to use Kotlin as the preferred development language. This project is entirely written in Kotlin, and there are no plans to support Java compatibility.

All demo examples in the documentation will be described using Kotlin. If you are not familiar with Kotlin, you may encounter difficulties in using this project effectively.

## Contribution

The maintenance of this project is inseparable from the support and contributions of all developers.

This project is currently in its early stages, and there may still be some problems or lack of functions you need.

If possible, feel free to submit a PR to contribute features you think are needed to this project, or go to [GitHub Issues](repo://issues)
to make suggestions to us.