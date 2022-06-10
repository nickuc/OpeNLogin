/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.chat.impl;

import com.nickuc.openlogin.bukkit.enums.BukkitVersion;
import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;
import com.nickuc.openlogin.bukkit.serializer.chat.ChatComponentSerializer;
import com.nickuc.openlogin.bukkit.ui.chat.ActionbarAPI;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.UUID;

public class PacketActionbarImpl implements ActionbarAPI {

    public final UUID EMPTY_UUID = new UUID(0L, 0L);

    private final Object typeMessage;
    private final Constructor<?> chatConstructor;
    private final boolean isModern;

    public PacketActionbarImpl() throws ReflectiveOperationException {
        BukkitVersion bukkitVersion = BukkitVersion.getVersion();

        Class<?> typeMessageClass;
        if (bukkitVersion.isNewerOrEqual(BukkitVersion.v1_12)) {
            typeMessageClass = BukkitReflection.getClass("net.minecraft.network.chat.ChatMessageType", "{nms}.ChatMessageType");
            typeMessage = typeMessageClass.getEnumConstants()[2];
        } else {
            typeMessageClass = byte.class;
            typeMessage = (byte) 2;
        }

        Class<?> ppoc = BukkitReflection.getClass("net.minecraft.network.protocol.game.PacketPlayOutChat", "{nms}.PacketPlayOutChat");
        if (bukkitVersion.isNewerOrEqual(BukkitVersion.v1_16)) {
            chatConstructor = ppoc.getConstructor(ChatComponentSerializer.icbc, typeMessageClass, UUID.class);
            isModern = true;
        } else {
            chatConstructor = ppoc.getConstructor(ChatComponentSerializer.icbc, typeMessageClass);
            isModern = false;
        }
    }

    @Override
    public void send(Player player, String message) {
        try {
            Object chatMessage = ChatComponentSerializer.fromText(message);
            Object packet = isModern ?
                    chatConstructor.newInstance(chatMessage, typeMessage, EMPTY_UUID) :
                    chatConstructor.newInstance(chatMessage, typeMessage);
            BukkitReflection.sendPacket(player, packet);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not send actionbar for " + player.getName() + "!", e);
        }
    }
}
