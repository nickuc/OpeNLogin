/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ReflectionUtils;
import com.nickuc.openlogin.bukkit.reflection.ServerVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public class ActionBarAPI extends Packet {

    public static final UUID serverUuid = UUID.randomUUID();
    private static byte type;

    private static Method sendActionBar;

    private static Object typeMessage;
    private static Constructor<?> chatConstructor;

    static {
        try {
            load();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendActionBar(Player player, String message) {
        if (type == 0 || !player.isOnline()) {
            return;
        }

        try {
            switch (type) {
                case 1:
                case 2:
                    sendActionBar.invoke(player, message);
                    break;

                case 3:
                    Object chatMessage = ChatComponent.chatComponentFromText(message);
                    Object packet = ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_16) ?
                            chatConstructor.newInstance(chatMessage, typeMessage, serverUuid) :
                            chatConstructor.newInstance(chatMessage, typeMessage);
                    sendPacket(player, packet);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            type = 0;
        }
    }

    public static void load() throws Throwable {
        try {
            sendActionBar = ReflectionUtils.getMethod(Player.class, "sendActionBar", String.class);
            type = 1;
            return;
        } catch (Throwable ignored) {
        }

        try {
            sendActionBar = ReflectionUtils.getMethod(craftPlayerClass, "sendActionBar", String.class);
            type = 2;
            return;
        } catch (Throwable ignored) {
        }

        ServerVersion serverVersion = ServerVersion.getServerVersion();
        try {
            Class<?> typeMessageClass;
            if (serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_12)) {
                typeMessageClass = ReflectionUtils.getClass("net.minecraft.network.chat.ChatMessageType", "{nms}.ChatMessageType");
                typeMessage = typeMessageClass.getEnumConstants()[2];
            } else {
                typeMessageClass = byte.class;
                typeMessage = (byte) 2;
            }

            Class<?> ppoc = ReflectionUtils.getClass("net.minecraft.network.protocol.game.PacketPlayOutChat", "{nms}.PacketPlayOutChat");
            if (serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_16)) {
                chatConstructor = ppoc.getConstructor(ChatComponent.icbc, typeMessageClass, UUID.class);
            } else {
                chatConstructor = ppoc.getConstructor(ChatComponent.icbc, typeMessageClass);
            }
            type = 3;
        } catch (Throwable e) {
            if (serverVersion.isGreaterThanOrEqualTo(ServerVersion.v1_18)) {
                throw e;
            }
        }
    }

}