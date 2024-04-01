/*
 * The MIT License (MIT)
 *
 * A practical, secure and friendly authentication plugin.
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
