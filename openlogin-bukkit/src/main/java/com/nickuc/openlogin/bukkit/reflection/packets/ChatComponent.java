/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ReflectionUtils;
import com.nickuc.openlogin.bukkit.reflection.ServerVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChatComponent extends Packet {

    public static final Class<?> icbc;
    private static Method a;

    static {
        try {
            icbc = ReflectionUtils.getClass("net.minecraft.network.chat.IChatBaseComponent", "{nms}.IChatBaseComponent");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Object chatComponentFromText(String text) {
        return chatComponentFromJson("{\"text\":\"" + text + "\"}");
    }

    public static Object chatComponentFromJson(String json) {
        try {
            return a.invoke(null, json);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new UnsupportedOperationException("Could not get ChatComponent from json! " + json);
    }

    public static void load() throws Throwable {
        try {
            Class<?>[] icbcDeclaredClasses = icbc.getDeclaredClasses();
            if (icbcDeclaredClasses.length > 0) {
                a = icbcDeclaredClasses[0].getMethod("a", String.class);
            } else {
                a = ReflectionUtils.getClass("{nms}.ChatSerializer").getMethod("a", String.class);
            }
        } catch (Throwable e) {
            if (ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_18)) {
                throw e;
            }
        }
    }

}
