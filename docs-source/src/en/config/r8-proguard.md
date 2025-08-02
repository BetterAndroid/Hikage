# R8 & Proguard Obfuscation

> In most scenarios, app packages can be compressed through obfuscation.
> Here is an introduction to how to configure obfuscation rules.

`Hikage` does not require additional configuration of obfuscation rules, since `View` objects loaded by Hikage do not need to be defined in XML, they can be equally obfuscated.

You can force them to be obfuscated with your custom `View`, such as `com.yourpackage.YourView`, using the following rules.

```
-allowobfuscation class com.yourpackage.YourView
```

If you must prevent `Hikage` from being obfuscated or if something occurs after being obfuscated, you can use the following rules to prevent `Hikage` from being obfuscated.

```
-keep class com.highcapable.hikage**
```