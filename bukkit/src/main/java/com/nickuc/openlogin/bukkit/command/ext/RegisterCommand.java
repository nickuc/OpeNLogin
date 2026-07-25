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
import com.nickuc.openlogin.bukkit.api.events.AsyncRegisterEvent;
import com.nickuc.openlogin.bukkit.command.BukkitCommand;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.settings.Settings;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class RegisterCommand extends BukkitCommand {

    public RegisterCommand(OpenLoginBukkit plugin) {
        super(plugin, "register");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (sender instanceof Player) {
            performPlayer((Player) sender, lb, args);
        } else {
            performConsole(sender, lb, args);
        }
    }

    private void performPlayer(Player sender, String lb, String[] args) {
        String name = sender.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();
        if (loginManagement.isAuthenticated(name)) {
            ComponentSender.send(sender, Messages.ALREADY_LOGIN.asComponent());
            return;
        }

        if (args.length != 2) {
            ComponentSender.send(sender, Messages.MESSAGE_REGISTER.asComponent());
            return;
        }

        String password = args[0];
        int passwordLength = password.length();

        if (passwordLength <= Settings.PASSWORD_SMALL.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_SMALL.asComponent());
            return;
        }

        if (passwordLength >= Settings.PASSWORD_LARGE.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_LARGE.asComponent());
            return;
        }

        if (!password.equals(args[1])) {
            ComponentSender.send(sender, Messages.PASSWORDS_DONT_MATCH.asComponent());
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        boolean exists = accountManagement.retrieveOrLoad(name).isPresent();
        if (exists) {
            ComponentSender.send(sender, Messages.ALREADY_REGISTERED.asComponent());
            return;
        }

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
        String address = sender.getAddress().getAddress().getHostAddress();
        if (!accountManagement.update(name, hashedPassword, address, false)) {
            ComponentSender.send(sender, Messages.DATABASE_ERROR.asComponent());
            return;
        }

        AsyncRegisterEvent registerEvent = new AsyncRegisterEvent(sender);
        if (registerEvent.callEvt()) {
            plugin.getLoginManagement().setAuthenticated(name);

            ComponentSender.sendTitle(sender, Messages.TITLE_AFTER_REGISTER.asTitle());
            ComponentSender.send(sender, Messages.SUCCESSFUL_REGISTER.asComponent());

            plugin.getFoliaLib().runAtEntity(sender, task -> {
                sender.setWalkSpeed(0.2F);
                sender.setFlySpeed(0.1F);
            });

            new AsyncAuthenticateEvent(sender).callEvt();
        }
    }

    private void performConsole(CommandSender sender, String lb, String[] args) {
        if (!sender.hasPermission("openlogin.admin")) {
            ComponentSender.send(sender, Messages.INSUFFICIENT_PERMISSIONS.asComponent(Placeholder.unparsed("permission", "openlogin.admin")));
            return;
        }

        if (args.length != 2) {
            ComponentSender.send(sender, "<#E0575B>Usage: /" + lb + " [player] [password]");
            return;
        }

        String playerName = args[0];
        String password = args[1];
        int passwordLength = password.length();

        if (passwordLength <= Settings.PASSWORD_SMALL.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_SMALL.asComponent());
            return;
        }

        if (passwordLength >= Settings.PASSWORD_LARGE.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_LARGE.asComponent());
            return;
        }

        Player playerIfOnline = plugin.getServer().getPlayerExact(playerName);
        if (playerIfOnline != null) {
            playerName = playerIfOnline.getName();
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        boolean exists = accountManagement.retrieveOrLoad(playerName).isPresent();
        if (exists) {
            ComponentSender.send(sender, Messages.ALREADY_REGISTERED.asComponent());
            return;
        }

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
        String address = playerIfOnline != null ?
                Objects.requireNonNull(playerIfOnline.getAddress()).getAddress().getHostAddress() : null;
        if (!accountManagement.update(playerName, hashedPassword, address, false)) {
            ComponentSender.send(sender, Messages.DATABASE_ERROR.asComponent());
            return;
        }

        ComponentSender.send(sender, Messages.SUCCESSFUL_REGISTER.asComponent());

        if (playerIfOnline != null) {
            AsyncRegisterEvent registerEvent = new AsyncRegisterEvent(playerIfOnline);
            if (registerEvent.callEvt()) {
                plugin.getLoginManagement().setAuthenticated(playerName);

                ComponentSender.sendTitle(playerIfOnline, Messages.TITLE_AFTER_REGISTER.asTitle());
                ComponentSender.send(playerIfOnline, Messages.SUCCESSFUL_REGISTER.asComponent());

                plugin.getFoliaLib().runAtEntity(playerIfOnline, task -> {
                    playerIfOnline.setWalkSpeed(0.2F);
                    playerIfOnline.setFlySpeed(0.1F);
                });

                new AsyncAuthenticateEvent(playerIfOnline).callEvt();
            }
        }
    }
}
