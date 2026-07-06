# 更新日志

> 这里记录了 `Hikage` 的版本更新历史。

::: danger

我们只会对最新的 API 版本进行维护，若你正在使用过时的 API 版本则代表你自愿放弃一切维护的可能性。

:::

### 1.1.1 | 2026.07.06 &ensp;<Badge type="tip" text="最新" vertical="middle" />

#### hikage-core

- 修复 `LayoutSession` 重复设置 `View` ID 的问题，避免布局管线干预用户手动设置的 ID
- 新增 `HikageFactory.Config`，支持配置是否启用 `privateFactory` 以兼容 XML 布局管线的处理方式
- 补充对 `Fragment` 遗漏的 `lazyHikage` 重载方法支持
- 新增对运行时属性的 Lint，检测 `@+id` 并可使用快速修复添加至 `ids.xml`
- 新增对运行时属性合法性 Lint 检测

#### hikage-compiler

- 增强对不合法的语法关键词别名的检测能力

#### hikage-runtime-attribute

- 不再接受 `@+id` 作为运行时属性的合法性，调整为 Fail-Fast 处理，避免运行时异常

#### hikage-widget-androidx

- 补全所有公开 API 的 `View` 组件声明

#### hikage-widget-material

- 补全所有公开 API 的 `View` 组件声明

### 1.1.0 | 2026.07.05 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- **<u>⚠️ 破坏性更新</u>**，修正所有命名 `Hikageable` 到 `Hikagable`
- **<u>⚠️ 破坏性更新</u>**，调整 `HikageView` 与 `HikageViewDeclaration` 注解参数，移除 `requireInit`、`requirePerformer` 与 `final`，改为使用 `init` 与 `performer` 控制是否生成对应参数

#### hikage-core

- 重构核心布局构建结构，拆分布局参数、布局会话以及 `Hikage.Performer` 的具体实现
- 新增 `attrs` XML 属性集合动态创建支持，参考 [hikage-runtime-attribute](../library/hikage-runtime-attribute.md) 模块
- 将 `Hikage.LayoutParams` 迁移为 `LayoutParams`
- 修复 `HikageFactory` 在多层布局中获取父布局对象不正确的问题
- 新增 `HikageBuilder` 的 `lazyHikage` 重载方法，支持直接创建 `Hikage` 对象
- 修复 `ViewGroup` 当前布局参数与子布局参数类型混用的问题
- 优化 `LayoutParams` 对 `ViewGroup.generateLayoutParams` 的反射调用缓存
- 将布局组件类型传递从 `Class` 调整为 `KClass`
- 将 `Hikage.PerformerParams` 调整为 `HikageFactory.Params`，使自定义装载器参数语义更加明确
- `HikageFactory.Params` 新增 `factory` 参数，支持直接传入构造方法函数体以避免不必要的反射调用
- 新增 `Context parameters` 支持，可更友好地调用组合布局
- 修复 `LayoutParams` 在创建时部分属性失效的问题
- 修复 `HikagePreview` 在 Android Studio 预览中的相关问题
- 移动 `ResourcesScope` 至 `layout/extension` 包
- `ResourcesScope` 新增 `pluralStringResource`、`pluralTextResource`、`textResource`、`stringArrayResource`、`integerResource`、`integerArrayResource`、`booleanResource`、`dimenPixelSizeResource`、`dimenPixelOffsetResource` 与 `fractionResource` 功能
- 分离运行时功能至 [hikage-runtime](../library/hikage-runtime.md) 模块

#### hikage-compiler

