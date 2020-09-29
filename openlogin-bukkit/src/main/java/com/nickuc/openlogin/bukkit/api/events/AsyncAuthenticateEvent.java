/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;

public class AsyncAuthenticateEvent extends OpenLoginEvent {

    @Getter private final Player player;

    public AsyncAuthenticateEvent(Player player) {
        super(true);
        this.player = player;
    }
}
