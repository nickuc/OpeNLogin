/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.commands;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.commands.executors.*;
import com.nickuc.openlogin.common.security.filter.LoggerFilterManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class CommandManagement {

    private static final Set<String> ALLOWED_COMMANDS = new HashSet<>();

    private final OpenLoginBukkit plugin;

    /**
     * Checks if the provided command is allowed.
     *
     * @param command the command to check
     * @return true if is allowed
     */
    public boolean isAllowedCommand(@NonNull String command) {
        return ALLOWED_COMMANDS.contains(command.toLowerCase());
    }

    public void register() {
        for (Commands command : Commands.values()) {
            try {
                PluginCommand pluginCommand = plugin.getCommand(command.name);
                if (pluginCommand == null) {
                    throw new RuntimeException("Missing command '" + command.name + "'");
                }

                ALLOWED_COMMANDS.add("/" + command.name);
                LoggerFilterManager.addOpenLoginCommand("/" + command.name);
                for (String alias : pluginCommand.getAliases()) {
                    ALLOWED_COMMANDS.add("/" + alias.toLowerCase());
                    LoggerFilterManager.addOpenLoginCommand("/" + alias.toLowerCase());
                }

                Constructor<?> constructor = command.clasz.getConstructor(OpenLoginBukkit.class);
                BukkitAbstractCommand bukkitCommand = (BukkitAbstractCommand) constructor.newInstance(plugin);
                pluginCommand.setExecutor(bukkitCommand);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor
    public enum Commands {

        CHANGE_PASSWORD("changepassword", ChangePasswordCommand.class),
        LOGIN("login", LoginCommand.class),
        REGISTER("register", RegisterCommand.class),
        OPENLOGIN("openlogin", OpenLoginCommand.class),
        UNREGISTER("unregister", UnregisterCommand.class);

        private final String name;
        private final Class<?> clasz;

    }

}
