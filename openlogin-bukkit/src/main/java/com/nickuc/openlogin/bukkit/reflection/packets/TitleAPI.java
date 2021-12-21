/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ReflectionUtils;
import com.nickuc.openlogin.bukkit.reflection.ServerVersion;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TitleAPI extends Packet {

    private static byte type;

    private static Method sendTitleMethod;
    private static Method resetTitleMethod;

    // <= 1.16
    private static Object enumTIMES;
    private static Object enumTITLE;
    private static Object enumSUBTITLE;
    private static Constructor<?> timeTitleConstructor;
    private static Constructor<?> textTitleConstructor;

    public static void sendTitle(Player player, Title title) {
        sendTitle(player, title.start, title.duration, title.end, title.title, title.subtitle);
    }

    public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
        if (type == 0 || !player.isOnline()) {
            return;
        }
        try {
            if (resetTitleMethod != null && title.isEmpty() && subtitle.isEmpty()) {
                resetTitleMethod.invoke(player);
                return;
            }

            switch (type) {
                case 3:
                    Object chatTitle = ChatComponent.chatComponentFromText(title);
                    Object chatSubtitle = ChatComponent.chatComponentFromText(subtitle);
                    Object timeTitlePacket = timeTitleConstructor.newInstance(enumTIMES, null, fadeIn, stay, fadeOut);
                    Object titlePacket = textTitleConstructor.newInstance(enumTITLE, chatTitle);
                    Object subtitlePacket = textTitleConstructor.newInstance(enumSUBTITLE, chatSubtitle);

                    sendPacket(player, timeTitlePacket);
                    sendPacket(player, titlePacket);
                    sendPacket(player, subtitlePacket);
                    break;

                case 1:
                case 2:
                    if (title.isEmpty()) {
                        title = "§r";
                    }
                    if (subtitle.isEmpty()) {
                        subtitle = "§r";
                    }
                    sendTitleMethod.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
                    break;

                case 4:
                    if (title.isEmpty()) {
                        title = "§r";
                    }
                    if (subtitle.isEmpty()) {
                        subtitle = "§r";
                    }
                    sendTitleMethod.invoke(player, title, subtitle);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            type = 0;
        }
    }

    public static void load() throws Throwable {
        try {
            resetTitleMethod = ReflectionUtils.getMethod(craftPlayerClass, "resetTitle");
        } catch (Throwable ignored) {
        }

        // Player#sendTitle(String, String, int, int, int)
        try {
            sendTitleMethod = ReflectionUtils.getMethod(Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class);
            type = 1;
            return;
        } catch (Throwable ignored) {
        }

        // CraftPlayer#sendTitle(String, String, int, int, int)
        try {
            sendTitleMethod = ReflectionUtils.getMethod(craftPlayerClass, "sendTitle", String.class, String.class, int.class, int.class, int.class);
            type = 2;
            return;
        } catch (Throwable ignored) {
        }

        // <= 1.16
        if (ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_16)) {
            try {
                load116();
                type = 3;
            } catch (Throwable ignored) {
            }
        }

        try {
            // Player#sendTitle(String, String)
            sendTitleMethod = ReflectionUtils.getMethod(Player.class, "sendTitle", String.class, String.class);
            type = 4;
        } catch (Throwable e) {
            if (ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_18)) {
                throw e;
            }
        }
    }

    private static void load116() throws Throwable {
        Class<?> ppot = ReflectionUtils.getClass("{nms}.PacketPlayOutTitle");
        Class<?> enumClass;

        Class<?>[] ppotDeclaredClasses = ppot.getDeclaredClasses();
        if (ppotDeclaredClasses.length > 0) {
            enumClass = ppotDeclaredClasses[0];
        } else {
            enumClass = ReflectionUtils.getClass("{nms}.EnumTitleAction");
        }

        enumTIMES = enumClass.getField("TIMES").get(null);
        enumTITLE = enumClass.getField("TITLE").get(null);
        enumSUBTITLE = enumClass.getField("SUBTITLE").get(null);
        timeTitleConstructor = ppot.getConstructor(enumClass, ChatComponent.icbc, int.class, int.class, int.class);
        textTitleConstructor = ppot.getConstructor(enumClass, ChatComponent.icbc);
    }

}