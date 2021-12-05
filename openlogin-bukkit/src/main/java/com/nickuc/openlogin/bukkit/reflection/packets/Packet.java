/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ServerVersion;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.*;

public abstract class Packet {

    public static Class<?> craftPlayerClass, entityPlayerClass, playerConnectionClass;
    private static Method getHandleMethod, sendPacketMethod;
    private static Field playerConnectionField, playerNetworkManagerField;

    static {
        try {
            craftPlayerClass = getOBC("entity.CraftPlayer");
            entityPlayerClass = getNSNMS("level.EntityPlayer", "EntityPlayer");
            playerConnectionClass = getNSNMS("network.PlayerConnection", "PlayerConnection");
            playerConnectionField = getField(entityPlayerClass, "playerConnection");
            getHandleMethod = getMethod(craftPlayerClass, "getHandle");

            Class<?> packetClass = getNSNMS("network.protocol.Packet", "Packet");
            if (ServerVersion.getServerVersion().isGreaterThanOrEqualTo(ServerVersion.v1_18)) {
                Class<?> networkManager = Class.forName("net.minecraft.network.NetworkManager");
                for (Field field : playerConnectionClass.getDeclaredFields()) {
                    if (networkManager.isAssignableFrom(field.getType())) {
                        playerNetworkManagerField = field;
                        break;
                    }
                }

                try {
                    sendPacketMethod = getMethod(networkManager, "sendPacket", packetClass);
                } catch (NoSuchMethodException e) {
                    sendPacketMethod = getMethod(networkManager, "a", packetClass);
                }
            } else  {
                sendPacketMethod = getMethod(playerConnectionClass, "sendPacket", packetClass);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void sendPacket(Player player, Object packet, Object... packets) throws InvocationTargetException, IllegalAccessException {
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
