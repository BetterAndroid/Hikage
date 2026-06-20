/*
 * Hikage - A real-time Android View runtime powered by Kotlin DSL.
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
@file:Suppress("FunctionName", "UNCHECKED_CAST")

package com.highcapable.hikage.core.layout

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.FractionRes
import androidx.annotation.IntegerRes
import androidx.annotation.LayoutRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
import androidx.viewbinding.ViewBinding
import com.highcapable.betterandroid.ui.extension.binding.ViewBindingBuilder
import com.highcapable.betterandroid.ui.extension.component.base.getColorCompat
import com.highcapable.betterandroid.ui.extension.component.base.getColorStateListCompat
import com.highcapable.betterandroid.ui.extension.component.base.getDrawableCompat
import com.highcapable.betterandroid.ui.extension.component.base.getFontCompat
import com.highcapable.betterandroid.ui.extension.component.base.toDp
import com.highcapable.betterandroid.ui.extension.component.base.toPx
import com.highcapable.betterandroid.ui.extension.view.inflate
import com.highcapable.betterandroid.ui.extension.view.layoutInflater
import com.highcapable.hikage.core.Hikage
import com.highcapable.hikage.core.attrs.HikageAttribute
import com.highcapable.hikage.core.base.HikagePerformer
import com.highcapable.hikage.core.base.HikageView
import com.highcapable.hikage.core.base.LayoutParamsBody
import com.highcapable.hikage.core.base.PerformerException
import com.highcapable.hikage.core.base.ProvideException
import com.highcapable.hikage.core.base.ViewConstructor
import com.highcapable.hikage.core.layout.session.LayoutSession
import kotlin.reflect.KClass

/**
 * The [Hikage]'s performer context of layout build [LP].
 * @param session the current [LayoutSession].
 * @param parent the parent view group.
 * @param attachToParent whether to attach the layout to the parent.
 * @param lpClass the layout params type.
 * @param baseContext the context to create the layout, priority is given to [parent]'s context.
 * if [parent] is null, it must be set manually.
 */
