/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ServerVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getNMS;
import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.getNSNMS;

public class ActionBarAPI extends Packet {

    private static boolean available = true;
    private static Method a;
    private static Object typeMessage;
    private static Constructor<?> chatConstructor;

    public static void sendActionBar(Player player, String message) {
        if (!available || !player.isOnline()) {
            return;
        }

        try {
            Object chatMessage = a.invoke(null, "{\"text\":\"" + message + "\"}");
            ServerVersion serverVersion = ServerVersion.getServerVersion();
            Object packet;
            if (serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_16)) {
                packet = chatConstructor.newInstance(chatMessage, typeMessage, UUID.randomUUID());
            } else {
                packet = chatConstructor.newInstance(chatMessage, typeMessage);
            }
            sendPacket(player, packet);
        } catch (Exception e) {
            available = false;
            e.printStackTrace();
        }
    }

    static {
        try {
            Class<?> icbc = getNSNMS("network.chat.IChatBaseComponent", "IChatBaseComponent");
            Class<?> ppoc = getNSNMS("network.protocol.game.PacketPlayOutChat", "PacketPlayOutChat");

            if (icbc.getDeclaredClasses().length > 0) {
                a = icbc.getDeclaredClasses()[0].getMethod("a", String.class);
            } else {
                a = getNMS("ChatSerializer").getMethod("a", String.class);
            }

            Class<?> typeMessageClass;
            ServerVersion serverVersion = ServerVersion.getServerVersion();
            boolean newConstructor = serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_16);
            if (serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_12)) {
                typeMessageClass = getNMS("ChatMessageType");
                typeMessage = typeMessageClass.getEnumConstants()[2];
            } else {
                typeMessageClass = byte.class;
                typeMessage = (byte) 2;
            }

            chatConstructor = newConstructor ?
                    ppoc.getConstructor(icbc, typeMessageClass, UUID.class) :
                    ppoc.getConstructor(icbc, typeMessageClass);
        } catch (Throwable e) {
            available = false;
            e.printStackTrace();
        }
    }

}