# hikage-core

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-core?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-core%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is the core dependency of Hikage, and you need to introduce this module to use the basic features of Hikage.

## Configure Dependency

You can add this module to your project using the following method.

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-core = "<version>"

[libraries]
hikage-core = { module = "com.highcapable.hikage:hikage-core", version.ref = "hikage-core" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.core)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-core:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-core).

### Basic Usage

Use the code below to create your first Hikage layout.

First, use `Hikageable` to create a `Hikage.Delegate` object.

> The following example

```kotlin
val myLayout = Hikageable {
    LinearLayout {
        TextView {
            text = "Hello, World!"
        }
    }
}
```

Then, set it to the parent or root layout you want to display.

> The following example

```kotlin
// Assume that's your Activity.
val activity: Activity
// Instantiate the Hikage object.
val hikage = myLayout.create(activity)
// Get the root layout.
val root = hikage.root
// Content view set to Activity.
activity.setContentView(root)
```

In this way, we can complete a simple layout creation and configuration.

### Layout Agreement

The basic layout elements of Hikage are based on the Android native `View` component.

All layout elements can be created directly using the Android native `View` component.

The creation process of all layouts will be limited to the specified scope `Hikage.Performer`,
which is called the "player" of the layout, that is, the role object that plays the layout.

This object can be created and maintained in the following ways.

#### Hikageable

