/*
 * The MIT License (MIT)
 *
 * Copyright © 2024 - OpenLogin Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
