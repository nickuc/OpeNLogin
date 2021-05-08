/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
public class AsyncLoginEvent extends OpenLoginEvent implements Cancellable {

    private final Player player;
    @Setter
    private boolean cancelled;

    public AsyncLoginEvent(Player player) {
        super(true);
        this.player = player;
    }
}
