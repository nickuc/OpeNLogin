/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.command.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.command.BukkitAbstractCommand;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class UnregisterCommand extends BukkitAbstractCommand {

    public UnregisterCommand(OpenLoginBukkit plugin) {
        super(plugin, true, "unregister");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (sender instanceof Player) {
            performPlayer((Player) sender, lb, args);
        } else {
            performConsole(sender, lb, args);
        }
    }

    private void performPlayer(Player sender, String lb, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Messages.MESSAGE_UNREGISTER.asString());
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        String name = sender.getName();
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            sender.sendMessage(Messages.NOT_REGISTERED.asString());
            return;
        }

        Account account = accountOpt.get();
        String currentPassword = args[0];
        if (!accountManagement.comparePassword(account, currentPassword)) {
            sender.sendMessage(Messages.INCORRECT_PASSWORD.asString());
            return;
        }

        if (!accountManagement.delete(name)) {
            sender.sendMessage(Messages.DATABASE_ERROR.asString());
            return;
        }

        plugin.getFoliaLib().runAtEntity(sender, task -> sender.kickPlayer(Messages.UNREGISTER_KICK.asString()));
    }

    private void performConsole(CommandSender sender, String lb, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /" + lb + " <player>");
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        String playerName = args[0];

        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(playerName);
        if (!accountOpt.isPresent()) {
            sender.sendMessage(Messages.NOT_REGISTERED.asString());
            return;
        }

        if (!accountManagement.delete(playerName)) {
            sender.sendMessage(Messages.DATABASE_ERROR.asString());
            return;
        }

        Player playerIfOnline = plugin.getServer().getPlayer(playerName);
        if (playerIfOnline != null) {
            plugin.getFoliaLib().runAtEntity(playerIfOnline, task -> playerIfOnline.kickPlayer(Messages.UNREGISTER_KICK.asString()));
        }

        sender.sendMessage("§aSuccess!");
    }
}
