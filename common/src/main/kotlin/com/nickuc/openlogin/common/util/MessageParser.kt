/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 - 2026 - OpenLogin Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nickuc.openlogin.common.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

object MessageParser {

    private val miniMessage = MiniMessage.miniMessage()

    private val LEGACY_TAGS = mapOf(
        '0' to "black", '1' to "dark_blue", '2' to "dark_green", '3' to "dark_aqua",
        '4' to "dark_red", '5' to "dark_purple", '6' to "gold", '7' to "gray",
        '8' to "dark_gray", '9' to "blue", 'a' to "green", 'b' to "aqua",
        'c' to "red", 'd' to "light_purple", 'e' to "yellow", 'f' to "white",
        'k' to "obfuscated", 'l' to "bold", 'm' to "strikethrough", 'n' to "underlined",
        'o' to "italic", 'r' to "reset"
    )

    @JvmStatic
    fun parse(input: String, vararg placeholders: TagResolver): Component {
        return miniMessage.deserialize(convertLegacy(input), *placeholders)
    }

    private fun convertLegacy(input: String): String {
        val builder = StringBuilder(input.length)
        var i = 0
        while (i < input.length) {
            val c = input[i]
            val tag = if (c == '&' && i + 1 < input.length) LEGACY_TAGS[input[i + 1].lowercaseChar()] else null
            if (tag != null) {
                builder.append('<').append(tag).append('>')
                i += 2
            } else {
                builder.append(c)
                i++
            }
        }
        return builder.toString()
    }
}