As shown in [Basic Usage](#basic-usage), `Hikageable` can directly create a `Hikage.Delegate` or `Hikage` object.

In DSL, you can get the `Hikage.Performer` object to create the layout content.

The first solution is created anywhere.

> The following example

```kotlin
// myLayout is a Hikage.Delegate object.
val myLayout = Hikageable {
    // ...
}
// Assume that's your Context.
val context: Context
// Instantiate the Hikage object where the Context is needed.
val hikage = myLayout.create(context)
```

The second solution is created directly where `Context` exists.

> The following example

```kotlin
// Assume that's your Context.
val context: Context
// Create a layout, myLayout is a Hikage object.
val myLayout = Hikageable(context) {
    // ...
}
```

#### HikageBuilder

In addition to the above methods, you can also maintain a `HikageBuilder` object to pre-create the layout.

First, we need to create a `HikageBuilder` object and define it as a singleton.

> The following example

```kotlin
object MyLayout : HikageBuilder {

    override fun build() = Hikageable {
        // ...
    }
}
```

Then, use it where needed, there are two options as follows.

The first solution is to create a `Hikage.Delegate` object directly using `build`.

> The following example

```kotlin
// myLayout is a Hikage.Delegate object.
val myLayout = MyLayout.build()
// Assume that's your Context.
val context: Context
// Instantiate the Hikage object where the Context is needed.
val hikage = myLayout.create(context)
```

The second solution is to create the `Hikage` delegate object using `Context.lazyHikage`.

For example, we can use it like `ViewBinding` in `Activity`.

> The following example

```kotlin
class MyActivity: AppCompatActivity() {

    private val myLayout by lazyHikage(MyLayout)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the root layout.
        val root = myLayout.root
        // Content view set to Activity.
        setContentView(root)
    }
}
```

### Basic Layout Components

Hikage uses a functional component creation scheme consistent with Jetpack Compose.

Its layout is done using two basic components, the `View` and `ViewGroup` functions.
They correspond to Android native components based on `View` and `ViewGroup`, respectively.

#### View

The basic parameters of the `View` function are the following three, and the `View` object type created using generic definitions.

If the generic type is not declared, the default is to use `android.view.View` as the object type created.

| Parameter Name | Description                                                                   |
| -------------- | ----------------------------------------------------------------------------- |
| `lparams`      | Layout parameter, i.e. `ViewGroup.LayoutParams`, created using `LayoutParams` |
| `id`           | Used to find the ID of the created object, defined using a string             |
| `init`         | The initialization method body of `View`, passed as the last DSL parameter    |

> The following example

```kotlin
View<TextView>(
    lparams = LayoutParams(),
    id = "my_text_view"
) {
    text = "Hello, World!"
    textSize = 16f
    gravity = Gravity.CENTER
}
```

#### ViewGroup

The basic parameters of the `ViewGroup` function are four, and compared with the `View` function, there is one more `performer` parameter.

It must declare a generic type because `ViewGroup` is an abstract class and requires a concrete implementation class.

`ViewGroup` provides an additional generic parameter based on `ViewGroup.LayoutParams` to provide layout parameters for sub-layouts.

`ViewGroup.LayoutParams` is used by default when not declared.

| Parameter Name | Description                                                                   |
| -------------- | ----------------------------------------------------------------------------- |
| `lparams`      | Layout parameter, i.e. `ViewGroup.LayoutParams`, created using `LayoutParams` |
| `id`           | Used to find the ID of the created object, defined using a string             |
| `init`         | The initialization method body of `ViewGroup`, passed in as DSL parameter     |
| `performer`    | `Hikage.Performer` object, passed as the last DSL parameter                   |

The function of the `performer` parameter is to pass a new `Hikage.Performer` object downward as the creator of the sub-layout.

> The following example

```kotlin
ViewGroup<LinearLayout, LinearLayout.LayoutParams>(
    lparams = LayoutParams(),
    id = "my_linear_layout",
    // Initialization method body will be reflected here using `init`.
    init = {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    // You can continue to create sub-layouts here.
    View()
}
```

#### LayoutParams

Layouts in Hikage can be set using the `LayoutParams` function, you can create it using the following parameters.

| Parameter Name      | Description                                                                                   |
| ------------------- | --------------------------------------------------------------------------------------------- |
| `width`             | Manually specify layout width                                                                 |
| `height`            | Manually specify layout height                                                                |
| `matchParent`       | Whether to use `MATCH_PARENT` as layout width and height                                      |
| `wrapContent`       | Whether to use `WRAP_CONTENT` as layout width and height                                      |
| `widthMatchParent`  | Set width to `MATCH_PARENT` only                                                              |
| `heightMatchParent` | Set the height to `MATCH_PARENT` only                                                         |
| `body`              | The initialization method body of the layout parameter, passed into as the last DSL parameter |

When you do not set the `LayoutParams` object or specify `width` and `height`, Hikage will automatically use `WRAP_CONTENT` as layout parameters.

The type of the `body` method body comes from the second generic parameter provided by the upper layer [ViewGroup](#viewgroup).

> The following example

```kotlin
View(
    // Assume that the layout parameter type provided by the upper layer is LinearLayout.LayoutParams.
    lparams = LayoutParams(width = 100.dp) {
        topMargin = 20.dp
    }
)
```

If you only need a horizontally filled layout, you can use `widthMatchParent = true` directly.

> The following example

```kotlin
View(
    lparams = LayoutParams(widthMatchParent = true)
)
```

#### Layout

Hikage supports references to third-party layouts, you can pass in XML layout resource IDs, other Hikage objects, and `View` objects, and even `ViewBinding`.

> The following example

```kotlin
ViewGroup<...> {
    // Quote XML layout resource ID.
    Layout(R.layout.my_layout)
    // Quote ViewBinding.
    Layout<MyLayoutBinding>()
    // Reference another Hikage or Hikage.Delegate object.
    Layout(myLayout)
}
```

### Positioning Layout Components

Hikage supports locating components using `id`. In the example above, we used the `id` parameter to set the component's ID.

After setting the ID, you can use the `Hikage.get` method to get them.

> The following example

```kotlin
val myLayout = Hikageable {
    View<TextView>(id = "my_text_view") {
        text = "Hello, World!"
    }
}
// Assume that's your Context.
val context: Context
// Instantiate the Hikage object where the Context is needed.
val hikage = myLayout.create(context)
// Get the specified component and return the View type.
val textView = hikage["my_text_view"]
// Get the specified component and declare the component type.
val textView = hikage.get<TextView>("my_text_view")
// If you are not sure whether the ID exists, you can use the `getOrNull` method.
val textView = hikage.getOrNull<TextView>("my_text_view")
```

### Custom Layout Components

Hikage provides functions corresponding to component class names for Android basic layout components.

You can directly use these functions to create components without using generics to declare them. If you need components provided by Jetpack or Material,
the [hikage-widget-androidx](./hikage-widget-androidx) or [hikage-widget-material](./hikage-widget-material) modules can be introduced.

> The following example

```kotlin
LinearLayout(
    lparams = LayoutParams(),
    id = "my_linear_layout",
    init = {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
    }
) {
    TextView(
        lparams = LayoutParams(),
        id = "my_text_view"
    ) {
        text = "Hello, World!"
        textSize = 16f
        gravity = Gravity.CENTER
    }
}
```

The initialized `View` or `ViewGroup` objects return instances of their own object type, which you can use in the following layout.

> The following example

```kotlin
val textView = TextView {
    text = "Hello, World!"
    textSize = 16f
    gravity = Gravity.CENTER
}
Button {
    text = "Click Me!"
    setOnClickListener {
        // Use the textView object directly.
        textView.text = "Clicked!"
    }
}
```

If the provided components do not meet your needs, you can create your own components manually.

> The following example

```kotlin
// Suppose you have defined your custom components.
class MyCustomView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    // ...
}

// Below, create the function corresponding to the component.
// Custom components must declare this annotation.
// Declaring the annotation of the component is contagious,
// and this annotation is required in every scope used to build the layout.
@Hikageable
// The naming of functions can be done at will, but it is recommended to use a big camel name.
// The signature part of the function needs to be fixedly
// declared as `inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>`.
inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>.MyCustomView(
    lparams: Hikage.LayoutParams? = null,
    id: String? = null,
    noinline init: HikageView<MyCustomView> = {},
    // If this component is a container, you can declare a `performer` parameter.
    // performer: HikagePerformer<LP> = {}
) = View<MyCustomView>(lparams, id, init)
```

It would seem tedious to implement such complex functions manually every time.
If you want to be able to automatically generate component functions, you can introduce and refer to the [hikage-compiler](./hikage-compiler) module.

### Combination and Disassembly Layout

When building a UI, we usually use reusable layouts as components.

If you don't want each part to be customized separately using a native custom `View`, you can split the layout logic parts directly.

Hikage supports splitting layouts into multiple parts and combining them, you can use the `Hikageable` function anywhere to create a new `Hikage.Delegate` object.

> The following example

```kotlin
// Assume this is your main layout.
val mainLayout = Hikageable {
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
        }
    ) {
        TextView {
            text = "Hello, World!"
        }
        // Combination sublayout.
        Layout(subLayout)
    }
}
// Assume this is your layout submodule.
// Since the upper layout uses LinearLayout,
// you can declare LinearLayout.LayoutParams for the sublayout.
val subLayout = Hikageable<LinearLayout.LayoutParams> {
    TextView(
        lparams = LayoutParams {
            topMargin = 16.dp
        }
    ) {
        text = "Hello, Sub World!"
    }
}
```

### State Management

Hikage has a similar state management workaround to Jetpack Compose, which makes it easy to set up state listening for layout components.

Hikage provides two states, `NonNullState` and `NullableState`, which are divided into two states: holding non-null and nullable.

Unlike the recompose of Jetpack Compose, Hikage will not be recomposed, and the states takes effect through listening and callbacks.

You can use both states in the following scenarios.

> The following example

```kotlin
val myLayout = Hikageable {
    // Declare a non-null variable state.
    val mTextState = mutableStateOf("Hello, World!")
    // Declare a nullable and variable state.
    val mDrawState = mutableStateOfNull<Drawable>()
    // You can delegate the state to a variable.
    var mText by mTextState
    var mDraw by mDrawState
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
        }
    ) {
        TextView {
            textSize = 16f
            gravity = Gravity.CENTER
            // Set (binding) state to text.
            setState(mTextState) {
                text = it
            }
        }
        ImageView {
            // Set (binding) state to Drawable.
            setState(mDrawState) {
                setImageDrawable(it)
            }
        }
        Button {
            text = "Click Me!"
            setOnClickListener {
                // Modify the value of non-null state.
                mText = "Hello, Hikage!"
                // Modify the value of the nullable state.
                mDraw = drawableResource(R.drawable.ic_my_drawable)
            }
        }
    }
}
```

In the example above, we declare a non-null state `mTextState` with `"Hello, World!"` with `mutableStateOf`
Then continue to declare a nullable state `mDrawState` with `null` using `mutableStateOfNull`.

When clicking the button, we modify the value of `mTextState` to `"Hello, Hikage!"` and the value of `mDrawState` is the property resource `R.drawable.ic_my_drawable`.
At this time, the text and images of `TextView` and `ImageView` will be automatically updated.

### Custom Layout Factory

Hikage supports custom layout factories and is compatible with `LayoutInflater.Factory2`.
You can customize events and listening during the Hikage layout inflating process in the following ways.

> The following example

```kotlin
val factory = HikageFactory { parent, base, context, params ->
    // You can customize the behavior of the layout factory here.
    // For example, create a new View object in your own way.
    // `parent` is the ViewGroup object to which the current component is to be added,
    // and if not, it is `null`.
    // `base` is the View object created for the previous HikageFactory, if not, it is `null`.
    // `params` object contains the component ID, AttributeSet and Class objects of View.
    val view = MyLayoutFactory.createView(context, params)
    // You can also initialize and set the created View object here.
    view.setBackgroundColor(Color.RED)
    // Return the created View object.
    // Return `null` will use the default component inflating method.
    view
}
```

You can also pass in the `LayoutInflater` object directly to automatically inflate and use the `LayoutInflater.Factory2` in it.

> The following example

```kotlin
// Assume that this is your LayoutInflater object.
val layoutInflater: LayoutInflater
// Create HikageFactory object through LayoutInflater.
val factory = HikageFactory(layoutInflater)
```

Then set it to the Hikage layout you need to inflate.

> The following example

```kotlin
// Assume that's your Context.
val context: Context
// Create Hikage object.
val hikage = Hikageable(
    context = context,
    factory = {
        // Add a custom HikageFactory object.
        add(factory)
        // Add directly.
        add { parent, base, context, params ->
            // ...
            null
        }
        // Add multiple consecutively.
        addAll(factories)
    }
) {
    LinearLayout {
        TextView {
            text = "Hello, World!"
        }
    }
}
```

::: tip

Hikage will inflate the layout according to the `LayoutInflater.Factory2` of the `Context` object, if you are using `AppCompatActivity`,
Components in the layout will be automatically replaced with the corresponding Compat component or Material component,
which is consistent with the characteristics of the XML layout.

If you do not need this feature to be effective by default, you can turn it off globally using the following method.

> The following example

```kotlin
Hikage.isAutoProcessWithFactory2 = false
```

:::

### Preview Layout

Hikage supports previewing layouts in Android Studio.

With the help of the custom `View` preview plugin that comes with Android Studio, you can preview the layout using the following methods.

You just need to define a custom `View` for the preview layout and inherit from `HikagePreview`.

> The following example

```kotlin
class MyLayoutPreview(context: Context, attrs: AttributeSet?) : HikagePreview(context, attrs) {

    override fun build() = Hikageable {
        LinearLayout {
            TextView {
                text = "Hello, World!"
            }
        }
    }
}
```

Then the preview pane should appear on the right side of your current window.
After opening, click "Build & Refresh". The preview will be automatically displayed after the compilation is completed.

:::tip

`HikagePreview` implements the `HikageBuilder` interface, you can return any Hikage layout in the `build` method for preview.

:::

::: danger

`HikagePreview` supports previewing layouts in Android Studio only, do not use it at runtime or add it to any XML layout.

:::