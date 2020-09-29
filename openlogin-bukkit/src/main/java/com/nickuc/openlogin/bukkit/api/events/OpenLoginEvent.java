/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.api.events;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OpenLoginEvent extends Event {

    @Getter private static final HandlerList handlerList = new HandlerList();

    public OpenLoginEvent() {
    }

    public OpenLoginEvent(boolean async) {
        super(async);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public boolean callEvt() {
        Bukkit.getServer().getPluginManager().callEvent(this);
        return !(this instanceof Cancellable) || !((Cancellable) this).isCancelled();
    }

}
