# hikage-runtime

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-runtime?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-runtime%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

这是 Hikage 的运行时依赖，你可以引入它来使用状态管理相关功能。

## 配置依赖

你可以使用如下方式将此模块添加到你的项目中。

我们推荐你优先参考 [hikage-bom](./hikage-bom.md) 使用 BOM 统一管理版本。

### Version Catalog (推荐)

在你的项目 `gradle/libs.versions.toml` 中添加依赖。

```toml
[versions]
hikage-runtime = "<version>"

[libraries]
hikage-runtime = { module = "com.highcapable.hikage:hikage-runtime", version.ref = "hikage-runtime" }
```

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation(libs.hikage.runtime)
```

请将 `<version>` 修改为此文档顶部显示的版本。

### 传统方式

在你的项目 `build.gradle.kts` 中配置依赖。

```kotlin
implementation("com.highcapable.hikage:hikage-runtime:<version>")
```

请将 `<version>` 修改为此文档顶部显示的版本。

## 功能介绍

你可以 [点击这里](kdoc://hikage-runtime) 查看 KDoc。

### 状态管理

Hikage 为基于 `View` 的布局提供了一套轻量状态管理方案。

不同于 Jetpack Compose 的重组 (Recompose)，Hikage 不会在状态变化时重新构建布局树。
状态变化会通过监听回调生效，并直接修改已经存在的 `View` 实例。

Hikage 提供两种状态类型：

- `NonNullState` 持有非空值
- `NullableState` 持有可空值

在布局组件中推荐直接使用 `View.setState(...)` 绑定状态。
它会在 `View` `attach` 到窗口时注册监听，在 `detach` 时自动取消监听，再次 `attach` 时会重新注册并同步最新状态。

如果你需要直接监听状态变化，`observe(...)` 会返回一个 `StateSubscription`。
你可以通过调用 `cancel()` 主动取消监听。

> 示例如下

```kotlin
val textState = mutableStateOf("Hello, World!")
val drawableState = mutableStateOfNull<Drawable>()

var text by textState
var drawable by drawableState

LinearLayout(
    lparams = LayoutParams(matchParent = true),
    init = {
        orientation = LinearLayout.VERTICAL
    }
) {
    TextView {
        setState(textState) {
            this.text = it
        }
    }
    ImageView {
        setState(drawableState) {
            setImageDrawable(it)
        }
    }
    Button {
        this.text = "Click Me!"
        setOnClickListener {
            text = "Hello, Hikage!"
            drawable = drawableResource(R.drawable.ic_my_drawable)
        }
    }
}
```

在上面的示例中，`mutableStateOf` 会创建非空状态，`mutableStateOfNull` 会创建可空状态。

点击按钮后，已经存在的 `TextView` 和 `ImageView` 会通过状态回调被更新。

::: warning

直接使用 `state.observe(...)`，或在非 `View` 对象上使用 `setState(...)` 时，监听会作为长生命周期订阅存在，不会自动跟随 `View` 的 `detach` 释放。

如果监听目标可能早于状态对象销毁，请保存返回的 `StateSubscription` 并在不再需要时调用 `cancel()`。

:::

### 生命周期状态

在 `ViewModel` 场景中，我们推荐的模型是：

```text :no-line-numbers
ViewModel 持有状态
Hikage 构建 View 树
hikage-runtime 将 Flow/LiveData 绑定到 View 修改
```

`com.highcapable.hikage.runtime.lifecycle` 提供了针对 `StateFlow`、`Flow`、`LiveData` 和 effect flow 的生命周期感知适配。

以 `View` 为接收者的 API 会自动从 `View` 树中查找 `LifecycleOwner`。如果当前没有可用的所有者对象，请显式传入 `lifecycleOwner`。

所有生命周期感知 API 默认会在生命周期达到 `Lifecycle.State.STARTED` 时进行收集或监听。你可以通过 `minActiveState` 自定义这个行为。

#### StateFlow

`StateFlow` 是现代 `ViewModel` 中最常用的状态持有者。它拥有当前值，因此 `setState(...)` 会立即应用 `state.value`，并在生命周期内继续收集更新。

> 示例如下

```kotlin
data class LoginUiState(
    val username: String = "",
    val password: String = ""
) {
    val canSubmit get() = username.isNotBlank() && password.isNotBlank()
}

class LoginViewModel : ViewModel() {

    private val mutableUiState = MutableStateFlow(LoginUiState())

    val uiState = mutableUiState.asStateFlow()

    fun updateUsername(value: String) {
        mutableUiState.update { it.copy(username = value) }
    }
}
```

在 Hikage 布局中使用。

> 示例如下

```kotlin
TextInputEditText {
    doOnTextChanged { text, _, _, _ ->
        viewModel.updateUsername(text.toString())
    }
}

Button {
    setState(viewModel.uiState) {
        isEnabled = it.canSubmit
    }
}
```

#### Flow

对于普通 `Flow`，如果你需要在第一次发送前提供初始值，可以使用 `setState(flow, initialValue)`。

> 示例如下

```kotlin
TextView {
    setState(
        flow = viewModel.runtimeTicker,
        initialValue = 0
    ) {
        text = "Flow 计时：$it 秒"
    }
}
```

如果你不需要初始值，可以使用 `collectState(...)`。

> 示例如下

```kotlin
TextView {
    collectState(viewModel.runtimeTicker) {
        text = "collectState 镜像 $it"
    }
}
```

#### LiveData

Hikage 同样支持 `LiveData`，如果 `LiveData` 已经拥有初始化值，Hikage 会先应用当前值，再开始监听。

```kotlin
TextView {
    setState(viewModel.compatibilityStatus) {
        text = it
    }
}
```

#### Effect

对于 Toast、导航、弹窗显示这类一次性 UI 事件，请使用 `collectEffect(...)`。

与状态收集不同，effect 会逐个收集并处理。

> 示例如下

```kotlin
collectEffect(viewModel.effects, lifecycleOwner = this) { effect ->
    when (effect) {
        is LoginEffect.Toast -> toast(effect.message)
        LoginEffect.NavigateBack -> finish()
    }
}
```

::: tip

`collectEffect(...)` 接受任意 `Flow`，包括 `SharedFlow` 和 `Channel.receiveAsFlow()`。

:::

::: warning

当接收者不是 `View`，或当前 `View` 所在的 `View` 树中没有 `LifecycleOwner` 时，请显式传入 `lifecycleOwner`。

> 示例如下

```kotlin
setState(
    state = viewModel.uiState,
    lifecycleOwner = this
) {
    // 在这里更新非 View 对象的状态
}
```

:::

所有生命周期感知 API 都会返回 `StateSubscription`，你可以在需要时手动取消订阅。