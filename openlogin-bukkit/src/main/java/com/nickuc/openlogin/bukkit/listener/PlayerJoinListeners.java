/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listener;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.bukkit.util.TextComponentMessage;
import com.nickuc.openlogin.common.model.Title;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.util.ClassUtils;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class PlayerJoinListeners implements Listener {

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String name = player.getName();

        if (plugin.isNewUser()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }

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
                        player.sendMessage("  §6(proprietary)      §b(open source)");
                        player.sendMessage("");
                        player.sendMessage(" §7To use nLogin, type: §f'/openlogin nlogin'");
                        player.sendMessage(" §7To use OpeNLogin, type: §f'/openlogin setup'");
                    }
                    player.sendMessage("");

                    TitleAPI.getApi().send(player,
                            new Title("", "§ePlease answer the question sent in the chat.", 0, 9999, 10));
                }
            }.runTaskLater(plugin, 30);

            e.setJoinMessage("");
            return;
        }

        boolean registered = plugin.getAccountManagement().retrieveOrLoad(name).isPresent();
        LoginQueue.addToQueue(name, registered);

        player.setWalkSpeed(0F);
        player.setFlySpeed(0F);

        if (registered) {
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> {
                 player.sendMessage(Messages.MESSAGE_LOGIN.asString());
                 TitleAPI.getApi().send(player, Messages.TITLE_BEFORE_LOGIN.asTitle());
            }, 10); // run after 10 ticks = 0.5s
        } else {
            player.sendMessage(Messages.MESSAGE_REGISTER.asString());
            TitleAPI.getApi().send(player, Messages.TITLE_BEFORE_REGISTER.asTitle());
        }
    }

}
