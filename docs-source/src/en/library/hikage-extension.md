# hikage-extension

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-extension?logo=apachemaven&logoColor=orange)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-extension%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android)

This is a Hikage extension dependency for UI component-related features.

## Configure Dependency

You can add this module to your project using the following method.

### SweetDependency (Recommended)

Add dependency in your project's `SweetDependency` configuration file.

```yaml
libraries:
  com.highcapable.hikage:
    hikage-extension:
      version: +
```

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation(com.highcapable.hikage.hikage.extension)
```

### Version Catalog

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-extension = "<version>"

[libraries]
hikage-extension = { module = "com.highcapable.hikage:hikage-extension", version.ref = "hikage-extension" }
```

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation(libs.hikage.extension)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-extension:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-extension).

### Activity

Hikage provides better extensions for `Activity`, and creating a Hikage in `Activity` will be easier.

> The following example

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView {
            LinearLayout(
                lparams = LayoutParams(matchParent = true) {
                    topMargin = 16.dp
                },
                init = {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }
            ) {
                TextView {
                    text = "Hello, World!"
                    textSize = 16f
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}
```

With the `setContentView` extension method of `Hikage`, you can set the layout using the `setContent` method like Jetpack Compose.

### Window

Using Hikage to create a layout in Window is consistent with [Activity](#activity), you just need to use the `setContentView` method to pass in a `Hikage` layout.

### Dialog

If you want to create a layout using Hikage directly in `AlertDialog`, you can now do it more simply using the following scheme.

> The following example

```kotlin
// Assume this is your Context.
val context: Context
// Create a dialog box and display it.
AlertDialog.Builder(context)
    .setTitle("Hello, World!")
    .setView {
        TextView {
            text = "Hello, World!"
            textSize = 16f
        }
    }
    .show()
```

To create a layout using Hikage in `AlertDialog`, you just need to use the `setView` method to pass in a `Hikage` layout.

If you inherited from `Dialog` for customization, you can use the `setContentView` method as in [Activity](#activity).

> The following example

```kotlin
class CustomDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView {
            LinearLayout(
                lparams = LayoutParams(matchParent = true) {
                    topMargin = 16.dp
                },
                init = {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }
            ) {
                TextView {
                    text = "Hello, World!"
                    textSize = 16f
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}
```

### PopupWindow

You can inherit from `PopupWindow` for customization and then use Hikage to create the layout,
and you can use the `setContentView` method like in [Activity](#activity).

> The following example

```kotlin
class CustomPopupWindow(context: Context) : PopupWindow(context) {

    init {
        setContentView(context) {
            LinearLayout(
                lparams = LayoutParams(matchParent = true) {
                    topMargin = 16.dp
                },
                init = {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }
            ) {
                TextView {
                    text = "Hello, World!"
                    textSize = 16f
                    gravity = Gravity.CENTER
                }
            }
        }
    }
}
```

::: danger

To create a `PopupWindow` for Hikage layout, you need to use the `Context` constructor method to initialize it.
If the `Context` cannot be obtained immediately, please pass the `Context` instance to the `setContentView` method.

:::

### ViewGroup

Hikage extends the `addView` method of `ViewGroup`, and you can use the Hikage layout directly to quickly add new layouts to the current `ViewGroup`.

> The following example

```kotlin
// Assume this is your ViewGroup.
val root: FrameLayout
// Add Hikage layout.
root.addView {
    TextView {
        text = "Hello, World!"
        textSize = 16f
    }
}
```

Or, use in a custom `View`.

> The following example

```kotlin
class CustomView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    init {
        addView {
            TextView {
                text = "Hello, World!"
                textSize = 16f
            }
        }
    }
}
```