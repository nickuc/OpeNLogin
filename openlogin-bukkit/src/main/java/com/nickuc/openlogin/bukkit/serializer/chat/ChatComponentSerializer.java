/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.serializer.chat;

import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChatComponentSerializer {

    public static final Class<?> icbc;
    private static final Method a;

    static {
        try {
            icbc = BukkitReflection.getClass("net.minecraft.network.chat.IChatBaseComponent", "{nms}.IChatBaseComponent");
        } catch (Throwable throwable) {
            throw new RuntimeException("Could not find IChatBaseComponent class!", throwable);
        }

        Method a2 = null;
        try {
            Class<?>[] icbcDeclaredClasses = icbc.getDeclaredClasses();
            if (icbcDeclaredClasses.length > 0) {
                a2 = icbcDeclaredClasses[0].getMethod("a", String.class);
            } else {
                a2 = BukkitReflection.getClass("{nms}.ChatSerializer").getMethod("a", String.class);
            }
        } catch (Throwable ignored) {
        }

        a = a2;
    }

    public static Object fromText(String text) {
        return fromJson("{\"text\":\"" + text + "\"}");
    }

    public static Object fromJson(String json) {
        if (a == null) {
            throw new IllegalStateException("ChatComponent api not loaded!");
        }

        try {
            return a.invoke(null, json);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not serialize ChatComponent! \"" + json + "\"", e);
        }
    }
}
