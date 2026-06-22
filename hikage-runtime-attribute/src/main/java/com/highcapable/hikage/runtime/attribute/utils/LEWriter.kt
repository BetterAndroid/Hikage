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
 * This file is created by fankes on 2026/6/3.
 */
package com.highcapable.hikage.runtime.attribute.utils

import java.io.ByteArrayOutputStream

/**
 * A little-endian byte writer.
 */
internal class LEWriter private constructor() {

    companion object {

        inline operator fun invoke(block: LEWriter.() -> Unit) = LEWriter().apply(block).toByteArray()
    }

    private val out = ByteArrayOutputStream()

    fun u8(v: Int) {
        out.write(v and 0xFF)
    }

    fun u16(v: Int) {
        out.write(v and 0xFF)
        out.write((v ushr 8) and 0xFF)
    }

    fun u32(v: Int) {
        u16(v and 0xFFFF)
        u16((v ushr 16) and 0xFFFF)
    }

    fun bytes(b: ByteArray) {
        out.write(b)
    }

    fun size() = out.size()

    fun toByteArray(): ByteArray = out.toByteArray()
}