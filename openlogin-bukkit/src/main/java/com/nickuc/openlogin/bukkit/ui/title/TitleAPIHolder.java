/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.title;

import com.nickuc.openlogin.bukkit.ui.title.impl.PacketTitleImpl;
import com.nickuc.openlogin.bukkit.ui.title.impl.SpigotTitleImpl;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

public class TitleAPIHolder {

    private static TitleAPI api;

    static {
        try {
            api = new SpigotTitleImpl();
        } catch (Throwable ignored) {
            try {
                api = new PacketTitleImpl();
            } catch (Throwable ignored2) {
                api = new TitleAPI() {
                    @Override
                    public void send(Player player, Title title) {
                    }

                    @Override
                    public void reset(Player player) {
                    }
                };
            }
        }
    }

    static TitleAPI getApi() {
        return api;
    }
}
