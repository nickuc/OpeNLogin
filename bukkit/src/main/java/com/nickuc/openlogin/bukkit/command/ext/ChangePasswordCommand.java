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
import com.nickuc.openlogin.bukkit.command.BukkitCommand;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.settings.Settings;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

public class ChangePasswordCommand extends BukkitCommand {

    public ChangePasswordCommand(OpenLoginBukkit plugin) {
        super(plugin, true, "changepassword");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (sender instanceof Player) {
            performPlayer((Player) sender, lb, args);
        } else {
            performConsole(sender, lb, args);
        }
    }

    private void performPlayer(Player sender, String lb, String[] args) {
        if (args.length != 2) {
            ComponentSender.send(sender, Messages.MESSAGE_CHANGEPASSWORD.asComponent());
            return;
        }

        String currentPassword = args[0];
        String newPassword = args[1];
        int passwordLength = newPassword.length();

        if (passwordLength <= Settings.PASSWORD_SMALL.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_SMALL.asComponent());
            return;
        }

        if (passwordLength >= Settings.PASSWORD_LARGE.asInt()) {
            ComponentSender.send(sender, Messages.PASSWORD_TOO_LARGE.asComponent());
            return;
        }

        if (currentPassword.equals(newPassword)) {
            ComponentSender.send(sender, Messages.PASSWORD_SAME_AS_OLD.asComponent());
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        String name = sender.getName();
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            ComponentSender.send(sender, Messages.NOT_REGISTERED.asComponent());
            return;
        }

        Account account = accountOpt.get();
        if (!accountManagement.comparePassword(account, currentPassword)) {
            ComponentSender.send(sender, Messages.PASSWORDS_DONT_MATCH.asComponent());
            return;
        }

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(newPassword, salt);
        String address = Objects.requireNonNull(sender.getAddress()).getAddress().getHostAddress();
        if (!accountManagement.update(name, hashedPassword, address)) {
            ComponentSender.send(sender, Messages.DATABASE_ERROR.asComponent());
            return;
        }

        ComponentSender.send(sender, Messages.PASSWORD_CHANGED.asComponent());
    }

    private void performConsole(CommandSender sender, String lb, String[] args) {
        if (!sender.hasPermission("openlogin.admin")) {
            ComponentSender.send(sender, Messages.INSUFFICIENT_PERMISSIONS.asComponent(Placeholder.unparsed("permission", "openlogin.admin")));
            return;
        }

        if (args.length != 2) {
            ComponentSender.send(sender, "<#E0575B>Usage: /" + lb + " [player] [new password]");
            return;
        }

        String playerName = args[0];
        String newPassword = args[1];
        int passwordLength = newPassword.length();

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
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(playerName);
        if (!accountOpt.isPresent()) {
            ComponentSender.send(sender, Messages.NOT_REGISTERED.asComponent());
            return;
        }

        Account account = accountOpt.get();
        if (accountManagement.comparePassword(account, newPassword)) {
            ComponentSender.send(sender, Messages.PASSWORD_SAME_AS_OLD.asComponent());
            return;
        }

        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(newPassword, salt);
        String address = playerIfOnline != null ?
                Objects.requireNonNull(playerIfOnline.getAddress()).getAddress().getHostAddress() : null;
        if (!accountManagement.update(playerName, hashedPassword, address)) {
            ComponentSender.send(sender, Messages.DATABASE_ERROR.asComponent());
            return;
        }

        ComponentSender.send(sender, Messages.PASSWORD_CHANGED.asComponent());

        if (playerIfOnline != null) {
            ComponentSender.send(playerIfOnline, Messages.PASSWORD_CHANGED.asComponent());
        }
    }
}
