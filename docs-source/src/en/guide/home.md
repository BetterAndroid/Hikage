# Introduction

> `Hikage` (Pronunciation /ˈhɪkɑːɡeɪ/) is a real-time Android View runtime powered by Kotlin DSL.

## Background

This is an Android View runtime powered by Kotlin DSL, designed around **real-time code-based UI construction**.

The project icon was designed by [MaiTungTM](https://github.com/Lagrio),
the name is taken from the original song "Haru**hikage**" in "BanG Dream It's MyGO!!!!!".

<details><summary>Why...</summary>
  <div align="center">
  <img src="/images/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

  **なんで春日影レイアウトしたの？**
  </div>
</details>

Its second layer of meaning is "Shadow under the Sun",
which symbolizes the design philosophy of this framework:
Hikage is like a shadow, providing a more modern and efficient UI construction system without interfering with the developer's original habits and project structure.

Hikage is focused on the native Android View ecosystem.
It brings the Kotlin DSL-driven layout experience into the classic View framework,
allowing you to build layouts at high speed with 100% out-of-the-box support for legacy and standard native components.

Hikage does not produce UI components, it is just a transporter of the Android 17-year-old native view ecosystem.

## Why Hikage?

Hikage is mainly intended for developers focused on the native Android platform.
Since Kotlin became the primary development language, there still has not been a truly complete solution for dynamic code-based layouts.
Projects that do not use Jetpack Compose still have to rely on XML, and even with ViewBinding, the experience is still not especially friendly.

Hikage keeps the native Android View naming style, while drawing inspiration from [Anko](https://github.com/Kotlin/anko), [Splitties](https://github.com/LouisCAD/Splitties), and the Jetpack Compose DSL design philosophy.
This makes it feel close to native Android while still offering a modern Kotlin DSL authoring experience.

> The following example

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

Hikage can be **plug-and-play** and create a `View` **anywhere** that can be attached to a parent layout or even a `Window`.

It is also **fully compatible** with hybrid layouts. You can embed XML (loaded via the `R.layout` scheme), ViewBinding, and even Jetpack Compose inside Hikage.

> The following example

```kotlin
LinearLayout(
    lparams = LayoutParams(matchParent = true),
    init = {
        orientation = LinearLayout.VERTICAL
    }
) {
    // Embed an XML layout.
    Layout(R.layout.my_layout)

    // Embed a ViewBinding layout.
    Layout<MyLayoutBinding>()

    // Embed a Jetpack Compose layout.
    ComposeView {
        Text("Hello, World!")
    }
}
```

Compared to pure DSL scaffolding lightweight dependency libraries like Anko and Splitties, Hikage's ecosystem naturally supports **dynamically constructing an `AttributeSet`**.
This solution has passed interactive tests and benchmark performance stress tests on emulators and real devices, ensuring **stable compatibility with Android 5.0.2 (API 21) ~ 17 (API 37)**.
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

Furthermore, Hikage natively supports Android Studio's layout preview feature,
allowing you to preview layout effects directly in the IDE without running the app.
For more usage details, please refer to [hikage-core → Preview Layout](../library/hikage-core.md#preview-layout).

From now on, forget about XML, ViewBinding, and even `findViewById`, and try building layouts directly in code.

**Don't know Jetpack Compose? No problem. Hikage can be your Kotlin DSL version of XML today, bringing the static XML layout experience into runtime and letting you build native Android Views with Kotlin.**

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