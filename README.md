# Hikage

[![GitHub license](https://img.shields.io/github/license/BetterAndroid/Hikage?color=blue&style=flat-square)](https://github.com/BetterAndroid/Hikage/blob/main/LICENSE)
[![Telegram](https://img.shields.io/badge/discussion%20dev-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/BetterAndroid_Dev)
[![QQ](https://img.shields.io/badge/discussion%20dev-QQ-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

<img src="img-src/icon.svg" width = "100" height = "100" alt="LOGO"/>

A real-time Android View runtime powered by Kotlin DSL.

English | [简体中文](README-zh-CN.md)

| <img src="https://github.com/BetterAndroid/.github/blob/main/img-src/logo.png?raw=true" width = "30" height = "30" alt="LOGO"/> | [BetterAndroid](https://github.com/BetterAndroid) |
|---------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|

This project belongs to the organization above. **Click the link to follow us** and discover more awesome projects.

## What's this

`Hikage` (Pronunciation /ˈhɪkɑːɡeɪ/), this is an Android View runtime powered by Kotlin DSL, designed around **real-time code-based UI construction**.

The project icon was designed by [MaiTungTM](https://github.com/Lagrio),
the name is taken from the original song "Haru**hikage**" in "BanG Dream It's MyGO!!!!!".

<details><summary>Why...</summary>
  <div align="center">
  <img src="img-src/nagasaki_soyo.png" width = "100" height = "100" alt="LOGO"/>

**なんで春日影レイアウト使いの？**
  </div>
</details>

Its second layer of meaning is "Shadow under the Sun",
which symbolizes the design philosophy of this framework:
Hikage is like a shadow, providing a more modern and efficient UI construction system without interfering with the developer's original habits and
project structure.

Hikage is focused on the native Android View ecosystem.
It brings the Kotlin DSL-driven layout experience into the classic View framework,
allowing you to build layouts at high speed with 100% out-of-the-box support for legacy and standard native components.

Hikage does not produce UI components, it is just a transporter of the Android 17-year-old native view ecosystem.

## Why Hikage?

Hikage is mainly intended for developers focused on the native Android platform.
Since Kotlin became the primary development language, there still has not been a truly complete solution for dynamic code-based layouts.
Projects that do not use Jetpack Compose still have to rely on XML, and even with ViewBinding, the experience is still not especially friendly.

Hikage keeps the native Android View naming style, while drawing inspiration
from [Anko](https://github.com/Kotlin/anko), [Splitties](https://github.com/LouisCAD/Splitties), and the Jetpack Compose DSL design philosophy.
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

It is also **fully compatible** with hybrid layouts. You can embed XML (loaded via the `R.layout` scheme), ViewBinding, and even Jetpack Compose
inside Hikage.

> The following example

```kotlin
LinearLayout(
    lparams = LayoutParams(matchParent = true),
    init = {
        orientation = LinearLayout.VERTICAL
    }
) {
    // Embed an XML layout
    Layout(R.layout.my_layout)

    // Embed a ViewBinding layout
    Layout<MyLayoutBinding>()

    // Embed a Jetpack Compose layout
    ComposeView {
        Text("Hello, World!")
    }
}
```

Compared to pure DSL scaffolding lightweight dependency libraries like Anko and Splitties, Hikage's ecosystem naturally supports **dynamically
constructing an `AttributeSet`**.
This solution has passed interactive tests and benchmark performance stress tests on emulators and real devices, ensuring **stable compatibility with
Android 5.0.2 (API 21) ~ 17 (API 37)**.
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

Furthermore, Hikage natively supports Android Studio's layout preview feature,
allowing you to preview layout effects directly in the IDE without running the app.

From now on, forget about XML, ViewBinding, and even `findViewById`, and try building layouts directly in code.

**Don't know Jetpack Compose? No problem. Hikage can be your Kotlin DSL version of XML today, bringing the static XML layout experience into runtime
and letting you build native Android Views with Kotlin.**

Hikage works best when used in conjunction with our other project [BetterAndroid](https://github.com/BetterAndroid/BetterAndroid), and
Hikage itself will automatically reference its [ui-extension](https://betterandroid.github.io/BetterAndroid/en/library/ui-extension) as a core
dependency.

## Get Started

| <img src="img-src/icon.svg" width = "30" height = "30" alt="LOGO"/> | [Hikage Documentation](https://betterandroid.github.io/Hikage/en) |
|---------------------------------------------------------------------|-------------------------------------------------------------------|

You can go to the documentation page for more detailed tutorials and content.

### What's next?

1. **Add dependencies**: Add the **hikage-core** dependency and the dependencies you need to your project.
2. **Sync the project**: After a Gradle sync, you can start using `Hikage`.

In the opened page, select the **Quick Start** section in the sidebar to continue reading.

## More Projects

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>Hey, wait a second! 👋</h2>
    <h3>If this project was helpful, why not stick around and check out more of my work below?</h3>
    <h3>Feel free to leave a follow or a star ⭐️ if they bring you value!</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README.md">→ Click here to discover more of my projects ←</a></h1>
</div>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=BetterAndroid/Hikage&type=Date)

## Third-Party Open Source Usage Statement

- [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass)

## License

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

Copyright © 2019 HighCapable