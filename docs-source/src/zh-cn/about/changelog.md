# 更新日志

> 这里记录了 `Hikage` 的版本更新历史。

::: danger

我们只会对最新的 API 版本进行维护，若你正在使用过时的 API 版本则代表你自愿放弃一切维护的可能性。

:::

::: tip

为了确保自动生成代码的一致性，[hikage-core](#hikage-core) 与 [hikage-compiler](#hikage-compiler) 模块的版本将保持一致。

:::

## hikage-core

### 1.0.3 | 2025.12.14 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- Lint 新增 `WebView` 、`SurfaceView` 的推荐警告支持
- 出于性能优化考虑和减少不合理的内部 API 暴露性，移除了对布局内容 DSL 的内联支持
- 适配 Kotlin 2.2+

### 1.0.2 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将 Java 反射相关行为由 [YukiReflection](https://github.com/HighCapable/YukiReflection) 迁移至 [KavaRef](https://github.com/HighCapable/KavaRef)
- 适配 Android 16 (API 36)，解决了 Android 16 上 `XmlBlock` 的崩溃问题
- 优化布局性能，移除了不必要的内联操作，对反射操作增加缓存
- `HikageView` 和 `HikageViewDeclaration` 新增 `final` 参数以配合 `hikage-compiler` 实现新功能
- `Widgets` 新增 `SurfaceView` 和 `WebView` 内置组件
- `Widgets` 调整部分组件为 `final`

### 1.0.1 | 2025.05.06 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 修复 KSP 源码没有成功发布的问题
- 新增状态管理功能

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-compiler

### 1.0.3 | 2025.12.14 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- 将版本对齐 [hikage-core](#hikage-core)
- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 生成方式

### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 新增对 `HikageView` 和 `HikageViewDeclaration` 的 `final` 参数的支持，详情请参考文档的相关用法

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-extension

### 1.0.2 | 2025.12.14 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 使用方式

### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将 Java 反射相关行为由 [YukiReflection](https://github.com/HighCapable/YukiReflection) 迁移至 [KavaRef](https://github.com/HighCapable/KavaRef)
- `ViewGroup` 新增对 `addView` 的泛型 `ViewGroup.LayoutParams` 支持

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-extension-betterandroid

### 1.0.2 | 2025.12.14 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- 跟随 [hikage-core](#hikage-core) 变更进行一次更新

### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配了 `BetterAndroid` 解耦合后的 `ui-component` 和 `ui-component-adapter`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-extension-compose

### 1.0.1 | 2025.12.14 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 使用方式

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-widget-androidx

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- `ConstraintLayout` 新增 `MotionLayout`、`ImageFilterButton`、`ImageFilterView`、`MockView`、`MotionButton`、`MotionLabel`、`MotionTelltales` 组件
- 调整部分组件为 `final`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

## hikage-widget-material

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="最新" vertical="middle" />

- 调整部分组件为 `final`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven