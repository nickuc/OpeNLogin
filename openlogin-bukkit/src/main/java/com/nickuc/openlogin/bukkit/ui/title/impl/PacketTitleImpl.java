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

package com.nickuc.openlogin.bukkit.ui.title.impl;

import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;
import com.nickuc.openlogin.bukkit.serializer.chat.ChatComponentSerializer;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class PacketTitleImpl implements TitleAPI {

    private final Object enumTimes;
    private final Object enumTitle;
    private final Object enumSubtitle;
    private final Object enumReset;
    private final Constructor<?> timeTitleConstructor;
    private final Constructor<?> textTitleConstructor;

    public PacketTitleImpl() throws ReflectiveOperationException {
        Class<?> ppot = BukkitReflection.getClass("{nms}.PacketPlayOutTitle");
        Class<?> enumClass;

        Class<?>[] ppotDeclaredClasses = ppot.getDeclaredClasses();
        if (ppotDeclaredClasses.length > 0) {
            enumClass = ppotDeclaredClasses[0];
        } else {
            enumClass = BukkitReflection.getClass("{nms}.EnumTitleAction");
        }

        enumTimes = enumClass.getField("TIMES").get(null);
        enumTitle = enumClass.getField("TITLE").get(null);
        enumSubtitle = enumClass.getField("SUBTITLE").get(null);
        enumReset = enumClass.getField("RESET").get(null);

        timeTitleConstructor = ppot.getConstructor(enumClass, ChatComponentSerializer.icbc, int.class, int.class, int.class);
        textTitleConstructor = ppot.getConstructor(enumClass, ChatComponentSerializer.icbc);
    }

    @Override
    public void send(Player player, Title title) {
        if (title.title.isEmpty() && title.subtitle.isEmpty()) {
            reset(player);
        } else {
            String finalTitle = "§r";
            if (!title.title.isEmpty()) {
                finalTitle = title.title;
            }
            String finalSubtitle = "§r";
            if (!title.subtitle.isEmpty()) {
                finalSubtitle = title.subtitle;
            }

            try {
                Object chatTitle = ChatComponentSerializer.fromText(finalTitle);
                Object chatSubtitle = ChatComponentSerializer.fromText(finalSubtitle);
                Object timeTitlePacket = timeTitleConstructor.newInstance(enumTimes, null, title.start, title.duration, title.end);
                Object titlePacket = textTitleConstructor.newInstance(enumTitle, chatTitle);
                Object subtitlePacket = textTitleConstructor.newInstance(enumSubtitle, chatSubtitle);

                BukkitReflection.sendPacket(player, timeTitlePacket);
                BukkitReflection.sendPacket(player, titlePacket);
                BukkitReflection.sendPacket(player, subtitlePacket);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not send title for " + player.getName() + "!", e);
            }
        }
    }

    @Override
    public void reset(Player player) {
        try {
            Object resetTitlePacket = textTitleConstructor.newInstance(enumReset, null);
            BukkitReflection.sendPacket(player, resetTitlePacket);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not reset title for " + player.getName() + "!", e);
        }
    }
}