- 适配 [hikage-core](#hikage-core) 的 `LayoutParams` 迁移以及布局 DSL 函数拆分
- 优化 KSP 生成文件依赖声明，改为使用对应源文件的独立依赖
- 修复多轮处理过程中重复生成文件可能被静默忽略的问题
- 优化生成代码中的布局 DSL 函数导入，避免与 Android 原生类型产生歧义
- 优先生成基于直接调用构造方法的布局组件函数 (Hikage Performer)，避免不必要的反射调用
- 新增 `View` 声明文件 JSON 入口，支持从文件或目录生成布局组件函数 (Hikage Performer)
- 新增 Performer 符号生成功能

#### hikage-runtime

- 首个版本提交至 Maven

#### hikage-runtime-attribute

- 首个版本提交至 Maven

#### hikage-extension

- 修复 `Activity.setContentView`、`Window.setContentView`、`Dialog.setContentView` 与 `ViewGroup.addView` 创建 `Hikage` 时无父容器 `LayoutParams` 传递问题

#### hikage-widget-foundation

- 首个版本提交至 Maven

#### hikage-widget-androidx

- 仅作为 Jetpack AndroidX 组件声明文件存在，不再发布预生成布局组件函数 (Hikage Performer)

#### hikage-widget-material

- 仅作为 Google Material (MDC) 组件声明文件存在，不再发布预生成布局组件函数 (Hikage Performer)

#### hikage-extension-compose

- 为 `ComposeView` 设置默认的组合释放策略，避免脱离窗口或对象池释放后继续持有组合
- 为 `HikageView` 适配 `AndroidView` 的释放生命周期，支持在 Compose 侧回收时释放布局引用

## Gradle 插件

### 1.0.1 | 2026.07.06 &ensp;<Badge type="tip" text="最新" vertical="middle" />

#### hikage-gradle-plugin

- 自动读取 `hikage-bom` 管理的 `hikage-compiler` 版本，避免插件与编译器版本不一致

### 1.0.0 | 2026.07.05 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

#### hikage-gradle-plugin

- 首个版本提交至 Maven

#### hikage-declaration-gradle-plugin

- 首个版本提交至 Maven

## 历史版本

### hikage-core

#### 1.0.4 | 2025.12.17 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配 `BetterAndroid` 新特性
- 将 `LayoutParam` 的 `width` 和 `height` 默认值设置为 `null`

#### 1.0.3 | 2025.12.14 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- Lint 新增 `WebView` 、`SurfaceView` 的推荐警告支持
- 出于性能优化考虑和减少不合理的内部 API 暴露性，移除了对布局内容 DSL 的内联支持
- 适配 Kotlin 2.2+

#### 1.0.2 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将 Java 反射相关行为由 [YukiReflection](https://github.com/HighCapable/YukiReflection) 迁移至 [KavaRef](https://github.com/HighCapable/KavaRef)
- 适配 Android 16 (API 36)，解决了 Android 16 上 `XmlBlock` 的崩溃问题
- 优化布局性能，移除了不必要的内联操作，对反射操作增加缓存
- `HikageView` 和 `HikageViewDeclaration` 新增 `final` 参数以配合 `hikage-compiler` 实现新功能
- `Widgets` 新增 `SurfaceView` 和 `WebView` 内置组件
- `Widgets` 调整部分组件为 `final`

#### 1.0.1 | 2025.05.06 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 修复 KSP 源码没有成功发布的问题
- 新增状态管理功能

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-compiler

#### 1.0.4 | 2025.12.17 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将版本对齐 [hikage-core](#hikage-core)

#### 1.0.3 | 2025.12.14 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将版本对齐 [hikage-core](#hikage-core)
- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 生成方式

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 新增对 `HikageView` 和 `HikageViewDeclaration` 的 `final` 参数的支持，详情请参考文档的相关用法

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-extension

#### 1.0.3 | 2025.12.17 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配 `BetterAndroid` 新特性

#### 1.0.2 | 2025.12.14 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 使用方式

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 将 Java 反射相关行为由 [YukiReflection](https://github.com/HighCapable/YukiReflection) 迁移至 [KavaRef](https://github.com/HighCapable/KavaRef)
- `ViewGroup` 新增对 `addView` 的泛型 `ViewGroup.LayoutParams` 支持

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-extension-betterandroid

#### 1.0.3 | 2025.12.17 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配 `BetterAndroid` 新特性

#### 1.0.2 | 2025.12.14 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 跟随 [hikage-core](#hikage-core) 变更进行一次更新

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配了 `BetterAndroid` 解耦合后的 `ui-component` 和 `ui-component-adapter`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-extension-compose

#### 1.0.2 | 2025.12.17 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 跟随 [hikage-core](#hikage-core) 变更进行一次更新

#### 1.0.1 | 2025.12.14 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 适配了 [hikage-core](#hikage-core) 移除内联后的布局内容 DSL 使用方式

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-widget-androidx

#### 1.0.2 | 2026.04.04 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 跟随 [hikage-core](#hikage-core) 变更进行一次更新

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- `ConstraintLayout` 新增 `MotionLayout`、`ImageFilterButton`、`ImageFilterView`、`MockView`、`MotionButton`、`MotionLabel`、`MotionTelltales` 组件
- 调整部分组件为 `final`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven

### hikage-widget-material

#### 1.0.2 | 2026.04.04 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 跟随 [hikage-core](#hikage-core) 变更进行一次更新

#### 1.0.1 | 2025.08.24 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 调整部分组件为 `final`

#### 1.0.0 | 2025.04.20 &ensp;<Badge type="warning" text="过旧" vertical="middle" />

- 首个版本提交至 Maven