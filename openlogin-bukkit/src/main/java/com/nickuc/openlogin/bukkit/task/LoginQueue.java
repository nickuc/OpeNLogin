/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.task;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.settings.Settings;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginQueue {

    private static final ConcurrentHashMap<String, PlayerLogin> pendingLogin = new ConcurrentHashMap<>();

    /**
     * Starts the global timeout task.
     *
     * @param plugin the plugin instance
     */
    public static void startTask(OpenLoginBukkit plugin) {
        final Server server = plugin.getServer();
        plugin.getFoliaLib().runTimerAsync(() -> {
            if (pendingLogin.isEmpty()) return;

            for (Map.Entry<String, PlayerLogin> entry : pendingLogin.entrySet()) {
                String name = entry.getKey();
                Player player = server.getPlayer(name);
                if (player == null || plugin.getLoginManagement().isAuthenticated(name)) {
                    pendingLogin.remove(name);
                    return;
                }

                PlayerLogin playerLogin = entry.getValue();
                int seconds = playerLogin.seconds;
                if (seconds >= Settings.TIME_TO_LOGIN.asInt()) {
                    plugin.getFoliaLib().runAtEntity(player, task -> player.kickPlayer(playerLogin.registered ? Messages.DELAY_KICK_LOGIN.asString() : Messages.DELAY_KICK_REGISTER.asString()));
                    pendingLogin.remove(name);
                    return;
                }
                playerLogin.addSecond();
            }
        }, 0, 20);
    }

    /**
     * Add a player to timeout timer.
     *
     * @param name       the name of the player
     * @param registered should be true if the player is registered
     */
    public static void addToQueue(@NonNull String name, boolean registered) {
        pendingLogin.put(name, new PlayerLogin(registered));
    }

    /**
     * Removes a player from timeout timer.
     *
     * @param name the name of the player
     */
    public static void removeFromQueue(@NonNull String name) {
        pendingLogin.remove(name);
    }

    // Player login class
    @RequiredArgsConstructor
    private static class PlayerLogin {

        private final boolean registered;
        private int seconds;

        public void addSecond() {
            seconds++;
        }

    }
}
