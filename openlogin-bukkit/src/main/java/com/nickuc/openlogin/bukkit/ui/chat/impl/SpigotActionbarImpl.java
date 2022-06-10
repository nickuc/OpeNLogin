/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.chat.impl;

import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;
import com.nickuc.openlogin.bukkit.ui.chat.ActionbarAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;

public class SpigotActionbarImpl implements ActionbarAPI {

    public SpigotActionbarImpl() throws ReflectiveOperationException {
        BukkitReflection.getMethod(Player.class, "spigot");
        Class<?> baseComponentArrayClass = Array.newInstance(BaseComponent.class, 0).getClass();
        BukkitReflection.getMethod(Player.Spigot.class, "sendMessage", ChatMessageType.class, baseComponentArrayClass);
    }

    @Override
    public void send(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
