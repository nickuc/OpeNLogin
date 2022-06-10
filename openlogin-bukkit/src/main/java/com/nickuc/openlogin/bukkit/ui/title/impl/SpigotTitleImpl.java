/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.title.impl;

import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

public class SpigotTitleImpl implements TitleAPI {

    public SpigotTitleImpl() throws ReflectiveOperationException {
        BukkitReflection.getMethod(Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class);
        BukkitReflection.getMethod(Player.class, "resetTitle");
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
            player.sendTitle(finalTitle, finalSubtitle, title.start, title.duration, title.end);
        }
    }

    @Override
    public void reset(Player player) {
        player.resetTitle();
    }
}
