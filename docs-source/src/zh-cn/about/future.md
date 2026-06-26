# 展望未来

> 未来是美好的，也是不确定的，让我们共同期待 `Hikage` 在未来的发展空间。

## 未来的计划

> 这里收录了 `Hikage` 可能会在后期添加的功能。

### 生成组件 ID

`Hikage` 未来可能会根据需求支持生成使用字符串自定义的组件 ID 的直接调用功能。

> 示例如下

```kotlin
object MyLayout : HikageBuilder {

    override fun build() = Hikagable(context) {
        LinearLayout(
            id = "lin_layout",
            lparams = LayoutParams(matchParent = true),
            init = {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }
        ) {
            TextView(id = "text_view") {
                text = "Hello, World!"
                textSize = 16f
                gravity = Gravity.CENTER
            }
        }
    }
}

val context: Context
// 创建 TypedHikage
val myLayout = MyLayout.asTyped().build().create(context)
// 或者，使用懒加载
val myLayout by context.lazyTypedHikage(MyLayout)
// 直接调用根据字符串生成的 ID
val linLayout = myLayout.linLayout
val textView = myLayout.textView
// 获取根布局，即 LinearLayout
val root = myLayout.root
```