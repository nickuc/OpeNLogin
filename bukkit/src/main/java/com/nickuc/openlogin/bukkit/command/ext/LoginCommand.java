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
import com.nickuc.openlogin.bukkit.adventure.ComponentSender;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import com.nickuc.openlogin.bukkit.api.events.AsyncLoginEvent;
import com.nickuc.openlogin.bukkit.command.BukkitCommand;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class LoginCommand extends BukkitCommand {

    public LoginCommand(OpenLoginBukkit plugin) {
        super(plugin, "login");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (!(sender instanceof Player)) {
            ComponentSender.send(sender, Messages.PLAYER_COMMAND_USAGE.asComponent());
            return;
        }

        String name = sender.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();
        if (loginManagement.isAuthenticated(name)) {
            ComponentSender.send(sender, Messages.ALREADY_LOGIN.asComponent());
            return;
        }

        if (args.length != 1) {
            ComponentSender.send(sender, Messages.MESSAGE_LOGIN.asComponent());
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            ComponentSender.send(sender, Messages.NOT_REGISTERED.asComponent());
            return;
        }

        Account account = accountOpt.get();
        String password = args[0];

        Player player = (Player) sender;
        if (!accountManagement.comparePassword(account, password)) {
            plugin.getFoliaLib().runAtEntity(player, task -> player.kickPlayer(ComponentSender.toKickString(Messages.INCORRECT_PASSWORD.asComponent())));
            return;
        }

        AsyncLoginEvent loginEvent = new AsyncLoginEvent(player);
        if (loginEvent.callEvt()) {
            plugin.getLoginManagement().setAuthenticated(name);

            ComponentSender.send(player, Messages.SUCCESSFUL_LOGIN.asComponent());
            ComponentSender.sendTitle(player, Messages.TITLE_AFTER_LOGIN.asTitle());

            plugin.getFoliaLib().runAtEntity(player, task -> {
                player.setWalkSpeed(0.2F);
                player.setFlySpeed(0.1F);
            });

            new AsyncAuthenticateEvent(player).callEvt();
        }
    }
}
