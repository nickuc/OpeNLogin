/*
 * The MIT License (MIT)
 *
 * Copyright © 2024 - OpenLogin Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.nickuc.openlogin.bukkit.command;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.command.executors.*;
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
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
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
