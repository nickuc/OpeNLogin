/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ReflectionUtils;
import com.nickuc.openlogin.common.model.Title;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getNMS;

public class TitleAPI extends Packet {

    private static boolean available = true;
    private static Method a;
    private static Object enumTIMES, enumTITLE, enumSUBTITLE;
    private static Constructor<?> timeTitleConstructor, textTitleConstructor;
    private static Method sendTitleMethod;

    public static void sendTitle(@NonNull Player player, int fadeIn, int stay, int fadeOut, @NonNull String title, @NonNull String subtitle) {
        if (!available) {
            return;
        }

        if (sendTitleMethod == null) {
            try {
                Object chatTitle = a.invoke(null, "{\"text\":\"" + title + "\"}");
                Object chatSubtitle = a.invoke(null, "{\"text\":\"" + subtitle + "\"}");

                Object timeTitlePacket = timeTitleConstructor.newInstance(enumTIMES, null, fadeIn, stay, fadeOut);
                Object titlePacket = textTitleConstructor.newInstance(enumTITLE, chatTitle);
                Object subtitlePacket = textTitleConstructor.newInstance(enumSUBTITLE, chatSubtitle);

                sendPacket(player, timeTitlePacket);
                sendPacket(player, titlePacket);
                sendPacket(player, subtitlePacket);
            } catch (Exception e) {
                try {
                    try {
                        Player.class.getMethod("sendTitle", String.class, String.class).invoke(player, title, subtitle);
                    } catch (NoSuchMethodError | NoSuchMethodException ex) {
                        Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class).invoke(player, title, subtitle, fadeIn, stay, fadeOut);
                    }
                } catch (Exception ex) {
                    available = false;
                    ex.printStackTrace();
                }
            }
        } else {
            try {
                sendTitleMethod.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendTitle(@NonNull Player player, @NonNull Title title) {
        sendTitle(player, title.start, title.duration, title.end, title.title, title.subtitle);
    }

    static {
        try {
            load116();
        } catch (Throwable e) {
            try {
                load117();
            } catch (Throwable e2) {
                available = false;
            }
        }
    }

    private static void load116() throws Throwable {
        Class<?> icbc = getNMS("IChatBaseComponent");
        Class<?> ppot = getNMS("PacketPlayOutTitle");
        Class<?> enumClass;

        if (ppot.getDeclaredClasses().length > 0) {
            enumClass = ppot.getDeclaredClasses()[0];
        } else {
            enumClass = getNMS("EnumTitleAction");
        }
        if (icbc.getDeclaredClasses().length > 0) {
            a = icbc.getDeclaredClasses()[0].getMethod("a", String.class);
        } else {
            a = getNMS("ChatSerializer").getMethod("a", String.class);
        }
        enumTIMES = enumClass.getField("TIMES").get(null);
        enumTITLE = enumClass.getField("TITLE").get(null);
        enumSUBTITLE = enumClass.getField("SUBTITLE").get(null);
        timeTitleConstructor = ppot.getConstructor(enumClass, icbc, int.class, int.class, int.class);
        textTitleConstructor = ppot.getConstructor(enumClass, icbc);
    }

    private static void load117() throws Throwable {
        sendTitleMethod = ReflectionUtils.getMethod(craftPlayerClass, "sendTitle", String.class, String.class, int.class, int.class, int.class);
    }
}