/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listener;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.reflection.packets.TitleAPI;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.util.TextComponentMessage;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.util.ClassUtils;
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

        if (plugin.isNewUser()) {
            player.sendMessage("");
            player.sendMessage(" §eHello, " + player.getName() + "!");
            player.sendMessage("");
            player.sendMessage("  §7Before we start, please select");
            player.sendMessage("  §7your favorite login plugin.");
            player.sendMessage("");
            if (ClassUtils.exists("net.md_5.bungee.api.chat.TextComponent")) {
                TextComponentMessage.sendPluginChoice(player);
            } else {
                player.sendMessage("      §enLogin              §eOpeNLogin");
                player.sendMessage("  §c(proprietary)      §a(open source)");
                player.sendMessage("");
                player.sendMessage(" §7To use nLogin, type: §f'/openlogin nlogin'");
                player.sendMessage(" §7To use OpeNLogin, type: §f'/openlogin setup'");
            }
            player.sendMessage("");
            e.setJoinMessage("");
            return;
        }

        boolean registered = plugin.getAccountManagement().retrieveOrLoad(name).isPresent();
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
