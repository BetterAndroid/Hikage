---
layout: home
title: Home
hero:
  name: Hikage
  tagline: A real-time Android View runtime powered by Kotlin DSL
  image:
    src: /images/logo.svg
    alt: Hikage
  actions:
    - text: Get Started
      link: /en/guide/home
      theme: brand
    - text: Changelog
      link: /en/about/changelog
      theme: alt
features:
  - icon: 🧩
    title: Native Control
    details: Using View as the foundation and Kotlin as the development language, 100% dynamic code layout, no additional configuration required, supports custom Views.
  - icon: 🔄
    title: Fully Compatible
    details: Supports embedding and mixing XML, ViewBinding, and Jetpack Compose, and provides support for Material components and Jetpack.
  - icon: ⚡
    title: Quick to Start
    details: Simple and easy to use right now! No complex configuration or extensive development experience needed. Just integrate dependencies and enjoy!
---

### Layout, it's that flexible.

::: code-group

```kotlin [Hikage DSL]
LinearLayout(
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
```

```xml [XML]
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center">

    <TextView
        android:id="@+id/text_view"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello, World!"
        android:textSize="16sp"
        android:gravity="center" />
</LinearLayout>
```

:::