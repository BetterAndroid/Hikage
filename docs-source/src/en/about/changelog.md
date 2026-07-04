# Changelog

> The version update history of `Hikage` is recorded here.

::: danger

We will only maintain the latest dependencies. If you are using outdated dependencies, you voluntarily renounce any possibility of maintenance.

:::

::: warning

To avoid translation time consumption, Changelog will use **Google Translation** from **Chinese** to **English**, please refer to the original text for actual reference.

Time zone of version release date: **UTC+8**

:::

### 1.1.0 | 2026.07.05 &ensp;<Badge type="tip" text="latest" vertical="middle" />

- **<u>⚠️ Breaking Change</u>**: All names `Hikageable` have been corrected to `Hikagable`
- **<u>⚠️ Breaking Change</u>**: Adjusted `HikageView` and `HikageViewDeclaration` annotation parameters, removed `requireInit`, `requirePerformer` and `final`, and replaced them with `init` and `performer` to control whether the corresponding parameters are generated

#### hikage-core

- Refactored the core layout building structure, split layout params, layout session and the concrete implementation of `Hikage.Performer`
- Added support for dynamic creation of `attrs` XML attribute sets, refer to [hikage-runtime-attribute](../library/hikage-runtime-attribute.md) module
- Migrated `Hikage.LayoutParams` to `LayoutParams`
- Fixed the problem that `HikageFactory` gets an incorrect parent layout object in multi-level layouts
- Added an overloaded method `lazyHikage` to `HikageBuilder`, which supports directly creating `Hikage` objects
- Fixed the problem that the current layout params and sub-layout params types of `ViewGroup` are mixed
- Optimized the reflection call cache of `ViewGroup.generateLayoutParams` in `LayoutParams`
- Changed layout component type passing from `Class` to `KClass`
- Changed `Hikage.PerformerParams` to `HikageFactory.Params` for clearer custom loader parameter semantics
- `HikageFactory.Params` added `factory` parameter, allowing direct passing of constructor function body to avoid unnecessary reflection calls
- Added `Context parameters` support for friendlier combined layout invocation
- Fixed issues related to `LayoutParams` when some attributes were invalid during creation
- Fixed issues related to `HikagePreview` in Android Studio preview
- Moved `ResourcesScope` to the `layout/extension` package
- Added `pluralStringResource`, `pluralTextResource`, `textResource`, `stringArrayResource`, `integerResource`, `integerArrayResource`, `booleanResource`, `dimenPixelSizeResource`, `dimenPixelOffsetResource` and `fractionResource` functions to `ResourcesScope`
- Separated runtime features into the [hikage-runtime](../library/hikage-runtime.md) module

#### hikage-compiler

- Adapted to the `LayoutParams` migration and layout DSL function splitting in [hikage-core](#hikage-core)
- Optimized the dependency declaration of KSP generated files, changed to use independent dependencies of corresponding source files
- Fixed the problem that repeatedly generated files may be silently ignored during multi-round processing
- Optimized the import of layout DSL functions in generated code to avoid ambiguity with Android native types
- Prioritized generating layout component functions (Hikage Performer) based on direct constructor calls to avoid unnecessary reflection calls
- Added a JSON `View` declaration file entry to generate layout component functions (Hikage Performer) from files or directories
- Added performer symbol generation feature

#### hikage-runtime

- First version submitted to Maven

#### hikage-runtime-attribute

- First version submitted to Maven

#### hikage-extension

- Fixed the problem of no parent container `LayoutParams` being passed when creating `Hikage` with `Activity.setContentView`, `Window.setContentView`, `Dialog.setContentView` and `ViewGroup.addView`

#### hikage-widget-foundation

- First version submitted to Maven

#### hikage-widget-androidx

- Only exists as Jetpack AndroidX component declaration files, no longer publishes pre-generated layout component functions (Hikage Performer)

#### hikage-widget-material

- Only exists as Google Material (MDC) component declaration files, no longer publishes pre-generated layout component functions (Hikage Performer)

#### hikage-extension-compose

- Set the default composition disposal strategy for `ComposeView` to avoid keeping compositions after detach or pool release
- Adapted `HikageView` to the release lifecycle of `AndroidView`, allowing layout references to be released when recycled by Compose

## Gradle Plugins

### 1.0.0 | 2026.07.05 &ensp;<Badge type="tip" text="latest" vertical="middle" />

#### hikage-gradle-plugin

- First version submitted to Maven

#### hikage-declaration-gradle-plugin

- First version submitted to Maven

## Historical Versions

### hikage-core

#### 1.0.4 | 2025.12.17 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to `BetterAndroid` new features
- Set the default values of `width` and `height` in `LayoutParam` to `null`

#### 1.0.3 | 2025.12.14 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Lint added recommended warning support for `WebView` and `SurfaceView`
- Removed inline support for layout content DSL for performance optimization and to reduce unreasonable internal API exposure
- Adapted to Kotlin 2.2+

#### 1.0.2 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Migrated Java reflection related behaviors from [YukiReflection](https://github.com/HighCapable/YukiReflection) to [KavaRef](https://github.com/HighCapable/KavaRef)
- Adapted to Android 16 (API 36), fixed the `XmlBlock` crash issue on Android 16
- Optimized layout performance, removed unnecessary inline operations, added caching for reflection operations
- Added `final` parameter to `HikageView` and `HikageViewDeclaration` to support new features in `hikage-compiler`
- Added `SurfaceView` and `WebView` built-in components to `Widgets`
- Adjusted some components in `Widgets` to be `final`

#### 1.0.1 | 2025.05.06 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Fixed the issue where the KSP source code was not successfully released
- Added states management feature

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-compiler

#### 1.0.4 | 2025.12.17 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Aligned version with [hikage-core](#hikage-core)

#### 1.0.3 | 2025.12.14 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Aligned version with [hikage-core](#hikage-core)
- Adapted to the layout content DSL generation method after removing inline in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Added support for the `final` parameter of `HikageView` and `HikageViewDeclaration`, please refer to the relevant usage in the documentation

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-extension

#### 1.0.3 | 2025.12.17 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to `BetterAndroid` new features

#### 1.0.2 | 2025.12.14 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to the layout content DSL usage after removing inline in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Migrated Java reflection related behaviors from [YukiReflection](https://github.com/HighCapable/YukiReflection) to [KavaRef](https://github.com/HighCapable/KavaRef)
- Added generic `ViewGroup.LayoutParams` support for `addView` in `ViewGroup`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-extension-betterandroid

#### 1.0.3 | 2025.12.17 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to `BetterAndroid` new features

#### 1.0.2 | 2025.12.14 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Updated to follow the changes in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to decoupled `ui-component` and `ui-component-adapter` in `BetterAndroid`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-extension-compose

#### 1.0.2 | 2025.12.17 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Updated to follow the changes in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.12.14 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adapted to the layout content DSL usage after removing inline in [hikage-core](#hikage-core)

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-widget-androidx

#### 1.0.2 | 2026.04.04 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Updated to follow the changes in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Added `MotionLayout`, `ImageFilterButton`, `ImageFilterView`, `MockView`, `MotionButton`, `MotionLabel`, `MotionTelltales` components to `ConstraintLayout`
- Adjusted some components to be `final`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven

### hikage-widget-material

#### 1.0.2 | 2026.04.04 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Updated to follow the changes in [hikage-core](#hikage-core)

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- Adjusted some components to be `final`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="stale" vertical="middle" />

- The first version is submitted to Maven