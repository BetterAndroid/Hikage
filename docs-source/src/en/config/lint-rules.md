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
| [HikageableBeyondScope](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageableBeyondScopeDetector.kt)                  | `CORRECTNESS` | `ERROR`   | `10`     | Functions marked with `@Hikageable` can only be passed in `Hikage.Performer`.                                 |
| [HikageableFunctions](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageableFunctionsDetector.kt)                      | `CORRECTNESS` | `ERROR`   | `10`     | Functions which invoke `@Hikageable` functions must be marked with the `@Hikageable` annotation.              |
| [ReplaceWithHikageSafeTypeCast](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageSafeTypeCastDetector.kt)             | `USABILITY`   | `WARNING` | `5`      | Recommended to use `hikage.get<YourView>("your_id")` instead of `hikage["your_id"] as YourView`.              |
| [ReplaceWithHikageWidgets](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/WidgetsUsageDetector.kt)                        | `USABILITY`   | `WARNING` | `5`      | Use the built-in widget function component provided by Hikage like `TextView(...)` instead.                   |

</div>

## How to Disable or Adjust Rules

If you want to disable a specific rule or change its severity, you can create a `lint.xml` file in the project root.

> The following example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="ReplaceWithHikageWidgets" severity="ignore" />
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
        disable += "ReplaceWithHikageWidgets"
        warning += "ReplaceWithHikageSafeTypeCast"
        error += "HikageableFunctions"
    }
}
```

If you only want to check part of the rules, you can also use `checkOnly`.

> The following example

```kotlin
android {
    lint {
        checkOnly += setOf(
            "ReplaceWithHikageWidgets",
            "HikageableFunctions"
        )
    }
}
```

## Feedback

Currently, most Lint detectors are completed by AI Agent, and there may still be issues, such as complex UAST scenarios that have not been tested.
If you find that a Lint rule is misreporting or the Quick Fix cannot correctly fix the problem during use, you can provide an Issue ID to directly feedback to us on [GitHub Issues](repo://issues).

If you think some rules are not reasonable and overly restrictive on the code, or if you have some new rule suggestions,
please also feel free to provide feedback to us, and we will evaluate and adjust them.