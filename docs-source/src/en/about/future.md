# Looking Toward the Future

> The future is bright and uncertain, let us look forward to the future development space of `Hikage`.

## Future Plans

> Features that `Hikage` may add later are included here.

### Generate Components ID

`Hikage` may support the direct call function to generate component IDs customized with strings as required in the future.

> The following example

```kotlin
object MyLayout : HikageBuilder {

    override fun build() = Hikageable(context) {
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
// Create TypedHikage.
val myLayout = MyLayout.asTyped().build().create(context)
// Or, use lazy init.
val myLayout by context.lazyTypedHikage(MyLayout)
// Directly call the ID generated from the string.
val linLayout = myLayout.linLayout
val textView = myLayout.textView
// Get the root layout, i.e. LinearLayout.
val root = myLayout.root
```