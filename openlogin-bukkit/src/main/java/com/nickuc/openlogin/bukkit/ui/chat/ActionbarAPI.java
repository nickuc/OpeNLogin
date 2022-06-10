/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.chat;

import org.bukkit.entity.Player;

public interface ActionbarAPI {

    static ActionbarAPI getApi() {
        return ActionbarAPIHolder.getApi();
    }

    void send(Player player, String message);

    default void reset(Player player) {
        send(player, "");
    }

}
