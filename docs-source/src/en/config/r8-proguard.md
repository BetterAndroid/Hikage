# R8 & Proguard Obfuscation

> In most scenarios, app packages can be compressed through obfuscation.
> Here is an introduction to how to configure obfuscation rules.

`Hikage` itself does not require additional configuration of obfuscation rules, since `View` objects created by Hikage do not need to be defined in XML, they can be equally obfuscated.

However, please note that if a custom `View` is not defined in XML, the latest R8 rules will choose to obfuscate it. To prevent the constructor from being lost, you still need to add the following rules to avoid problems.

```
-keep,allowobfuscation class * extends android.view.View {
    <init>(...);
}
```

If `Hikage` itself encounters problems after being obfuscated, you can use the following more aggressive rules to prevent `Hikage` from being obfuscated.

```
-keep class com.highcapable.hikage.**
```