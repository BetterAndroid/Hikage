# Lint Rules

> To help developers discover potential problems at compile time and keep API usage consistent, `Hikage` provides a built-in set of Android Lint rules.

## Rule List

The following are all Lint rules currently in effect (Only effective for Kotlin language).

### hikage-core

<div class="lint-rules-table">

| Issue ID                                                                                                                                                             | Category      | Severity  | Priority | Brief Description                            |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- | --------- | -------- | -------------------------------------------- |
| [MissingHikageAttributeNamespace](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)              | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute missing namespace.          |
| [DuplicateHikageAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                     | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute duplicate.                  |
| [ReplaceWithHikageAttributeNamespaceShortcuts](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt) | `USABILITY`   | `WARNING` | `5`      | Hikage attribute namespace usage.            |
| [IneffectiveHikageLayoutAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)             | `CORRECTNESS` | `WARNING` | `5`      | Hikage layout attribute ineffective.         |
| [InvalidHikageAttributeName](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                   | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute name invalid.               |
| [InvalidHikageAttributeResourceReference](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)      | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute resource reference invalid. |
| [InvalidHikageAttributeColorValue](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)             | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute color value invalid.        |
| [TooLongHikageAttributeString](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                 | `CORRECTNESS` | `ERROR`   | `6`      | Hikage attribute string too long.            |
| [HikagableBeyondScope](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikagableBeyondScopeDetector.kt)                    | `CORRECTNESS` | `ERROR`   | `10`     | Hikagable beyond scope.                      |
| [MissingHikagableAnnotation](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikagablePropagationDetector.kt)              | `CORRECTNESS` | `ERROR`   | `10`     | Missing @Hikagable annotation.               |
| [ReplaceWithHikageSafeTypeCast](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageSafeTypeCastDetector.kt)             | `USABILITY`   | `WARNING` | `5`      | Hikage safe type cast usage.                 |
| [UseHikageResourcesScope](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageResourcesScopeDetector.kt)                 | `CORRECTNESS` | `WARNING` | `5`      | Hikage resources scope violation.            |
| [RemoveHikagePerformerAlias](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikagePerformerAliasDetector.kt)              | `CORRECTNESS` | `WARNING` | `6`      | Hikage performer import alias.               |
| [ReplaceWithGeneratedHikagePerformer](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/GeneratedHikagePerformerDetector.kt) | `USABILITY`   | `WARNING` | `5`      | Hikage generated performer function usage.   |

</div>

## How to Disable or Adjust Rules

If you want to disable a specific rule or change its severity, you can create a `lint.xml` file in the project root.

> The following example

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="ReplaceWithGeneratedHikagePerformer" severity="ignore" />
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
        disable += "ReplaceWithGeneratedHikagePerformer"
        warning += "ReplaceWithHikageSafeTypeCast"
        error += "MissingHikagableAnnotation"
    }
}
```

If you only want to check part of the rules, you can also use `checkOnly`.

> The following example

```kotlin
android {
    lint {
        checkOnly += setOf(
            "ReplaceWithGeneratedHikagePerformer",
            "MissingHikagableAnnotation"
        )
    }
}
```

## Feedback

Currently, most Lint detectors are completed by AI Agent, and there may still be issues, such as complex UAST scenarios that have not been tested.
If you find that a Lint rule is misreporting or the Quick Fix cannot correctly fix the problem during use, you can provide an Issue ID to directly feedback to us on [GitHub Issues](repo://issues).

If you think some rules are not reasonable and overly restrictive on the code, or if you have some new rule suggestions,
please also feel free to provide feedback to us, and we will evaluate and adjust them.