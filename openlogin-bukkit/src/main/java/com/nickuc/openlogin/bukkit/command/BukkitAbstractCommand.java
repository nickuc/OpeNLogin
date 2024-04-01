/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.command;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.command.executors.OpenLoginCommand;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.settings.Messages;
import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BukkitAbstractCommand implements CommandExecutor {

    protected final OpenLoginBukkit plugin;
    private final boolean requireAuth;
    private final String permission;

    public BukkitAbstractCommand(OpenLoginBukkit plugin, @NonNull String command) {
        this(plugin, false, command);
    }

    public BukkitAbstractCommand(OpenLoginBukkit plugin, boolean requireAuth, @NonNull String command) {
        this.plugin = plugin;
        this.requireAuth = requireAuth;
        this.permission = "openlogin.command." + command.toLowerCase();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {
        final String name = sender.getName();
        final LoginManagement loginManagement = plugin.getLoginManagement();

        if (requireAuth && sender instanceof Player && !loginManagement.isAuthenticated(name)) {
            return true;
        }

        if (plugin.isNewUser()) {
            if (!(this instanceof OpenLoginCommand)) {
                return true;
            }
        } else if (!sender.hasPermission(permission)) {
            sender.sendMessage(Messages.INSUFFICIENT_PERMISSIONS.asString());
            return true;
        }

        if (loginManagement.isUnlocked(name)) {
            plugin.getFoliaLib().runAsync(task -> {
                try {
                    perform(sender, lb, args);
                } catch (Exception e) {
                    e.printStackTrace();
                    plugin.sendMessage("§cFailed to perform the command '" + lb + "', sender: " + sender.getName());
                }
            });
        }
        return true;
    }

    protected abstract void perform(CommandSender sender, String lb, String[] args);
}
