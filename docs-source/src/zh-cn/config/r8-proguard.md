# R8 与 Proguard 混淆

> 大部分场景下应用程序安装包可通过混淆压缩体积，这里介绍了混淆规则的配置方法。

`Hikage` 自身不需要额外配置混淆规则，由于 Hikage 装载的 `View` 不需要在 XML 中被定义，它们也可以同样被混淆。

但是请注意，如果自定义 `View` 没在 XML 中被定义，最新的 R8 规则将选择对其进行混淆，为了防止构造方法丢失，你依然需要添加以下规则以避免出现问题。

```
-keep,allowobfuscation class * extends android.view.View {
    <init>(...);
}
```

如果 `Hikage` 自身被混淆后发生了问题，那么你可以使用以下比较强硬的规则来防止 `Hikage` 被混淆。

```
-keep class com.highcapable.hikage.**
```