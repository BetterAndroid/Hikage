# hikage-runtime

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-runtime?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-runtime%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is the runtime dependency for Hikage, which you can implement to use state management features.

## Configure Dependency

You can add this module to your project using the following method.

We recommend that you first refer to [hikage-bom](./hikage-bom.md) to use BOM for unified version management.

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-runtime = "<version>"

[libraries]
hikage-runtime = { module = "com.highcapable.hikage:hikage-runtime", version.ref = "hikage-runtime" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.runtime)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-runtime:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-runtime).

### State Management

Hikage provides a lightweight state management solution for `View`-based layouts.

Unlike Jetpack Compose recomposition, Hikage does not rebuild the layout tree when state changes.
State changes take effect through observer callbacks and mutate existing `View` instances.

Hikage provides two state types:

- `NonNullState` holds non-null values.
- `NullableState` holds nullable values.

It is recommended to use `View.setState(...)` inside layout components.
It subscribes the observer when the `View` is attached to window, automatically cancels it when detached,
and subscribes again with the latest state when reattached.

If you need to observe state changes directly, `observe(...)` returns a `StateSubscription`.
You can call `cancel()` to cancel it manually.

> The following example

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

In the example above, `mutableStateOf` creates a non-null state and `mutableStateOfNull` creates a nullable state.

When clicking the button, the existing `TextView` and `ImageView` are updated through state callbacks.

::: warning

When using `state.observe(...)` directly, or using `setState(...)` on a non-`View` object, the observer is a long lifecycle subscription
and will not be automatically released with `View` detach.

If the observer target may be destroyed before the state object, keep the returned `StateSubscription` and call `cancel()` when it is no longer needed.

:::

### Lifecycle State

For ViewModel scenarios, the recommended model is:

```text :no-line-numbers
ViewModel owns state
Hikage builds View tree
hikage-runtime binds Flow/LiveData to View mutation
```

The `com.highcapable.hikage.runtime.lifecycle` provides lifecycle-aware adapters for `StateFlow`, `Flow`, `LiveData` and effect flows.

The `View` receiver variants automatically find `LifecycleOwner` from the view tree.
If there is no available owner, pass `lifecycleOwner` explicitly.

All lifecycle-aware APIs collect or observe when the lifecycle reaches `Lifecycle.State.STARTED` by default.
You can customize this with `minActiveState`.

#### StateFlow

`StateFlow` is the most common state holder in modern ViewModel usage.
It has a current value, so `setState(...)` applies `state.value` immediately and keeps collecting updates with lifecycle.

> The following example

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

Used in Hikage layout.

> The following example

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

For regular `Flow`, you can use `setState(flow, initialValue)` when you need an initial value before the first emission.

> The following example

```kotlin
TextView {
    setState(
        flow = viewModel.runtimeTicker,
        initialValue = 0
    ) {
        text = "Flow ticker: $it s"
    }
}
```

If you do not need an initial value, use `collectState(...)`.

> The following example

```kotlin
TextView {
    collectState(viewModel.runtimeTicker) {
        text = "collectState mirrored $it"
    }
}
```

#### LiveData

`LiveData` is also supported for existing Android projects.
If the `LiveData` already has an initialized value, Hikage applies it before observing.

> The following example

```kotlin
TextView {
    setState(viewModel.compatibilityStatus) {
        text = it
    }
}
```

#### Effect

For one-shot UI events such as toast, navigation and dialog display, use `collectEffect(...)`.

Unlike state collection, effects are collected one by one.

> The following example

```kotlin
collectEffect(viewModel.effects, lifecycleOwner = this) { effect ->
    when (effect) {
        is LoginEffect.Toast -> toast(effect.message)
        LoginEffect.NavigateBack -> finish()
    }
}
```

::: tip

`collectEffect(...)` accepts any `Flow`, including `SharedFlow` and `Channel.receiveAsFlow()`.

:::

::: warning

When the receiver is not a `View`, or when the `View` is not attached to a view tree with a `LifecycleOwner`, pass `lifecycleOwner` explicitly.

> The following example

```kotlin
setState(
    state = viewModel.uiState,
    lifecycleOwner = this
) {
    // Update non-View object state here.
}
```

:::

All lifecycle-aware APIs return `StateSubscription`, so you can cancel them manually when necessary.