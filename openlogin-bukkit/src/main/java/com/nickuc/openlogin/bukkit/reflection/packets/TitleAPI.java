/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.common.model.Title;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getMethod;
import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getNMS;

public class TitleAPI extends Packet {

    private static Method a;
    private static Object enumTIMES;
    private static Object enumTITLE;
    private static Object enumSUBTITLE;
    private static Constructor<?> timeTitleConstructor;
    private static Constructor<?> textTitleConstructor;
    private static byte type;
    private static Method sendTitleMethod;
    private static Method resetTitleMethod;

    public static void sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
        if (type == 0) {
            return;
        }
        try {
            if (resetTitleMethod != null && title.isEmpty() && subtitle.isEmpty()) {
                resetTitleMethod.invoke(player);
                return;
            }

            switch (type) {
                case 1:
                    Object chatTitle = a.invoke(null, "{\"text\":\"" + title + "\"}");
                    Object chatSubtitle = a.invoke(null, "{\"text\":\"" + subtitle + "\"}");
                    Object timeTitlePacket = timeTitleConstructor.newInstance(enumTIMES, null, fadeIn, stay, fadeOut);
                    Object titlePacket = textTitleConstructor.newInstance(enumTITLE, chatTitle);
                    Object subtitlePacket = textTitleConstructor.newInstance(enumSUBTITLE, chatSubtitle);

                    sendPacket(player, timeTitlePacket);
                    sendPacket(player, titlePacket);
                    sendPacket(player, subtitlePacket);
                    break;

                case 2:
                case 3:
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

    public static void sendTitle(@NonNull Player player, @NonNull Title title) {
        sendTitle(player, title.start, title.duration, title.end, title.title, title.subtitle);
    }

    static {
        load();
    }

    private static void load() {
        try {
            resetTitleMethod = getMethod(craftPlayerClass, "resetTitle");
        } catch (Throwable ignored) {}

        try {
            load116();
            type = 1;
            return;
        } catch (Throwable ignored) {}
        try {
            sendTitleMethod = getMethod(Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class);
            type = 2;
            return;
        } catch (Throwable ignored) {}

        try {
            sendTitleMethod = getMethod(craftPlayerClass, "sendTitle", String.class, String.class, int.class, int.class, int.class);
            type = 3;
            return;
        } catch (Throwable ignored) {}
        try {
            sendTitleMethod = getMethod(Player.class, "sendTitle", String.class, String.class);
            type = 4;
        } catch (Throwable ignored) {}
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

}