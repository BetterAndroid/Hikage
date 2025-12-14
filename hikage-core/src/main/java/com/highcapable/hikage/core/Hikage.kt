/*
 * Hikage - An Android responsive UI building tool.
 * Copyright (C) 2019 HighCapable
 * https://github.com/BetterAndroid/Hikage
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2025/2/25.
 */
@file:Suppress("unused", "FunctionName", "PropertyName", "ConstPropertyName", "UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package com.highcapable.hikage.core

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
import androidx.viewbinding.ViewBinding
import com.highcapable.betterandroid.ui.extension.binding.ViewBinding
import com.highcapable.betterandroid.ui.extension.binding.ViewBindingBuilder
import com.highcapable.betterandroid.ui.extension.component.base.DisplayDensity
import com.highcapable.betterandroid.ui.extension.component.base.getColorCompat
import com.highcapable.betterandroid.ui.extension.component.base.getColorStateListCompat
import com.highcapable.betterandroid.ui.extension.component.base.getDrawableCompat
import com.highcapable.betterandroid.ui.extension.component.base.getFontCompat
import com.highcapable.betterandroid.ui.extension.component.base.toDp
import com.highcapable.betterandroid.ui.extension.component.base.toPx
import com.highcapable.betterandroid.ui.extension.view.LayoutParamsWrapContent
import com.highcapable.betterandroid.ui.extension.view.ViewLayoutParams
import com.highcapable.betterandroid.ui.extension.view.inflate
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.annotation.Hikageable
import com.highcapable.hikage.bypass.XmlBlockBypass
import com.highcapable.hikage.core.base.HikageFactory
import com.highcapable.hikage.core.base.HikageFactoryBuilder
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.HikageView
import com.highcapable.hikage.core.base.LayoutParamsBody
import com.highcapable.hikage.core.base.PerformerException
import com.highcapable.hikage.core.base.ProvideException
import com.highcapable.hikage.core.extension.ResourcesScope
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.createInstanceOrNull
import com.highcapable.kavaref.extension.isNotSubclassOf
import com.highcapable.kavaref.resolver.ConstructorResolver
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * The Hikage core layout builder.
 *
 * A [Hikage] can have multiple levels of [Hikage.Performer].
 * @param factories the factories to customize the custom view in the initialization.
 */
class Hikage private constructor(private val factories: List<HikageFactory>) {

    companion object {

        /** The Android widget class prefix. */
        internal const val ANDROID_WIDGET_CLASS_PREFIX = "android.widget."

        /** The unspecified layout params value. */
        private const val LayoutParamsUnspecified = LayoutParamsWrapContent - 1

        /** The view constructors map. */
        private val viewConstructors = mutableMapOf<String, ViewConstructor<*>>()

        /** The view atomic id. */
        private val viewAtomicId = AtomicInteger(0x7F00000)

        /** Returns nothing. */
        private fun noGetter(): Nothing = error("No getter available.")

        /**
         * Automatically add [HikageFactory] to handle [LayoutInflater.Factory2].
         *
         * This [LayoutInflater] will be retrieved from the [Context] you passed in.
         *
         * This option is enabled by default and will add this feature when creating any [Hikage].
         * It can support the [View] autoboxing feature that supports libraries such as `androidx.appcompat`,
         * which can be disabled if you don't need it.
         */
        var isAutoProcessWithFactory2 = true

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        @JvmName("createTyped")
        inline fun <reified LP : ViewGroup.LayoutParams> create(
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            noinline factory: HikageFactoryBuilder.() -> Unit = {},
            noinline performer: HikagePerformer<LP>
        ) = create(classOf<LP>(), context, parent, attachToParent, factory, performer)

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        fun create(
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<ViewGroup.LayoutParams>
        ) = create(classOf<ViewGroup.LayoutParams>(), context, parent, attachToParent, factory, performer)

        /**
         * Create a new [Hikage].
         * @param lpClass the layout params type.
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage]
         */
        fun <LP : ViewGroup.LayoutParams> create(
            lpClass: Class<LP>,
            context: Context,
            parent: ViewGroup? = null,
            attachToParent: Boolean = parent != null,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<LP>
        ) = Hikage(HikageFactoryBuilder.create {
            if (isAutoProcessWithFactory2) add(HikageFactory(context.layoutInflater))
            factory()
        }.build()).apply {
            // If the parent view is specified and mark as attach to parent,
            // add it directly to the first position.
            if (parent != null && attachToParent) {
                val parentId = generateRandomViewId()
                provideView(parent, parentId)
            }

            newPerformer(lpClass, parent, attachToParent, context).apply(performer)
        }

        /**
         * Create a new [Hikage.Delegate].
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[LP]>
         */
        @JvmName("buildTyped")
        inline fun <reified LP : ViewGroup.LayoutParams> build(
            noinline factory: HikageFactoryBuilder.() -> Unit = {},
            noinline performer: HikagePerformer<LP>
        ) = build(classOf<LP>(), factory, performer)

        /**
         * Create a new [Hikage.Delegate].
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[ViewGroup.LayoutParams]>
         */
        fun build(
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<ViewGroup.LayoutParams>
        ) = build(classOf<ViewGroup.LayoutParams>(), factory, performer)

        /**
         * Create a new [Hikage.Delegate].
         * @param lpClass the layout params type.
         * @param factory the [HikageFactory] builder.
         * @param performer the performer body.
         * @return [Hikage.Delegate]<[LP]>
         */
        fun <LP : ViewGroup.LayoutParams> build(
            lpClass: Class<LP>,
            factory: HikageFactoryBuilder.() -> Unit = {},
            performer: HikagePerformer<LP>
        ) = Delegate(lpClass, factory, performer)
    }

    /**
     * The [Hikage.Performer] scope interface.
     */
    private interface PerformerScope : DisplayDensity, ResourcesScope {

        /**
         * Get the actual view id by [id].
         * @param id the view id.
         * @return [Int] or -1.
         */
        fun actualViewId(id: String): Int
    }

    /**
     * The view constructor class.
     * @param resolver the constructor resolver.
     * @param parameterCount the parameter count.
     */
    private inner class ViewConstructor<V : View>(
        private val resolver: ConstructorResolver<V>,
        private val parameterCount: Int
    ) {

        /**
         * Build the view.
         * @param context the context.
         * @param attrs the attribute set.
         * @return [V] or null.
         */
        fun build(context: Context, attrs: AttributeSet) = when (parameterCount) {
            2 -> resolver.createQuietly(context, attrs)
            1 -> resolver.createQuietly(context)
            else -> null
        }
    }

    /** The performer set. */
    private val performers = linkedSetOf<Performer<*>>()

    /** The view map. */
    private val views = linkedMapOf<String, View>()

    /** The view id map. */
    private val viewIds = mutableMapOf<String, Int>()

    /**
     * Get the root view.
     * @return [View]
     */
    val root get() = views.values.firstOrNull()
        ?: throw PerformerException("No root view found, are you sure you have provided any view?")

    /**
     * Get the root view [V].
     * @return [V]
     */
    inline fun <reified V : View> root() = root as? V?
        ?: throw PerformerException("Root view is not a type of ${classOf<V>().name}.")

    /**
     * Get the view by [id].
     * @param id the view id.
     * @return [View]
     */
    operator fun get(id: String) = views[id]
        ?: throw PerformerException("View with id \"$id\" not found.")

    /**
     * Get the view by [id].
     * @param id the view id.
     * @return [View] or null.
     */
    fun getOrNull(id: String) = views[id]

    /**
     * Get the view by [id] via [V].
     * @param id the view id.
     * @return [V]
     */
    @JvmName("getTyped")
    inline fun <reified V : View> get(id: String) = get(id) as? V
        ?: throw PerformerException("View with id \"$id\" is not a ${classOf<V>().name}.")

    /**
     * Get the view by [id] via [V].
     * @param id the view id.
     * @return [V] or null.
     */
    @JvmName("getOrNullTyped")
    inline fun <reified V : View> getOrNull(id: String) = getOrNull(id) as? V?

    /**
     * Get the actual view id by [id].
     * @param id the view id.
     * @return [Int] or -1.
     */
    fun getActualViewId(id: String) = viewIds[id] ?: -1

    /**
     * Create a new [View] via [V].
     * @param viewClass the view class.
     * @param id the view id, generated by default.
     * @param context the context.
     * @return [V]
     */
    private fun <V : View> createView(viewClass: Class<V>, id: String?, context: Context): V {
        val attrs = createAttributeSet(context)

        val view = createViewFromFactory(viewClass, id, context, attrs) ?: getViewConstructor(viewClass)?.build(context, attrs)
        if (view == null) throw PerformerException(
            "Create view of type ${viewClass.name} failed. " +
                "Please make sure the view class has a constructor with a single parameter of type Context."
        )

        provideView(view, id)
        return view
    }

    /**
     * Get the view constructor.
     * @param viewClass the view class.
     * @return [ViewConstructor]<[V]> or null.
     */
    private fun <V : View> getViewConstructor(viewClass: Class<V>) =
        viewConstructors[viewClass.name] as? ViewConstructor<V>? ?: run {
            var parameterCount = 0

            val twoParams = viewClass.resolve()
                .optional(silent = true)
                .firstConstructorOrNull { parameters(Context::class, AttributeSet::class) }
            val onceParam = viewClass.resolve()
                .optional(silent = true)
                .firstConstructorOrNull { parameters(Context::class) }

            val constructor = onceParam?.apply { parameterCount = 1 }
                ?: twoParams?.apply { parameterCount = 2 }

            val viewConstructor = constructor?.let { ViewConstructor(it, parameterCount) }
            if (viewConstructor != null) viewConstructors[viewClass.name] = viewConstructor

            viewConstructor
        }

    /**
     * Create a new [View] from [HikageFactory].
     * @param viewClass the view class.
     * @param id the view id.
     * @param context the context.
     * @param attrs the attribute set.
     * @return [V] or null.
     */
    private fun <V : View> createViewFromFactory(viewClass: Class<V>, id: String?, context: Context, attrs: AttributeSet): V? {
        val parent = performers.firstOrNull()?.parent
        var processed: V? = null

        factories.forEach { factory ->
            val params = PerformerParams(id, attrs, viewClass as Class<View>)

            val view = factory(parent, processed, context, params)
            if (view != null && view.javaClass isNotSubclassOf viewClass) throw PerformerException(
                "HikageFactory cannot cast the created view type \"${view.javaClass}\" to \"${viewClass.name}\", " +
                    "please confirm that the view type you created is correct."
            )

            if (view != null) processed = view as? V?
        }

        return processed
    }

    /**
     * Provide an exists [View].
     * @param view the view instance.
     * @param id the view id.
     * @return [String]
     */
    private fun provideView(view: View, id: String?): String {
        val (requireId, viewId) = generateViewId(id)
        view.id = viewId
        views[requireId] = view

        return requireId
    }

    /**
     * Generate view id from [id].
     * @param id the view id.
     * @return [Pair]<[String], [Int]>
     */
    private fun generateViewId(id: String?): Pair<String, Int> {
        /**
         * Generate a new view id.
         * @param id the view id.
         * @return [Int]
         */
        fun doGenerate(id: String): Int {
            val generateId = View.generateViewId()

            if (viewIds.contains(id)) throw PerformerException("View with id \"$id\" already exists.")
            viewIds[id] = generateId

            return generateId
        }

        val requireId = id ?: generateRandomViewId()
        val viewId = doGenerate(requireId)

        return requireId to viewId
    }

    /**
     * Generate random view id.
     * @return [String]
     */
    private fun generateRandomViewId() = "anonymous@${viewAtomicId.getAndIncrement().toHexString()}"

    /**
     * We just need a [AttributeSet] instance.
     * @param context the context.
     * @return [AttributeSet]
     */
    private fun createAttributeSet(context: Context): AttributeSet = XmlBlockBypass.newParser(context)

    /**
     * Start a new performer [LP].
     * @param lpClass the layout params type.
     * @param parent the parent view group.
     * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
     * @param context the context, priority is given to [parent]'s context.
     * @return [Performer]
     */
    private fun <LP : ViewGroup.LayoutParams> newPerformer(
        lpClass: Class<LP>,
        parent: ViewGroup? = null,
        attachToParent: Boolean = parent != null,
        context: Context? = null
    ) = Performer(lpClass, parent, attachToParent, context).apply {
        // Init [XmlBlockBypass] if the context is not null.
        context?.let { XmlBlockBypass.init(it) }
        performers.add(this)
    }

    /**
     * Require no [Performer].
     * @param name the process name.
     * @param block the block.
     */
    internal inline fun requireNoPerformers(name: String, block: () -> Unit) {
        val viewCount = views.size

        block()
        if (views.size != viewCount) throw PerformerException(
            "Performers are not allowed to appear in $name DSL creation process."
        )
    }

    /**
     * Include a delegate.
     * @param delegate the delegate.
     * @param context the context.
     * @param embedded the embedded flag.
     * @return [Hikage]
     */
    private fun include(delegate: Delegate<*>, context: Context, embedded: Boolean): Hikage {
        val hikage = delegate.create(context)
        if (!embedded) return hikage

        val duplicateId = hikage.viewIds.toList().firstOrNull { (k, _) -> viewIds.containsKey(k) }?.first
        if (duplicateId != null) throw PerformerException(
            "Embedded layout view IDs conflict, the view id \"$duplicateId\" is already exists."
        )

        viewIds.putAll(hikage.viewIds)
        views.putAll(hikage.views)
        performers.addAll(hikage.performers)

        return hikage
    }

    /**
     * The core performer entity of layout build [LP].
     * @param parent the parent view group.
     * @param attachToParent whether to attach the layout to the parent.
     * @param lpClass the layout params type.
     * @param baseContext the context to create the layout, priority is given to [parent]'s context.
     * if [parent] is null, it must be set manually.
     */
    inner class Performer<LP : ViewGroup.LayoutParams> internal constructor(
        private val lpClass: Class<LP>,
        internal val parent: ViewGroup?,
        private val attachToParent: Boolean,
        private val baseContext: Context? = null
    ) : PerformerScope {

        /** The current [Hikage]. */
        private val current get() = this@Hikage

        /**
         * The context to create the layout.
         * @return [Context]
         */
        private val context get() = parent?.context
            ?: baseContext
            ?: throw PerformerException("Parent layout is null or broken, Hikage.Performer need a Context to create the layout.")

        override fun actualViewId(id: String) = getActualViewId(id)

        override fun stringResource(@StringRes resId: Int, vararg formatArgs: Any) =
            if (formatArgs.isNotEmpty())
                context.getString(resId, *formatArgs)
            else context.getString(resId)
        override fun colorResource(@ColorRes resId: Int) = context.getColorCompat(resId)
        override fun stateColorResource(@ColorRes resId: Int) = context.getColorStateListCompat(resId)
        override fun drawableResource(@DrawableRes resId: Int) = context.getDrawableCompat(resId)
        override fun bitmapResource(@DrawableRes resId: Int) = context.getDrawableCompat(resId).toBitmap()
        override fun dimenResource(@DimenRes resId: Int) = context.resources.getDimension(resId)
        override fun fontResource(@FontRes resId: Int) = context.getFontCompat(resId)

        override fun <N : Number> N.toPx() = toPx(context)
        override fun <N : Number> N.toDp() = toDp(context)

        /** The count of providing views. */
        private var provideCount = 0

        /**
         * Provide a new [View] instance [V].
         * @param viewClass the view class.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @return [V]
         */
        @Hikageable
        fun <V : View> View(
            viewClass: Class<V>,
            lparams: LayoutParams? = null,
            id: String? = null,
            init: HikageView<V> = {}
        ): V {
            val lpDelegate = LayoutParams.from(current, lpClass, parent, lparams)
            val view = createView(viewClass, id, context)
            view.layoutParams = lpDelegate.create()

            requireNoPerformers(viewClass.name) { view.init() }
            startProvide(id, viewClass)
            addToParentIfRequired(view)

            return view
        }

        /**
         * Provide a new [View] instance [V].
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @return [V]
         */
        @Hikageable
        @JvmName("ViewTyped")
        inline fun <reified V : View> View(
            lparams: LayoutParams? = null,
            id: String? = null,
            noinline init: HikageView<V> = {}
        ) = View(classOf<V>(), lparams, id, init)

        /**
         * Provide a new [View] instance.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @return [View]
         */
        @Hikageable
        fun View(
            lparams: LayoutParams? = null,
            id: String? = null,
            init: HikageView<View> = {}
        ) = View<View>(lparams, id, init)

        /**
         * Provide a new [ViewGroup] instance [VG].
         *
         * Provide the new type of [ViewGroup.LayoutParams] down via [LP].
         *
         * - Note: The [VG] must be inherited from [ViewGroup].
         * @param viewClass the view class.
         * @param lpClass the layout params class.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @param performer the performer body.
         * @return [VG]
         */
        @Hikageable
        fun <VG : ViewGroup, LP : ViewGroup.LayoutParams> ViewGroup(
            viewClass: Class<VG>,
            lpClass: Class<LP>,
            lparams: LayoutParams? = null,
            id: String? = null,
            init: HikageView<VG> = {},
            performer: HikagePerformer<LP> = {}
        ): VG {
            val lpDelegate = LayoutParams.from(current, lpClass, parent, lparams)
            val view = createView(viewClass, id, context)
            view.layoutParams = lpDelegate.create()

            requireNoPerformers(viewClass.name) { view.init() }
            startProvide(id, viewClass)
            addToParentIfRequired(view)
            newPerformer(lpClass, view).apply(performer)

            return view
        }

        /**
         * Provide a new [ViewGroup] instance [VG].
         *
         * - Note: The [VG] must be inherited from [ViewGroup].
         * @param viewClass the view class.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @param performer the performer body.
         * @return [VG]
         */
        @Hikageable
        fun <VG : ViewGroup> ViewGroup(
            viewClass: Class<VG>,
            lparams: LayoutParams? = null,
            id: String? = null,
            init: HikageView<VG> = {},
            performer: HikagePerformer<ViewGroup.LayoutParams> = {}
        ) = ViewGroup(viewClass, classOf<ViewGroup.LayoutParams>(), lparams, id, init, performer)

        /**
         * Provide a new [ViewGroup] instance [VG].
         *
         * Provide the new type of [ViewGroup.LayoutParams] down via [LP].
         *
         * - Note: The [VG] must be inherited from [ViewGroup].
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @param performer the performer body.
         * @return [VG]
         */
        @Hikageable
        @JvmName("ViewGroupLP")
        inline fun <reified VG : ViewGroup, reified LP : ViewGroup.LayoutParams> ViewGroup(
            lparams: LayoutParams? = null,
            id: String? = null,
            noinline init: HikageView<VG> = {},
            noinline performer: HikagePerformer<LP> = {}
        ) = ViewGroup(classOf<VG>(), classOf<LP>(), lparams, id, init, performer)

        /**
         * Provide a new [ViewGroup] instance [VG].
         *
         * - Note: The [VG] must be inherited from [ViewGroup].
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param init the view initialization body.
         * @param performer the performer body.
         * @return [VG]
         */
        @Hikageable
        inline fun <reified VG : ViewGroup> ViewGroup(
            lparams: LayoutParams? = null,
            id: String? = null,
            noinline init: HikageView<VG> = {},
            noinline performer: HikagePerformer<ViewGroup.LayoutParams> = {}
        ) = ViewGroup(classOf<VG>(), lparams, id, init, performer)

        /**
         * Provide layout from [resId].
         * @param resId the layout resource id.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         */
        @Hikageable
        fun Layout(
            @LayoutRes resId: Int,
            lparams: LayoutParams? = null,
            id: String? = null
        ): View {
            val view = context.layoutInflater.inflate(resId, parent, attachToRoot = false)

            startProvide(id, view.javaClass, view)
            lparams?.create()?.let { view.layoutParams = it }
            provideView(view, id)
            addToParentIfRequired(view)

            return view
        }

        /**
         * Provide layout from [ViewBinding].
         * @param bindingBuilder the view binding builder.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @return [VB]
         */
        @Hikageable
        fun <VB : ViewBinding> Layout(
            bindingBuilder: ViewBindingBuilder<VB>,
            lparams: LayoutParams? = null,
            id: String? = null
        ): VB {
            val viewBinding = bindingBuilder.inflate(context.layoutInflater, parent, attachToParent = false)
            val view = viewBinding.root

            startProvide(id, view.javaClass, view)
            if (view.parent != null) throw ProvideException(
                "The ViewBinding($view) already has a parent, " +
                    "it may have been created using layout root node <merge> or <include>, cannot be provided."
            )

            lparams?.create()?.let { view.layoutParams = it }
            provideView(view, id)
            addToParentIfRequired(view)

            return viewBinding
        }

        /**
         * Provide layout from [ViewBinding].
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @return [VB]
         */
        @Hikageable
        inline fun <reified VB : ViewBinding> Layout(
            lparams: LayoutParams? = null,
            id: String? = null
        ) = Layout(ViewBinding<VB>(), lparams, id)

        /**
         * Provide layout from exists [View].
         * @param view the view instance.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @return [View]
         */
        @Hikageable
        fun Layout(
            view: View,
            lparams: LayoutParams? = null,
            id: String? = null
        ): View {
            if (view.parent != null) throw ProvideException("The view $view already has a parent, cannot be provided.")

            startProvide(id, view.javaClass, view)
            val lpDelegate = LayoutParams.from(current, lpClass, parent, lparams, view.layoutParams)
            view.layoutParams = lpDelegate.create()

            provideView(view, id)
            addToParentIfRequired(view)

            return view
        }

        /**
         * Provide layout from another [Hikage].
         *
         * - Note: [Hikage] view IDs will not copied to this layout
         *   when provides a complete [Hikage] instead of [Delegate].
         *
         * ```kotlin
         * val first: Hikage = Hikageable(context) {
         *     TextView(id = "textView")
         * }
         * val second: Hikage = Hikageable(context) {
         *     TextView(id = "textView")
         *     // The view ID "textView" will not copied to this layout,
         *     // so there will be no problem of ID overlap.
         *     Layout(first)
         * }
         * ```
         * @param hikage the hikage instance.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @return [Hikage]
         */
        @Hikageable
        fun Layout(
            hikage: Hikage,
            lparams: LayoutParams? = null,
            id: String? = null
        ): Hikage {
            val view = hikage.root
            startProvide(id, view.javaClass, view)

            val lpDelegate = LayoutParams.from(current = this@Hikage, lpClass, parent, lparams, view.layoutParams)
            if (view.parent != null) throw ProvideException(
                "The Hikage layout root view $view already has a parent, cannot be provided."
            )

            view.layoutParams = lpDelegate.create()

            provideView(view, id)
            addToParentIfRequired(view)

            return hikage
        }

        /**
         * Provide layout from another [Delegate].
         *
         * - Note: If you use [embedded], you must make sure that the introduced view IDs
         *   do not overlap with the view IDs in the current layout.
         * @param delegate the delegate instance.
         * @param lparams the view layout params.
         * @param id the view id, generated by default.
         * @param embedded whether to embed this layout (All view IDs and performers will be copied),
         * default is true.
         * @return [Hikage]
         */
        @Hikageable
        fun Layout(
            delegate: Delegate<*>,
            lparams: LayoutParams? = null,
            id: String? = null,
            embedded: Boolean = true
        ): Hikage {
            val hikage = include(delegate, context, embedded)
            return Layout(hikage, lparams, id)
        }

        /**
         * Create a new layout params.
         *
         * Usage:
         *
         * ```kotlin
         * View(
         *     lparams = LayoutParams {
         *         // You code here.
         *     }
         * )
         * ```
         * @see ViewLayoutParams
         * @param body the layout params body.
         * @return [LayoutParams]
         */
        fun LayoutParams(
            width: Int = LayoutParamsUnspecified,
            height: Int = LayoutParamsUnspecified,
            matchParent: Boolean = false,
            widthMatchParent: Boolean = false,
            heightMatchParent: Boolean = false,
            body: LayoutParamsBody<LP> = {}
        ) = LayoutParams.from(
            current, lpClass, parent,
            width, height, matchParent, widthMatchParent, heightMatchParent,
            body = body
        )

        /** If required, add the [view] to the [parent]. */
        private fun addToParentIfRequired(view: View) {
            if (attachToParent) parent?.addView(view)
        }

        /**
         * Call to start providing a new view.
         * @param id the view id.
         * @param viewClass the view class.
         * @param view the view instance.
         */
        private fun <V : View> startProvide(id: String?, viewClass: Class<V>, view: V? = null) {
            provideCount++

            if (provideCount > 1 && (parent == null || !attachToParent)) throw ProvideException(
                "Provide view ${view?.javaClass ?: viewClass}(${id?.let { "\"$it\""} ?: "<anonymous>"}) failed. ${
                    if (parent == null) "No parent view group found"
                    else "Parent view group declares attachToParent = false"
                }, you can only provide one view for the root view."
            )
        }
    }

    /**
     * The parameters of the [Performer].
     * @param id the view ID.
     * @param attrs the attributes set.
     * @param viewClass the view class.
     */
    data class PerformerParams internal constructor(
        val id: String?,
        val attrs: AttributeSet,
        val viewClass: Class<View>
    ) : Serializable

    /**
     * The [Hikage] layout params.
     * @see ViewLayoutParams
     * @param current the current [Hikage].
     * @param lpClass the layout params type.
     * @param parent the parent view group.
     */
    class LayoutParams private constructor(
        private val current: Hikage,
        private val lpClass: Class<ViewGroup.LayoutParams>,
        private val parent: ViewGroup?
    ) {

        /**
         * Builder params of body.
         */
        private class BodyBuilder(
            val width: Int,
            val height: Int,
            val matchParent: Boolean,
            val widthMatchParent: Boolean,
            val heightMatchParent: Boolean,
            val body: LayoutParamsBody<ViewGroup.LayoutParams>
        )

        /**
         * Builder params of wrapper.
         */
        private class WrapperBuilder(
            val delegate: LayoutParams?,
            val lparams: ViewGroup.LayoutParams?
        )

        /** The layout params body. */
        private var bodyBuilder: BodyBuilder? = null

        /** The layout params wrapper. */
        private var wrapperBuilder: WrapperBuilder? = null

        internal companion object {

            /**
             * Create a new [LayoutParams]
             * @see ViewLayoutParams
             * @param current the current [Hikage].
             * @param parent the parent view group.
             * @return [LayoutParams]
             */
            fun <LP : ViewGroup.LayoutParams> from(
                current: Hikage,
                lpClass: Class<LP>,
                parent: ViewGroup?,
                width: Int,
                height: Int,
                matchParent: Boolean,
                widthMatchParent: Boolean,
                heightMatchParent: Boolean,
                body: LayoutParamsBody<LP>
            ) = LayoutParams(current, lpClass as Class<ViewGroup.LayoutParams>, parent).apply {
                bodyBuilder = BodyBuilder(
                    width, height, matchParent, widthMatchParent, heightMatchParent,
                    body as LayoutParamsBody<ViewGroup.LayoutParams>
                )
            }

            /**
             * Create a new [LayoutParams].
             * @param current the current [Hikage].
             * @param parent the parent view group.
             * @param delegate the delegate.
             * @param lparams the another layout params.
             * @return [LayoutParams]
             */
            fun <LP : ViewGroup.LayoutParams> from(
                current: Hikage,
                lpClass: Class<LP>,
                parent: ViewGroup?,
                delegate: LayoutParams?,
                lparams: ViewGroup.LayoutParams? = null
            ) = LayoutParams(current, lpClass as Class<ViewGroup.LayoutParams>, parent).apply {
                wrapperBuilder = WrapperBuilder(delegate, lparams)
            }
        }

        /**
         * Create a default layout params.
         * @return [ViewGroup.LayoutParams]
         */
        private fun createDefaultLayoutParams(lparams: ViewGroup.LayoutParams? = null): ViewGroup.LayoutParams {
            val wrapped = lparams?.let {
                parent?.asResolver()?.optional(silent = true)?.firstMethodOrNull {
                    name = "generateLayoutParams"
                    parameters(ViewGroup.LayoutParams::class)
                    superclass()
                }?.invokeQuietly<ViewGroup.LayoutParams>(it)
            } ?: lparams

            return wrapped
                // Build a default.
                ?: lpClass.createInstanceOrNull(LayoutParamsWrapContent, LayoutParamsWrapContent)
                ?: throw PerformerException("Create default layout params failed.")
        }

        /**
         * Create the layout params.
         * @return [ViewGroup.LayoutParams]
         */
        fun create(): ViewGroup.LayoutParams {
            if (bodyBuilder == null && wrapperBuilder == null) throw PerformerException("No layout params builder found.")

            return bodyBuilder?.let {
                val lparams = ViewLayoutParams(lpClass, it.width, it.height, it.matchParent, it.widthMatchParent, it.heightMatchParent)
                current.requireNoPerformers(lparams.javaClass.name) { it.body(lparams) }

                lparams
            } ?: wrapperBuilder?.let {
                val lparams = it.delegate?.create() ?: it.lparams

                createDefaultLayoutParams(lparams)
            } ?: throw PerformerException("Internal error of build layout params.")
        }
    }

    /**
     * The delegate for [Hikage].
     * @param lpClass the layout params type.
     * @param factory the [HikageFactory] builder.
     * @param performer the performer body.
     */
    class Delegate<LP : ViewGroup.LayoutParams> internal constructor(
        private val lpClass: Class<LP>,
        private val factory: HikageFactoryBuilder.() -> Unit = {},
        private val performer: HikagePerformer<LP>
    ) {

        /**
         * Create a new [Hikage].
         * @param context the context to create the layout.
         * @param parent the parent view group.
         * @param attachToParent whether to attach the layout to the parent when the [parent] is filled.
         * @return [Hikage]
         */
        fun create(context: Context, parent: ViewGroup? = null, attachToParent: Boolean = parent != null) =
            create(lpClass, context, parent, attachToParent, factory, performer)
    }
}