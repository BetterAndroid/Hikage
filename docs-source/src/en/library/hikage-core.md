# hikage-core

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-core?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-core%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is the core dependency of Hikage, and you need to introduce this module to use the basic features of Hikage.

## Configure Dependency

You can add this module to your project using the following method.

We recommend that you first refer to [hikage-bom](./hikage-bom.md) to use BOM for unified version management.

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

First, use `Hikagable` to create a `Hikage.Delegate` object.

> The following example

```kotlin
val myLayout = Hikagable {
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

#### Hikagable

As shown in [Basic Usage](#basic-usage), `Hikagable` can directly create a `Hikage.Delegate` or `Hikage` object.

In DSL, you can get the `Hikage.Performer` object to create the layout content.

The first solution is created anywhere.

> The following example

```kotlin
// myLayout is a Hikage.Delegate object.
val myLayout = Hikagable {
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
val myLayout = Hikagable(context) {
    // ...
}
```

#### HikageBuilder

In addition to the above methods, you can also maintain a `HikageBuilder` object to pre-create the layout.

First, we need to create a `HikageBuilder` object and define it as a singleton.

> The following example

```kotlin
object MyLayout : HikageBuilder {

    override fun build() = Hikagable {
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

Or, we can directly create a `Hikage` object.

> The following example

```kotlin
class MyActivity : AppCompatActivity() {

    private val myLayout by lazyHikage {
        // ...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the root layout.
        val root = myLayout.root
        // Set as the content view of the Activity.
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

| Parameter Name | Description                                                                     |
| -------------- | ------------------------------------------------------------------------------- |
| `lparams`      | Layout parameter, i.e. `ViewGroup.LayoutParams`, created using `LayoutParams`   |
| `id`           | Used to find the ID of the created object, defined using a string               |
| `attrs`        | The XML attribute set for creating the `View`, created using `Hikage.Attribute` |
| `init`         | The initialization method body of `View`, passed as the last DSL parameter      |

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

| Parameter Name | Description                                                                          |
| -------------- | ------------------------------------------------------------------------------------ |
| `lparams`      | Layout parameter, i.e. `ViewGroup.LayoutParams`, created using `LayoutParams`        |
| `id`           | Used to find the ID of the created object, defined using a string                    |
| `attrs`        | The XML attribute set for creating the `ViewGroup`, created using `Hikage.Attribute` |
| `init`         | The initialization method body of `ViewGroup`, passed in as DSL parameter            |
| `performer`    | `Hikage.Performer` object, passed as the last DSL parameter                          |

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
val myLayout = Hikagable {
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

Hikage can generate corresponding layout component functions (Hikage Performer) for component class names,
and you can directly use them to create components without having to declare them with generics.

If you need components provided by Jetpack or Material,
you can import the [hikage-widget-androidx](./hikage-widget-androidx.md) or [hikage-widget-material](./hikage-widget-material.md) modules.

The declaration of Android basic components depends on the [hikage-widget-foundation](./hikage-widget-foundation.md) module,
which has been automatically imported into the current module, so you don't need to import it separately.

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

You can continue to refer to [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md),
or manually introduce the [hikage-compiler](./hikage-compiler.md) module to automatically generate your own layout component functions.

We no longer recommend manually creating component functions, as their implementation costs are too high and unexpected problems may occur.
If you still decide to create it yourself, you can refer to the following scheme to enter the process of completely manually creating component functions.

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
@Hikagable
// The naming of functions can be done at will, but it is recommended to use a big camel name.
// The signature part of the function needs to be fixedly
// declared as `inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>`.
inline fun <reified LP : ViewGroup.LayoutParams> Hikage.Performer<LP>.MyCustomView(
    lparams: LayoutParams? = null,
    id: String? = null,
    noinline attrs: HikageAttribute = {},
    noinline init: HikageView<MyCustomView> = {},
    // If this component is a container, you can declare a `performer` parameter.
    // performer: HikagePerformer<LP> = {}
) = View<MyCustomView>({ context, attrs -> MyCustomView(context, attrs) }, lparams, id, attrs, init)
```

### Combination and Disassembly Layout

When building a UI, we usually use reusable layouts as components.

If you don't want each part to be customized separately using a native custom `View`, you can split the layout logic parts directly.

Hikage supports splitting layouts into multiple parts and combining them, you can use the `Hikagable` function anywhere to create a new `Hikage.Delegate` object.

> The following example

```kotlin
// Assume this is your main layout.
val mainLayout = Hikagable {
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
val subLayout = Hikagable<LinearLayout.LayoutParams> {
    TextView(
        lparams = LayoutParams {
            topMargin = 16.dp
        }
    ) {
        text = "Hello, Sub World!"
    }
}
```

You can also use Kotlin's **Context parameters** feature to make combined layouts feel more natural.

> The following example

```kotlin
Hikagable {
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
        }
    ) {
        TextView {
            text = "Hello, World!"
        }
        // Combined sublayout.
        SubTextView()
    }
}

val SubTextView = Hikagable {
    TextView {
        textSize = 14f
        text = "Hello, Sub World!"
    }
}
```

### XML Attribute Sets

::: danger

This feature is a runtime extension of Hikage, and it is not integrated by default as a main feature.
You need to manually introduce the [hikage-runtime-attribute](../library/hikage-runtime-attribute.md) module before using it,
otherwise the following features will not take effect and will explicitly throw an exception at runtime.

:::

Hikage supports passing in the XML attribute set via the parameter `attrs` when creating a component. These attribute values will be parsed dynamically at runtime and set to the component, and it will only take effect once when the component is created.

Hikage supports most of the attributes defined by XML, which is very friendly to some old custom components that cannot modify attributes dynamically. You can directly use XML attributes to set their values without having to consider using reflection or exposing extra setter methods in your component.

For example, in the following example, we can use the `android:text` attribute through `HikageAttribute` to set the text content of `TextView`.

> The following example

```kotlin
TextView(
    attrs = {
        // Declare the "android" namespace.
        android {
            // The following is equivalent to android:text="Hello, World!".
            set("text", "Hello, World!")
        }
    }
) {
    // Dynamically set content will overwrite XML attribute values.
    text = "Hello, World!"
}
```

`HikageAttribute` is the core DSL template for Hikage to build an XML attribute set. Each namespace provides an `AttributeScope`, in which you can use the `set` method to set the attribute value. The first parameter of the `set` method is the attribute name, and the second parameter is the attribute value.

You can directly use the namespace approach to set property values as in the example above.

> The following example

```kotlin
val myAttrs = HikageAttribute {
    // Declare your own namespace.
    namespace("myScope") {
        // The following is equivalent to myScope:myKey="My Value".
        set("myKey", "My Value")
    }
    // Use the namespace of the current project.
    app {
        // The following is equivalent to app:myKey="My Value".
        set("myKey", "My Value")
    }
}

// Then set to the component.
MyView(attrs = myAttrs)
```

You can also choose not to use the namespace DSL to set each attribute value independently.

> The following example

```kotlin
val myAttrs = HikageAttribute {
    // The following is equivalent to myScope:myKey="My Value".
    set("myScope:myKey", "My Value")
    // Or
    namespace("myScope").set("myKey", "My Value")
}
```

The second parameter of the `set` method provides three types: `String`, `Int`, and `Boolean`, which correspond to the three basic types of XML attribute values. Hikage will parse and set them dynamically based on the attribute's type.

> The following example

```kotlin
val myAttrs = HikageAttribute {
    android {
        // Set the property value of the String type.
        set("text", "Hello, World!")
        // Use an integer scheme to set the color property.
        set("textColor", Color.RED)
        // Use a hexadecimal string scheme to set the color property.
        set("textColor", "#FFFF0000")
        // Use a boolean value to set the property value.
        set("enabled", true)
        // Declare a resource ID as a property value.
        set("background", "@drawable/my_background")
        // Use the resource ID directly as a property value.
        set("background", R.drawable.my_background)
        // Use an integer scheme to set padding.
        set("padding", 16.dp)
        // Use a string scheme to set padding.
        set("padding", "16dp")
    }
}

// Then set to the component.
TextView(attrs = myAttrs)
```
::: warning

When you set an attribute value, Hikage will dynamically parse and set it according to the type of the attribute.
If the type of the attribute value you provide does not match the actual type of the attribute, it may cause an exception or fail silently at runtime.

Hikage may have some limitations in parsing dynamic type casting,
please make sure that the type of the attribute value you provide matches the actual type of the attribute and try to use strings to set attribute values preferentially to avoid potential issues.

The attribute value does not support dynamic modification after it is set. This is a design limitation of Android XML attributes,
not a design defect of Hikage.

Android XML attributes do not allow duplicate names, so you cannot set the same attribute multiple times in `HikageAttribute`,
even if their namespaces are different.

:::

::: danger

For attributes starting with `layout_`, they belong to the XML attributes when creating `LayoutParams`.
If you manually create `LayoutParams` using the `lparams` parameter, Hikage will ignore passing the `AttributeSet` to
the parent layout and create new `LayoutParams`, these attributes will no longer take effect and will be overridden,
you can only choose one scheme to set layout parameters.

:::

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
    // `params` object contains the component ID, AttributeSet, the Class object of the View,
    // and the direct creation function body of the constructor.
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
val hikage = Hikagable(
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

    override fun build() = Hikagable {
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

If you do not see the preview pane on the right side, you can create a new XML layout file and add the following template code.

> The following example

```xml
<?xml version="1.0" encoding="utf-8"?>
<yourpackage.MyLayoutPreview xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

At this point, you should be able to see the preview in the preview pane on the right side.
If you modify the layout code, click "Build & Refresh" and the preview will automatically update.

:::tip

`HikagePreview` implements the `HikageBuilder` interface, you can return any Hikage layout in the `build` method for preview.

:::

::: danger

`HikagePreview` supports previewing layouts in Android Studio only, do not use it at runtime or add it to any XML layout.

:::