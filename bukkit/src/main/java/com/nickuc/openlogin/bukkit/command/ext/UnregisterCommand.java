/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 - 2026 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.command.ext;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.command.BukkitCommand;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class UnregisterCommand extends BukkitCommand {

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
            sender.sendMessage(plugin.getLocaleManager().get(sender, Messages.MESSAGE_UNREGISTER));
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        String name = sender.getName();
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            sender.sendMessage(plugin.getLocaleManager().get(sender, Messages.NOT_REGISTERED));
            return;
        }

        Account account = accountOpt.get();
        String currentPassword = args[0];
        if (!accountManagement.comparePassword(account, currentPassword)) {
            sender.sendMessage(plugin.getLocaleManager().get(sender, Messages.INCORRECT_PASSWORD));
            return;
        }

        if (!accountManagement.delete(name)) {
            sender.sendMessage(plugin.getLocaleManager().get(sender, Messages.DATABASE_ERROR));
            return;
        }

        String kickMsg = plugin.getLocaleManager().get(sender, Messages.UNREGISTER_KICK);
        plugin.getFoliaLib().runAtEntity(sender, task -> sender.kickPlayer(kickMsg));
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
            String kickMsg = plugin.getLocaleManager().get(playerIfOnline, Messages.UNREGISTER_KICK);
            plugin.getFoliaLib().runAtEntity(playerIfOnline, task -> playerIfOnline.kickPlayer(kickMsg));
        }

        sender.sendMessage("§aSuccess!");
    }
}
