# Changelog

> The version update history of `Hikage` is recorded here.

::: danger

We will only maintain the latest dependencies. If you are using outdated dependencies, you voluntarily renounce any possibility of maintenance.

:::

::: warning

To avoid translation time consumption, Changelog will use **Google Translation** from **Chinese** to **English**, please refer to the original text for actual reference.

Time zone of version release date: **UTC+8**

:::

## hikage-core

### 1.0.2 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Migrated Java reflection related behaviors from [YukiReflection](https://github.com/HighCapable/YukiReflection) to [KavaRef](https://github.com/HighCapable/KavaRef)
- Adapted to Android 16 (API 36), fixed the `XmlBlock` crash issue on Android 16
- Optimized layout performance, removed unnecessary inline operations, added caching for reflection operations
- Added `final` parameter to `HikageView` and `HikageViewDeclaration` to support new features in `hikage-compiler`
- Added `SurfaceView` and `WebView` built-in components to `Widgets`
- Adjusted some components in `Widgets` to be `final`

### 1.0.1 | 2025.05.06 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Fixed the issue where the KSP source code was not successfully released
- Added states management feature

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

## hikage-compiler

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Added support for the `final` parameter of `HikageView` and `HikageViewDeclaration`, please refer to the relevant usage in the documentation

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

## hikage-extension

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Migrated Java reflection related behaviors from [YukiReflection](https://github.com/HighCapable/YukiReflection) to [KavaRef](https://github.com/HighCapable/KavaRef)
- Added generic `ViewGroup.LayoutParams` support for `addView` in `ViewGroup`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

## hikage-extension-betterandroid

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Adapted to decoupled `ui-component` and `ui-component-adapter` in `BetterAndroid`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

## hikage-extension-compose

### 1.0.0 | 2025.04.20 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- The first version is submitted to Maven

## hikage-widget-androidx

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Added `MotionLayout`, `ImageFilterButton`, `ImageFilterView`, `MockView`, `MotionButton`, `MotionLabel`, `MotionTelltales` components to `ConstraintLayout`
- Adjusted some components to be `final`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

## hikage-widget-material

### 1.0.1 | 2025.08.24 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- Adjusted some components to be `final`

### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven