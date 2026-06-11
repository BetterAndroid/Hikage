# Lint Rules

> To help developers discover potential problems at compile time and keep API usage consistent, `Hikage` provides a built-in set of Android Lint rules.

## Rule List

The following are all Lint rules currently in effect (Only effective for Kotlin language).

### hikage-core

<div class="lint-rules-table">

| Issue ID                                                                                                                                                             | Category      | Severity  | Priority | Brief Description                                                                                             |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- | --------- | -------- | ------------------------------------------------------------------------------------------------------------- |
| [MissingHikageAttributeNamespace](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)              | `CORRECTNESS` | `ERROR`   | `6`      | Attributes declared at the root attribute scope must include a namespace prefix.                              |
| [DuplicateHikageAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                     | `CORRECTNESS` | `ERROR`   | `6`      | Attributes declared in the same attribute scope must not use duplicate keys.                                  |
| [ReplaceWithHikageAttributeNamespaceShortcuts](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt) | `USABILITY`   | `WARNING` | `5`      | Use Hikage attribute namespace shortcuts and keep attribute names consistent with their namespace scope.      |
| [IneffectiveHikageLayoutAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)             | `CORRECTNESS` | `WARNING` | `5`      | Attributes with the `layout_` prefix have no effect when `lparams` is specified in the same view declaration. |
| [InvalidHikageAttributeName](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                   | `CORRECTNESS` | `ERROR`   | `6`      | Attribute names must use a valid namespace prefix and local name.                                             |
| [InvalidHikageAttributeResourceReference](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)      | `CORRECTNESS` | `ERROR`   | `6`      | Resource references must use the same resource reference format as XML.                                       |
| [InvalidHikageAttributeColorValue](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)             | `CORRECTNESS` | `ERROR`   | `6`      | Color values must be #RGB, #ARGB, #RRGGBB or #AARRGGBB.                                                       |
| [TooLongHikageAttributeString](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                 | `CORRECTNESS` | `ERROR`   | `6`      | Attribute strings must fit in the binary XML string pool.                                                     |
| [HikageableBeyondScope](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageableBeyondScopeDetector.kt)                  | `CORRECTNESS` | `ERROR`   | `10`     | Functions marked with `@Hikageable` can only be passed in `Hikage.Performer`.                                 |
| [MissingHikageableAnnotation](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageablePropagationDetector.kt)            | `CORRECTNESS` | `ERROR`   | `10`     | Functions which invoke `@Hikageable` functions must be marked with the `@Hikageable` annotation.              |
| [ReplaceWithHikageSafeTypeCast](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageSafeTypeCastDetector.kt)             | `USABILITY`   | `WARNING` | `5`      | Recommended to use `hikage.get<YourView>("your_id")` instead of `hikage["your_id"] as YourView`.              |
| [ReplaceWithHikageComponents](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageComponentsUsageDetector.kt)            | `USABILITY`   | `WARNING` | `5`      | Use the generated Hikage component function like `TextView(...)` instead.                                     |

</div>

## How to Disable or Adjust Rules

If you want to disable a specific rule or change its severity, you can create a `lint.xml` file in the project root.

> The following example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="ReplaceWithHikageComponents" severity="ignore" />
    <issue id="ReplaceWithHikageSafeTypeCast" severity="error" />
</lint>
```

Common `severity` values are:

- `ignore`
- `warning`
- `error`
- `fatal`

You can also control them directly in Gradle.

> The following example

```kotlin
android {
    lint {
        disable += "ReplaceWithHikageComponents"
        warning += "ReplaceWithHikageSafeTypeCast"
        error += "MissingHikageableAnnotation"
    }
}
```

If you only want to check part of the rules, you can also use `checkOnly`.

> The following example

```kotlin
android {
    lint {
        checkOnly += setOf(
            "ReplaceWithHikageComponents",
            "MissingHikageableAnnotation"
        )
    }
}
```

## Feedback

Currently, most Lint detectors are completed by AI Agent, and there may still be issues, such as complex UAST scenarios that have not been tested.
If you find that a Lint rule is misreporting or the Quick Fix cannot correctly fix the problem during use, you can provide an Issue ID to directly feedback to us on [GitHub Issues](repo://issues).

If you think some rules are not reasonable and overly restrictive on the code, or if you have some new rule suggestions,
please also feel free to provide feedback to us, and we will evaluate and adjust them.