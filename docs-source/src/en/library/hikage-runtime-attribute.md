# hikage-runtime-attribute

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.hikage/hikage-runtime-attribute?logo=apachemaven&logoColor=orange&style=flat-square)
<span style="margin-left: 5px"/>
![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fraw.githubusercontent.com%2FHighCapable%2Fmaven-repository%2Frefs%2Fheads%2Fmain%2Frepository%2Freleases%2Fcom%2Fhighcapable%2Fhikage%2Fhikage-runtime-attribute%2Fmaven-metadata.xml&logo=apachemaven&logoColor=orange&label=highcapable-maven-releases&style=flat-square)
<span style="margin-left: 5px"/>
![Android Min SDK](https://img.shields.io/badge/Min%20SDK-21-orange?logo=android&style=flat-square)

This is the XML attribute sets runtime module of Hikage, which provides an in-memory AAPT2 resource parsing simulator.

::: warning Play Compliance Risk Warning

Since this module involves reflection operations on `XmlBlock`, there may be risks related to Google Play compliance.
If your app needs to be listed on Google Play,
please carefully evaluate the potential risks of using this module and ensure that your app complies with Google Play's policy requirements.

Hikage is not responsible for any compliance issues that may arise from using this module.

:::

## Configure Dependency

You can add this module to your project using the following method.

We recommend that you first refer to [hikage-bom](./hikage-bom.md) to use BOM for unified version management.

### Version Catalog (Recommended)

Add dependency in your project's `gradle/libs.versions.toml`.

```toml
[versions]
hikage-runtime-attribute = "<version>"

[libraries]
hikage-runtime-attribute = { module = "com.highcapable.hikage:hikage-runtime-attribute", version.ref = "hikage-runtime-attribute" }
```

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation(libs.hikage.runtime.attribute)
```

Please change `<version>` to the version displayed at the top of this document.

### Traditional Method

Configure dependency in your project's `build.gradle.kts`.

```kotlin
implementation("com.highcapable.hikage:hikage-runtime-attribute:<version>")
```

Please change `<version>` to the version displayed at the top of this document.

## Function Introduction

You can view the KDoc [click here](kdoc://hikage-runtime-attribute).

### Compatibility Report

The following is a list of system versions tested by Hikage and their corresponding devices information for reference.

| Android Version / API Level | Availability |    Test Devices     |
| :-------------------------: | :----------: | :-----------------: |
|         5.0.2 (21)          |      ✅       |    Redmi Note 2     |
|          6.0 (23)           |      ✅       |      OPPO A53       |
|          7.0 (24)           |      ✅       |    Xiaomi Mi-4c     |
|         8.0.0 (26)          |      ✅       |      Huawei P9      |
|           10 (29)           |      ✅       |     Hisense A9      |
|           15 (35)           |      ✅       | Redmi Note 12 Turbo |
|           16 (36)           |      ✅       |   Xiaomi 15 Ultra   |
|           17 (37)           |      ✅       |      Emulator       |

### Use in Hikage

In most cases, the capabilities of this module will be used as the base of [hikage-core](./hikage-core.md). You do not need to use the APIs of this module directly to parse XML attribute sets.

For specific usage, please refer to [hikage-core → XML Attribute Sets](../library/hikage-core.md#xml-attribute-sets).

### Use in Any Project

Hikage provides decoupled API capabilities for this module. You can use the XML attribute set parsing capabilities provided by this module out of the box in any project.

#### AttributeSetResolver

We create an XML resource of `android:text` attribute for `TextView` and parse it into an `AttributeSet` object.

> The following example

```kotlin
// Assume this is your Context instance.
val context: Context
// Create an AttributeSet resolver instance.
val resolver = AttributeSetResolver.from(context)
// Returns an XmlResourceParser object, which is AttributeSet.
val attrs = resolver.newParser(
    listOf(
        AttributeItem.from(
            name = "android:text", 
            value = AttributeItem.Value.Str("Hello dynamic attribute!")
        )
    )
)
// Create a TextView and pass in the parsed AttributeSet.
val tv = TextView(context, attrs)
```

At this time, when `tv` is loaded into a parent layout or rendered in a layout, the `android:text` attribute will be correctly set and display "Hello dynamic attribute!".

`AttributeSetResolver` supports passing in the `AttributeResolverParams` parameter to configure its parsing behavior.

> The following example

```kotlin
// Assume this is your Context instance.
val context: Context
// Assume this is the AttributeItem list you need to construct.
val items: List<AttributeItem>
// Create parameter object
val params = AttributeResolverParams(
    // Specify the forged source XML layout resource ID,
    // only used to provide context information, and will not actually inflate.
    sourceResId = R.layout.my_layout
)
// Create an AttributeSet resolver instance
val resolver = AttributeSetResolver.from(context)
// Pass in the parameter object to parse the AttributeSet
val attrs = resolver.newParser(items, params)
```

`AttributeSetResolver` implements the `Closeable` interface. You can call the `close()` method to release the resources it occupies when you don't need to use it, or use `use` to automatically manage resource release.

> The following example

```kotlin
// Assume this is your Context instance.
val context: Context
// Assume this is the AttributeItem list you need to construct.
val items: List<AttributeItem>
// Create an AttributeSet resolver instance and use it in the use block.
val attrs = AttributeSetResolver.from(context).use { resolver ->
    // Returns an XmlResourceParser object, which is AttributeSet.
    resolver.newParser(items)
}
// Create a TextView and pass in the parsed AttributeSet.
val tv = TextView(context, attrs)
```

`AttributeSetResolver` also provides a `release(parser)` method, which is mainly targeted at the `layoutlib` of the Android Studio preview function. There is no need to call this method in actual device operation.

#### AttributeItem

`AttributeItem` is a data class used to create an `AttributeSet`. You can construct any XML attribute set through it.

`AttributeItem.Value` provides three types involved in the construction process: `Str`, `Raw`, and `Bool`.

> The following example

```kotlin
val items = listOf(
    AttributeItem.from(
        name = "android:text", 
        // Most commonly used, just pass in a string directly.
        value = AttributeItem.Value.Str("This is a text")
    ),
    AttributeItem.from(
        name = "android:textSize", 
        // Strings can be parsed to the final raw value.
        value = AttributeItem.Value.Str("16sp")
    ),
    AttributeItem.from(
        name = "android:padding", 
        // Strings can also be parsed into attribute references.
        value = AttributeItem.Value.Str("?attr/actionBarSize")
    ),
    AttributeItem.from(
        name = "android:enabled", 
        // Only supports true or false.
        value = AttributeItem.Value.Bool(true)
    ),
    AttributeItem.from(
        name = "android:layout_weight", 
        // Raw numerical value or property value of types like ID, Enum, etc.
        value = AttributeItem.Value.Raw(1)
    )
)
```

If you are not sure what to use in what scenario, just choose `AttributeItem.Value.Str`, whatever is in XML is supported.

Of course, you can also give `namespace` to specify the attribute namespace instead of needing to use `:` to specify it every time.

> The following example

```kotlin
val item = AttributeItem.from(
    namespace = "android",
    name = "text", 
    value = AttributeItem.Value.Str("This is a text")
)
```

`AttributeItem` implements the `Serializable` interface. You can serialize it into any object such as JSON for transmission and deserialize it back into an object when needed.