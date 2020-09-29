/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listeners;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.reflection.packets.TitleAPI;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.settings.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinListeners implements Listener {

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();

        boolean registered = loginManagement.retrieveOrLoad(name).isPresent();
        LoginQueue.addToQueue(name, registered);
        player.setWalkSpeed(0F);
        player.setFlySpeed(0F);

        if (registered) {
            player.sendMessage(Messages.MESSAGE_LOGIN.asString());
            TitleAPI.sendTitle(player, Messages.TITLE_BEFORE_LOGIN.asTitle());
        } else {
            player.sendMessage(Messages.MESSAGE_REGISTER.asString());
            TitleAPI.sendTitle(player, Messages.TITLE_BEFORE_REGISTER.asTitle());
        }
    }

}
