# Looking Toward the Future

> The future is bright and uncertain, let us look forward to the future development space of `Hikage`.

## Future Plans

> Features that `Hikage` may add later are included here.

### IDE Plugin

Hikage will launch an official plugin for Android Studio, which will be an independent project. It plans to support the following features.

#### AST Capabilities

The IDE plugin will implement recognizing UpperCamelCase naming of functions annotated with `@Hikagable` as a standard specification, and no more `FunctionName` and `PropertyName` check warnings.

It supports synthesizing virtual Kotlin stubs in memory, and provides code completion, navigation, refactoring, and other functions for KSP and other component functions (Hikage Performer) in the editor.

#### Layout Conversion Capabilities

The IDE plugin will provide layout conversion capabilities, allowing developers to convert XML layout files into Hikage Kotlin DSL.
You can choose to copy the complete code or create a `HikageBuilder` object.

Also, basic attributes will be translated into the content of the `attrs` block.

#### Layout Component Capabilities

In Hikage, obtaining a component's ID needs to be defined via the `id` parameter at build time, and then obtained via `Hikage.get<T>("view_id")`.

The IDE plugin will provide a capability that allows developers to directly auto-complete and call component IDs generated from strings during coding,
realizing type safety, and cooperate with Kotlin compiler plugins/FIR extensions or code generation capabilities to implement compile-time accessors.

> The following example

```kotlin
val myLayout = Hikagable(context) {
    LinearLayout(
        id = "lin_layout",
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
}

// Directly call the ID generated based on the string.
val linLayout = myLayout.linLayout
val textView = myLayout.textView
// Get the root layout, i.e., LinearLayout, no longer need root<LinearLayout>().
val root = myLayout.root
```

#### Layout Debugging Capabilities

The IDE plugin plans to explore integration with Android Studio's Preview Integration to preview Hikage layouts anywhere,
and will gradually support visual operations of the Component Tree and Palette component libraries in the future.

In the initial stage, the IDE plugin will provide a Tool Window panel to implement basic layout preview capabilities.

At the same time, it will also support the component traceability capability of Layout Inspector,
allowing developers to trace back to the source code location of Hikage components at runtime.

#### Performer Extension Capabilities (TBD)

The IDE plugin will provide extension capabilities for Hikage Performer.
Developers will be able to omit the `Hikage.Performer` receiver and LP generics when creating functions.
Functions annotated with `@Hikagable` will be automatically recognized, achieving similar functionality to Jetpack Compose's `@Composable` annotation.

For example, when declaring a function annotated with `@Hikagable`, the IDE plugin will automatically recognize it.

> The following example

```kotlin
@Hikagable
fun MyTextView(text: String) {
    TextView {
        this.text = text
    }
}
```

You don't need to explicitly define the `performer` parameter in the function parameters,
it's the implicit receiver of the current function. Using `performer` within the function allows you to continue passing child layouts.

> The following example

```kotlin
@Hikagable
fun MyLayout() {
    LinearLayout(
        lparams = LayoutParams(matchParent = true),
        init = {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
    ) {
        performer()
    }
}

val myLayout = Hikagable(context) {
    MyLayout {
        MyTextView("Hello, World!")
    }
}
```