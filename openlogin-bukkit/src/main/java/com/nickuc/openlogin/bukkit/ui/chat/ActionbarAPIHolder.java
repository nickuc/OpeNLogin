/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.chat;

import com.nickuc.openlogin.bukkit.ui.chat.impl.PacketActionbarImpl;
import com.nickuc.openlogin.bukkit.ui.chat.impl.SpigotActionbarImpl;

public class ActionbarAPIHolder {

    private static ActionbarAPI api;

    static {
        try {
            api = new SpigotActionbarImpl();
        } catch (Throwable ignored) {
            try {
                api = new PacketActionbarImpl();
            } catch (Throwable ignored2) {
                api = (player, message) -> {
                };
            }
        }
    }

    static ActionbarAPI getApi() {
        return api;
    }
}
