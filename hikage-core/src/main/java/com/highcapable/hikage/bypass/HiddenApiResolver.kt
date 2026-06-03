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
 * This file is created by fankes on 2026/6/3.
 */
package com.highcapable.hikage.bypass

import com.highcapable.betterandroid.system.extension.utils.AndroidVersion
import com.highcapable.kavaref.resolver.processor.MemberProcessor
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * The resolver for hidden API access.
 */
internal object HiddenApiResolver {

    /**
     * The resolver for member processor (bypasses hidden API restrictions on P+).
     * @return [MemberProcessor.Resolver]
     */
    val processor = object : MemberProcessor.Resolver() {

        override fun <T : Any> getDeclaredConstructors(declaringClass: Class<T>): List<Constructor<T>> =
            AndroidVersion.require(AndroidVersion.P, super.getDeclaredConstructors(declaringClass)) {
                runCatching {
                    HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Constructor<T>>().toList()
                }.getOrElse { super.getDeclaredConstructors(declaringClass) }
            }

        override fun <T : Any> getDeclaredMethods(declaringClass: Class<T>): List<Method> =
            AndroidVersion.require(AndroidVersion.P, super.getDeclaredMethods(declaringClass)) {
                runCatching {
                    HiddenApiBypass.getDeclaredMethods(declaringClass).filterIsInstance<Method>().toList()
                }.getOrElse { super.getDeclaredMethods(declaringClass) }
            }
    }
}