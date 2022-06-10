/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
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