internal class PerformContextImpl<LP : ViewGroup.LayoutParams>(
    private val session: LayoutSession,
    private val lpClass: KClass<LP>,
    internal val parent: ViewGroup?,
    private val attachToParent: Boolean,
    private val baseContext: Context? = null
) : Hikage.Performer<LP>, PerformContext<LP> {

    /**
     * The context to create the layout.
     * @return [Context]
     */
    private val context get() = parent?.context
        ?: baseContext
        ?: throw PerformerException("Parent layout is null or broken, Performer need a Context to create the layout.")

    /**
     * Get the actual view id by [id].
     * @param id the view id.
     * @return [Int] or -1.
     */
    override fun actualViewId(id: String) = session.getActualViewId(id)

    override fun stringResource(@StringRes resId: Int, vararg formatArgs: Any) =
        if (formatArgs.isNotEmpty())
            context.getString(resId, *formatArgs)
        else context.getString(resId)
    override fun pluralStringResource(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any) =
        if (formatArgs.isNotEmpty())
            context.resources.getQuantityString(resId, quantity, *formatArgs)
        else context.resources.getQuantityString(resId, quantity)
    override fun pluralTextResource(@PluralsRes resId: Int, quantity: Int) = context.resources.getQuantityText(resId, quantity)
    override fun textResource(@StringRes resId: Int) = context.getText(resId)
    override fun stringArrayResource(@ArrayRes resId: Int) = context.resources.getStringArray(resId)
    override fun integerResource(@IntegerRes resId: Int) = context.resources.getInteger(resId)
    override fun integerArrayResource(@ArrayRes resId: Int) = context.resources.getIntArray(resId)
    override fun booleanResource(@BoolRes resId: Int) = context.resources.getBoolean(resId)
    override fun colorResource(@ColorRes resId: Int) = context.getColorCompat(resId)
    override fun stateColorResource(@ColorRes resId: Int) = context.getColorStateListCompat(resId)
    override fun drawableResource(@DrawableRes resId: Int) = context.getDrawableCompat(resId)
    override fun bitmapResource(@DrawableRes resId: Int) = context.getDrawableCompat(resId).toBitmap()
    override fun dimenResource(@DimenRes resId: Int) = context.resources.getDimension(resId)
    override fun dimenPixelSizeResource(@DimenRes resId: Int) = context.resources.getDimensionPixelSize(resId)
    override fun dimenPixelOffsetResource(@DimenRes resId: Int) = context.resources.getDimensionPixelOffset(resId)
    override fun fractionResource(@FractionRes resId: Int, base: Int, pbase: Int) = context.resources.getFraction(resId, base, pbase)
    override fun fontResource(@FontRes resId: Int) = context.getFontCompat(resId)

    override fun <N : Number> N.toPx() = toPx(context)
    override fun <N : Number> N.toDp() = toDp(context)

    /** The count of providing views. */
    private var provideCount = 0

    override fun <V : View> View(
        viewClass: KClass<V>,
        factory: ViewConstructor<V>?,
        lparams: LayoutParams?,
        id: String?,
        attrs: HikageAttribute,
        init: HikageView<V>
    ): V {
        val view = session.process(context, attrs) { attributeSet, lParamsAttributeSet ->
            val lpDelegate = LayoutParams.from(session, lpClass, parent, lparams, attrs = lParamsAttributeSet)
            session.createView(viewClass, factory, id, context, attributeSet, parent).apply {
                layoutParams = lpDelegate.create()
            }
        }

        session.requireNoPerformers(viewClass.qualifiedName) { view.init() }
        startProvide(id, viewClass)
        addToParentIfRequired(view)

        return view
    }

    override fun View(
        lparams: LayoutParams?,
        factory: ViewConstructor<View>?,
        id: String?,
        attrs: HikageAttribute,
        init: HikageView<View>
    ) = View(View::class, factory, lparams, id, attrs, init)

    override fun <VG : ViewGroup, NLP : ViewGroup.LayoutParams> ViewGroup(
        viewClass: KClass<VG>,
        childLpClass: KClass<NLP>,
        factory: ViewConstructor<VG>?,
        lparams: LayoutParams?,
        id: String?,
        attrs: HikageAttribute,
        init: HikageView<VG>,
        performer: HikagePerformer<NLP>
    ): VG {
        val view = session.process(context, attrs) { attributeSet, lparamsAttributeSet ->
            val lpDelegate = LayoutParams.from(session, lpClass, parent, lparams, attrs = lparamsAttributeSet)
            session.createView(viewClass, factory, id, context, attributeSet, parent).apply {
                layoutParams = lpDelegate.create()
            }
        }

        session.requireNoPerformers(viewClass.qualifiedName) { view.init() }
        startProvide(id, viewClass)
        addToParentIfRequired(view)
        session.newPerformer(childLpClass, view).apply(performer)

        return view
    }

    override fun <VG : ViewGroup> ViewGroup(
        viewClass: KClass<VG>,
        factory: ViewConstructor<VG>?,
        lparams: LayoutParams?,
        id: String?,
        attrs: HikageAttribute,
        init: HikageView<VG>,
        performer: HikagePerformer<ViewGroup.LayoutParams>
    ) = ViewGroup(viewClass, ViewGroup.LayoutParams::class, factory, lparams, id, attrs, init, performer)

    override fun Layout(
        @LayoutRes resId: Int,
        lparams: LayoutParams?,
        id: String?
    ): View {
        val view = context.layoutInflater.inflate(resId, parent, attachToRoot = false)

        startProvide(id, view::class, view)
        lparams?.create()?.let { view.layoutParams = it }
        session.provideView(view, id)
        addToParentIfRequired(view)

        return view
    }

    override fun <VB : ViewBinding> Layout(
        bindingBuilder: ViewBindingBuilder<VB>,
        lparams: LayoutParams?,
        id: String?
    ): VB {
        val viewBinding = bindingBuilder.inflate(context.layoutInflater, parent, attachToParent = false)
        val view = viewBinding.root

        startProvide(id, view::class, view)
        if (view.parent != null) throw ProvideException(
            "The ViewBinding($view) already has a parent, " +
                "it may have been created using layout root node <merge> or <include>, cannot be provided."
        )

        lparams?.create()?.let { view.layoutParams = it }
        session.provideView(view, id)
        addToParentIfRequired(view)

        return viewBinding
    }

    override fun Layout(
        view: View,
        lparams: LayoutParams?,
        id: String?
    ): View {
        if (view.parent != null) throw ProvideException("The view $view already has a parent, cannot be provided.")

        startProvide(id, view::class, view)
        val lpDelegate = LayoutParams.from(session, lpClass, parent, lparams, view.layoutParams)
        view.layoutParams = lpDelegate.create()

        session.provideView(view, id)
        addToParentIfRequired(view)

        return view
    }

    override fun Layout(
        hikage: Hikage,
        lparams: LayoutParams?,
        id: String?
    ): Hikage {
        val view = hikage.root
        startProvide(id, view::class, view)

        val lpDelegate = LayoutParams.from(session, lpClass, parent, lparams, view.layoutParams)
        if (view.parent != null) throw ProvideException(
            "The Hikage layout root view $view already has a parent, cannot be provided."
        )

        view.layoutParams = lpDelegate.create()

        session.provideView(view, id)
        addToParentIfRequired(view)

        return hikage
    }

    override fun Layout(
        delegate: Hikage.Delegate<*>,
        lparams: LayoutParams?,
        id: String?,
        embedded: Boolean
    ): Hikage {
        val hikage = session.include(delegate, context, embedded)
        return Layout(hikage, lparams, id)
    }

    override fun LayoutParams(
        width: Int?,
        height: Int?,
        matchParent: Boolean,
        widthMatchParent: Boolean,
        heightMatchParent: Boolean,
        body: LayoutParamsBody<LP>
    ) = LayoutParams.from(
        session, lpClass, parent,
        width, height, matchParent, widthMatchParent, heightMatchParent,
        body = body
    )

    /** If required, add the [view] to the [parent]. */
    private fun addToParentIfRequired(view: View) {
        if (attachToParent) parent?.addView(view, view.layoutParams)
    }

    /**
     * Call to start providing a new view.
     * @param id the view id.
     * @param viewClass the view class.
     * @param view the view instance.
     */
    private fun <V : View> startProvide(id: String?, viewClass: KClass<out V>, view: V? = null) {
        provideCount++

        if (provideCount > 1 && (parent == null || !attachToParent)) throw ProvideException(
            "Provide view ${view?.let { it::class } ?: viewClass}(${id?.let { "\"$it\"" } ?: "<anonymous>"}) failed. ${
                if (parent == null) "No parent view group found"
                else "Parent view group declares attachToParent = false"
            }, you can only provide one view for the root view."
        )
    }
}