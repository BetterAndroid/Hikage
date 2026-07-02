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
 * This file is created by fankes on 2026/7/2.
 */
package com.highcapable.hikage.runtime.attribute

import android.content.Context
import android.content.ContextWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.highcapable.hikage.runtime.attribute.entity.AttributeItem
import com.highcapable.hikage.runtime.attribute.entity.AttributeResolverParams
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests [AttributeResolverParams].
 */
@RunWith(AndroidJUnit4::class)
class AttributeResolverParamsTest {

    @Test
    fun explicitResourcePackageNameResolvesUnqualifiedReferences() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val wrapped = WrongPackageNameContext(context)
        val params = AttributeResolverParams(resourcePackageName = context.packageName)
        val attrs = listOf(
            AttributeItem.from("android:contentDescription", AttributeItem.Value.Str("@layout/layout_hikage_view_tree_node"))
        )

        AttributeSetResolver.from(wrapped).use { resolver ->
            val parser = resolver.newParser(attrs, params)
            try {
                val actual = parser.getAttributeResourceValue(
                    "http://schemas.android.com/apk/res/android",
                    "contentDescription",
                    0
                )
                assertEquals(R.layout.layout_hikage_view_tree_node, actual)
            } finally {
                resolver.release(parser)
            }
        }
    }

    private class WrongPackageNameContext(base: Context) : ContextWrapper(base) {

        override fun getPackageName() = "${baseContext.packageName}.wrong"
    }
}