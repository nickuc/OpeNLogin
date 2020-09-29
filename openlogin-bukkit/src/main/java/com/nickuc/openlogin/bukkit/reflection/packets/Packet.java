/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.reflection.packets;

import com.nickuc.openlogin.bukkit.reflection.ReflectionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Packet {

    public static Class<?> craftPlayerClass, entityPlayerClass, playerConnectionClass;
    private static Method getHandleMethod, sendPacketMethod;
    private static Field playerConnectionField;

    static {
        try {
            craftPlayerClass = ReflectionUtils.getOBC("entity.CraftPlayer");
            entityPlayerClass = ReflectionUtils.getNMS("EntityPlayer");
            playerConnectionClass = ReflectionUtils.getNMS("PlayerConnection");
            getHandleMethod = ReflectionUtils.getMethod(craftPlayerClass, "getHandle");
            sendPacketMethod = ReflectionUtils.getMethod(playerConnectionClass, "sendPacket", ReflectionUtils.getNMS("Packet"));
            playerConnectionField = ReflectionUtils.getField(entityPlayerClass, "playerConnection");
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
