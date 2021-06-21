/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.nickuc.openlogin.bukkit.reflection.ReflectionUtils.*;

public abstract class Packet {

    public static Class<?> craftPlayerClass, entityPlayerClass, playerConnectionClass;
    private static Method getHandleMethod, sendPacketMethod;
    private static Field playerConnectionField;

    static {
        try {
            craftPlayerClass = getOBC("entity.CraftPlayer");
            entityPlayerClass = getNSNMS("level.EntityPlayer", "EntityPlayer");
            playerConnectionClass = getNSNMS("network.PlayerConnection", "PlayerConnection");
            getHandleMethod = getMethod(craftPlayerClass, "getHandle");
            sendPacketMethod = getMethod(playerConnectionClass, "sendPacket", getNMS("Packet"));
            playerConnectionField = getField(entityPlayerClass, "playerConnection");
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void sendPacket(Player player, Object packet, Object... packets) throws InvocationTargetException, IllegalAccessException {
        Object entityPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConnectionField.get(entityPlayer);
        sendPacketMethod.invoke(playerConnection, packet);
    }

}
