# hikage-compiler

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-compiler?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-compiler%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)

This is a Hikage automatic compilation module.

::: tip

Usually, we recommend directly using [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md) to automatically assemble this module.

:::

## Configure Dependency

If you want to take over KSP and compiler dependencies manually, you can add this module to your project using the following method.

We recommend that you first refer to [hikage-bom](./hikage-bom.md) to use BOM for unified version management.

::: warning

You need to integrate the [Google KSP](https://github.com/google/ksp/releases) plugin in your project that is suitable for the current Kotlin version of your project.

:::

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
ksp = "<ksp-version>"
hikage-compiler = "<version>"

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
hikage-compiler = { module = "com.highcapable.hikage:hikage-compiler", version.ref = "hikage-compiler" }
```

Configure dependency in your root project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp) apply false
}
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    alias(libs.plugins.ksp)
}

dependencies {
    // ...
    ksp(libs.hikage.compiler)
}
```

Please change `<version>` to the version displayed at the top of this document,
and change `<ksp-version>` to the KSP version corresponding to the Kotlin version currently used by your project.

### Traditional Method

Configure dependency in your root project `build.gradle.kts`.

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp") version "<ksp-version>" apply false
}
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
plugins {
    // ...
    id("com.google.devtools.ksp")
}

dependencies {
    // ...
    ksp("com.highcapable.hikage:hikage-compiler:<version>")
}
```

Please change `<version>` to the version displayed at the top of this document,
and change `<ksp-version>` to the KSP version corresponding to the Kotlin version currently used by your project.

## Function Introduction

Hikage's compilation module will automatically generate code at compile time.
After update, please re-run the `assembleDebug` or `assembleRelease` task to generate the latest code.

### Generate Layout Components

Hikage can automatically generate corresponding layout component functions (Hikage Performer) for specified layout components at compile time.

#### Custom View

You can add the `HikageView` annotation on your custom `View` to mark it as a Hikage layout component.

| Parameter Name | Description                                                                                                                                                                                                                             |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `lparams`      | LayoutParams `Class` object, if your custom `View` is a subclass of `ViewGroup`, you can declare or leave it blank to use the default value                                                                                             |
| `alias`        | The alias of the layout component, that is, the function name to be generated, gets the name of the current Class by default                                                                                                            |
| `attrs`        | Whether to add the `attrs` parameter to the generated layout component function, default is true                                                                                                                                        |
| `init`         | Whether to add the `init` parameter to the generated layout component function, default is true                                                                                                                                         |
| `performer`    | Whether to add the `performer` parameter to the generated layout component function, default is true, that is, after set to false, whether this layout inherits from or is `ViewGroup`, the `performer` parameter will not be generated |

> The following example

```kotlin
@HikageView(lparams = LinearLayout.LayoutParams::class)
class MyLayout(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    // ...
}
```

Once compiled, you can use `MyLayout` as the layout component in the Hikage layout.

> The following example

```kotlin
Hikagable {
    MyLayout {
        TextView(
            lparams = LayoutParams {
                topMargin = 16.dp
            }
        ) {
            text = "Hello, World!"
        }
    }
}
```

#### Third-party Components

Hikage can also automatically generate layout component functions (Hikage Performer) for the `View` component provided by third parties, and you can use the `HikageViewDeclaration` annotation to complete it.

| Parameter Name | Description                                                                                                                                                                                                                             |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `view`         | Class object of layout component that needs to be declared                                                                                                                                                                              |
| `lparams`      | LayoutParams `Class` object, if your custom `View` is a subclass of `ViewGroup`, you can declare or leave it blank to use the default value                                                                                             |
| `alias`        | The alias of the layout component, that is, the name of the function to be generated, obtains the name of the `view` Class by default                                                                                                   |
| `attrs`        | Whether to add the `attrs` parameter to the generated layout component function, default is true                                                                                                                                        |
| `init`         | Whether to add the `init` parameter to the generated layout component function, default is true                                                                                                                                         |
| `performer`    | Whether to add the `performer` parameter to the generated layout component function, default is true, that is, after set to false, whether this layout inherits from or is `ViewGroup`, the `performer` parameter will not be generated |

> The following example

```kotlin
@HikageViewDeclaration(ThirdPartyView::class)
object ThirdPartyViewDeclaration
```

This annotation can be declared on any `object` class and is only used as a class that needs to be automatically included by the annotation scanner. You can set visibility to `private`, but make sure that the annotated class must be modified with `object`.

Similarly, after compilation, you can use `ThirdPartyView` as the layout component in the Hikage layout.

> The following example

```kotlin
Hikagable {
    ThirdPartyView {
        // ...
    }
}
```

#### View Declaration File

In addition to using the `HikageViewDeclaration` annotation, you can also declare the third-party `View` components that need to generate layout component functions (Hikage Performer) through a view declaration file.

Usually, we recommend collecting declaration files automatically through [hikage-gradle-plugin](../plugin/hikage-gradle-plugin.md).

If you are publishing a declaration module, we recommend using [hikage-declaration-gradle-plugin](../plugin/hikage-declaration-gradle-plugin.md) to complete the automatic packaging of declaration file resources.

If you want to take over KSP arguments manually, you can pass one or more `View` declaration file or directory paths through `hikage.viewDeclarationFiles`, and separate multiple paths using `File.pathSeparator`.
When a directory is passed, the compiler recursively scans all `.json` files under it.

> The following example

```kotlin
ksp {
    arg(
        "hikage.viewDeclarationFiles",
        listOf(
            file("path/to/widgets.json").absolutePath,
            file("path/to/hikage-view-declaration").absolutePath
        ).joinToString(File.pathSeparator)
    )
}
```

`hikage.viewDeclarationFiles` is the strict declaration entry.
If the path points to a missing `.json` file, or a `viewClass` / `lparams` declared in JSON cannot be resolved, compilation fails.

At this point, you can use `hikage.optionalViewDeclarationFiles` to implement the function of an optional declaration entry. The compiler will skip the current item and output an `info` level log when it cannot be resolved.

> The following example

```kotlin
ksp {
    arg(
        "hikage.optionalViewDeclarationFiles",
        file("path/to/dependency-declarations").absolutePath
    )
}
```

The declaration file must be a JSON array, and each item is a `View` declaration that needs to generate a layout component function (Hikage Performer).

> The following example

```json
[
    {
        "viewClass": "com.google.android.material.button.MaterialButton",
        "alias": "MaterialButton",
        "attrs": true,
        "init": true,
        "performer": true
    },
    {
        "viewClass": "android.widget.LinearLayout",
        "lparams": "android.widget.LinearLayout.LayoutParams"
    }
]
```

| Parameter Name | Description                                                                                                                                                                                                                             |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `viewClass`    | The full Class name of the layout component to be declared. This item is required, and an exception will be thrown if the corresponding class cannot be found during the compilation process                                            |
| `lparams`      | LayoutParams `Class` object, if your custom `View` is a subclass of `ViewGroup`, you can declare or leave it blank to use the default value                                                                                             |
| `alias`        | The alias of the layout component, that is, the function name to be generated, gets the name of the current Class by default                                                                                                            |
| `attrs`        | Whether to add the `attrs` parameter to the generated layout component function, default is true                                                                                                                                        |
| `init`         | Whether to add the `init` parameter to the generated layout component function, default is true                                                                                                                                         |
| `performer`    | Whether to add the `performer` parameter to the generated layout component function, default is true, that is, after set to false, whether this layout inherits from or is `ViewGroup`, the `performer` parameter will not be generated |

::: tip

The function package name path for layout component functions (Hikage Performer) generated by Hikage is `com.highcapable.hikage.widget` + the full package name of your `View` or third-party `View` component.

When a `HikageView` or `HikageViewDeclaration` annotation declaration exists for the same `View`, the annotation declaration takes precedence over the view declaration file.

:::

::: danger

To ensure that the generated components have native-level construction performance,
third-party components defined by `HikageView`, `HikageViewDeclaration` annotations and `View` declaration files
must fully support the `(Context, AttributeSet?)` constructor,
and must meet the condition that the first parameter cannot be `null` and the second parameter can be `null`,
otherwise the compiler will throw an exception during generation.

:::