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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Packet {

    public static Class<?> craftPlayerClass, entityPlayerClass, playerConnectionClass;
    private static Method getHandleMethod, sendPacketMethod;
    private static Field playerConnectionField, playerNetworkManagerField;

    static {
        try {
            craftPlayerClass = ReflectionUtils.getClass("{obc}.entity.CraftPlayer");
            entityPlayerClass = ReflectionUtils.getClass("net.minecraft.server.level.EntityPlayer", "{nms}.EntityPlayer");
            playerConnectionClass = ReflectionUtils.getClass("net.minecraft.server.network.PlayerConnection", "{nms}.PlayerConnection");
            playerConnectionField = ReflectionUtils.getField(entityPlayerClass, playerConnectionClass, 0);
            getHandleMethod = ReflectionUtils.getMethod(craftPlayerClass, "getHandle");

            Class<?> packetClass = ReflectionUtils.getClass("net.minecraft.network.protocol.Packet", "{nms}.Packet");

            // send packet method
            if (ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_18)) {
                Class<?> networkManager = Class.forName("net.minecraft.network.NetworkManager");
                for (Field field : playerConnectionClass.getDeclaredFields()) {
                    if (networkManager.isAssignableFrom(field.getType())) {
                        playerNetworkManagerField = field;
                        break;
                    }
                }

                try {
                    sendPacketMethod = ReflectionUtils.getMethod(networkManager, "sendPacket", packetClass);
                } catch (NoSuchMethodException e) {
                    sendPacketMethod = ReflectionUtils.getMethod(networkManager, "a", packetClass);
                }
            } else  {
                sendPacketMethod = ReflectionUtils.getMethod(playerConnectionClass, "sendPacket", packetClass);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void sendPacket(Player player, Object packet) throws InvocationTargetException, IllegalAccessException {
        Object entityPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConnectionField.get(entityPlayer);
        if (playerNetworkManagerField != null) {
            Object networkManager = playerNetworkManagerField.get(playerConnection);
            sendPacketMethod.invoke(networkManager, packet);
        } else {
            sendPacketMethod.invoke(playerConnection, packet);
        }
    }

}
