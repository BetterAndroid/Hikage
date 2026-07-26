---
layout: home
title: 首页
hero:
  name: Hikage
  tagline: 一个由 Kotlin DSL 驱动的 Android View 实时运行时框架
  image:
    src: /images/logo.svg
    alt: Hikage
  actions:
    - text: 立即开始
      link: /zh-cn/guide/home
      theme: brand
    - text: 更新日志
      link: /zh-cn/about/changelog
      theme: alt
features:
  - icon: 🧩
    title: 原生可控
    details: 使用 View 作为基础，Kotlin 作为开发语言，100% 动态代码布局，无需任何额外配置，支持自定义 View。
  - icon: 🔄
    title: 全面兼容
    details: 支持 XML、ViewBinding 以及 Jetpack Compose 嵌入混合使用，并对 Material 组件及 Jetpack 提供支持。
  - icon: ⚡
    title: 快速上手
    details: 简单易用，不需要繁琐的配置，不需要十足的开发经验，搭建环境集成依赖即可立即开始使用。
---

### 布局，就是这么灵活。

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