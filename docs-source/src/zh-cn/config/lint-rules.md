# Lint 静态检查规范

> 为了帮助开发者在编译期及时发现潜在问题、规范 API 调用，`Hikage` 内置了一套 Android Lint 规则。

## 规则列表

以下是目前生效的所有 Lint 规则列表 (仅对 Kotlin 语言生效)。

### hikage-core

<div class="lint-rules-table">

| Issue ID                                                                                                                                                             | 类别          | 级别      | 优先级 | 简要描述                                                                                                      |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- | --------- | ------ | ------------------------------------------------------------------------------------------------------------- |
| [MissingHikageAttributeNamespace](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)              | `CORRECTNESS` | `ERROR`   | `6`    | Attributes declared at the root attribute scope must include a namespace prefix.                              |
| [DuplicateHikageAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)                     | `CORRECTNESS` | `ERROR`   | `6`    | Attributes declared in the same attribute scope must not use duplicate keys.                                  |
| [ReplaceWithHikageAttributeNamespaceShortcuts](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt) | `USABILITY`   | `WARNING` | `5`    | Use Hikage attribute namespace shortcuts and keep attribute names consistent with their namespace scope.      |
| [IneffectiveHikageLayoutAttribute](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageAttributeDetector.kt)             | `CORRECTNESS` | `WARNING` | `5`    | Attributes with the `layout_` prefix have no effect when `lparams` is specified in the same view declaration. |
| [HikageableBeyondScope](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageableBeyondScopeDetector.kt)                  | `COMPLIANCE`  | `ERROR`   | `10`   | Functions marked with `@Hikageable` can only be passed in `Hikage.Performer`.                                 |
| [HikageableFunctions](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageableFunctionsDetector.kt)                      | `COMPLIANCE`  | `ERROR`   | `10`   | Functions which invoke `@Hikageable` functions must be marked with the `@Hikageable` annotation.              |
| [UseHikageSafeTypeCast](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/HikageSafeTypeCastDetector.kt)                     | `COMPLIANCE`  | `WARNING` | `5`    | Recommended to use `hikage.get<YourView>("your_id")` instead of `hikage["your_id"] as YourView`.              |
| [ReplaceWithHikageWidgets](repo://tree/main/hikage-core-lint/src/main/java/com/highcapable/hikage/core/lint/detector/WidgetsUsageDetector.kt)                        | `USABILITY`   | `WARNING` | `5`    | Use the built-in widget function component provided by Hikage like `TextView(...)` instead.                   |

</div>

## 如何关闭或调整规则

如果你想关闭某一条规则，或将某一条规则调整为其它级别，可以在项目根目录创建 `lint.xml`。

> 示例如下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="ReplaceWithHikageWidgets" severity="ignore" />
    <issue id="UseHikageSafeTypeCast" severity="error" />
</lint>
```

其中 `severity` 常用值如下：

- `ignore`
- `warning`
- `error`
- `fatal`

你也可以直接在 Gradle 中进行控制。

> 示例如下

```kotlin
android {
    lint {
        disable += "ReplaceWithHikageWidgets"
        warning += "UseHikageSafeTypeCast"
        error += "HikageableFunctions"
    }
}
```

当你只想检查部分规则时，也可以使用 `checkOnly`。

> 示例如下

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

## 问题反馈

目前大部分 Lint 检测器均由 AI Agent 代为完成，可能仍然存在问题，例如尚未测试过的复杂 UAST 语法树场景。如果你在使用过程中发现了 Lint 规则出现了误报或 Quick Fix 无法正确修复问题，可以通过提供 Issue ID 在 [GitHub Issues](repo://issues) 直接向我们反馈。

如果你认为一些规则不够合理出现过于约束代码的情况，或者你有一些新的规则建议，也欢迎向我们反馈，我们会进行评估和调整。