/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.listeners;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;

public class PlayerAuthenticateListener implements Listener {

    private final OpenLoginBukkit plugin;
    private boolean newUser, notNlogin;

    public PlayerAuthenticateListener(OpenLoginBukkit plugin, boolean newUser) {
        this.plugin = plugin;
        this.newUser = newUser;
    }

    @EventHandler
    public void onAsyncAuthenticate(AsyncAuthenticateEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("openlogin.admin")) {
            if (new File(plugin.getDataFolder().getParentFile(), "nLogin").exists() || notNlogin) {
                sendNLoginWarning(player);
            } else if (newUser) {
                player.sendMessage("");
                player.sendMessage(" §eHello, " + player.getName() + "! Welcome to OpeNLogin!");
                player.sendMessage("");
                player.sendMessage(" §7Documentation:");
                player.sendMessage(" §bhttps://github.com/nickuc/OpeNLogin/tree/master/docs");
                player.sendMessage("");
                player.sendMessage(" §7If you need help, fell free to contact our support:");
                player.sendMessage(" §bhttps://nickuc.com/discord");
                player.sendMessage("");
                notNlogin = true;
                newUser = false;
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) sendNLoginWarning(player);
                }, 20*5); // run after 5 seconds
            } else if (plugin.isUpdateAvailable()) {
                player.sendMessage("");
                player.sendMessage(" §7A new version of §aOpeNLogin §7is available §a(v" + plugin.getDescription().getVersion() + " -> " + plugin.getLatestVersion() + ")§7.");
                player.sendMessage(" §7Use the command §f'/openlogin update' §7to download new version.");
                player.sendMessage("");
            }
        }
    }

    private void sendNLoginWarning(Player player) {
        player.sendMessage("");
        player.sendMessage(" §e§lIMPORTANT NOTICE!");
        player.sendMessage("");
        player.sendMessage(" §aOpeNLogin §7is not the same plugin as §anLogin§7!");
        player.sendMessage("");
        player.sendMessage(" §7To download §anLogin§7, perform the follow command:");
        player.sendMessage(" §b/openlogin nlogin");
        player.sendMessage("");
        player.sendMessage(" §7For more information:");
        player.sendMessage(" §bgithub.com/nickuc/OpeNLogin/blob/master/docs/nlogin.md");
        player.sendMessage("");
        notNlogin = false;
    }

}
