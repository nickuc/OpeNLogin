/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listeners;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class PlayerAuthenticateListener implements Listener {

    private final OpenLoginBukkit plugin;
    private boolean welcomeMessage;

    @EventHandler
    public void onAsyncAuthenticate(AsyncAuthenticateEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("openlogin.admin")) {
            if (welcomeMessage) {
                player.sendMessage("");
                player.sendMessage(" §eHello, " + player.getName() + "! Welcome to OpeNLogin!");
                player.sendMessage("");
                player.sendMessage(" §7Documentation:");
                player.sendMessage(" §bhttps://github.com/nickuc/OpeNLogin/tree/master/docs");
                player.sendMessage("");
                player.sendMessage(" §7If you need help, fell free to contact our support:");
                player.sendMessage(" §bhttps://nickuc.com/discord");
                player.sendMessage("");
                welcomeMessage = false;
            } else if (plugin.isUpdateAvailable()) {
                player.sendMessage("");
                player.sendMessage(" §7A new version of §aOpeNLogin §7is available §a(v" + plugin.getDescription().getVersion() + " -> " + plugin.getLatestVersion() + ")§7.");
                player.sendMessage(" §7Use the command §f'/openlogin update' §7to download new version.");
                player.sendMessage("");
            }
        }
    }
}
