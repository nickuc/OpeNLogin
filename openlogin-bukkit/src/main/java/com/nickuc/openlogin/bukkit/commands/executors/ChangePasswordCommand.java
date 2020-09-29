/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.commands.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.commands.BukkitAbstractCommand;
import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.encryption.BCrypt;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ChangePasswordCommand extends BukkitAbstractCommand {

    public ChangePasswordCommand(OpenLoginBukkit plugin) {
        super(plugin, true, "changepassword");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(Messages.MESSAGE_CHANGEPASSWORD.asString());
            return;
        }

        String currentPassword = args[0];
        String newPassword = args[1];
        if (currentPassword.equals(newPassword)) {
            sender.sendMessage(Messages.PASSWORD_SAME_AS_OLD.asString());
            return;
        }

        String name = sender.getName();
        Optional<Account> accountOpt = plugin.getLoginManagement().retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            sender.sendMessage(Messages.NOT_REGISTERED.asString());
            return;
        }

        Account account = accountOpt.get();
        if (!account.comparePassword(currentPassword)) {
            sender.sendMessage(Messages.PASSWORDS_DONT_MATCH.asString());
            return;
        }

        Player player = (Player) sender;
        Database database = plugin.getDatabase();
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(newPassword, salt);
        String address = player.getAddress().getAddress().getHostAddress();
        if (!Account.update(database, name, hashedPassword, address)) {
            sender.sendMessage(Messages.DATABASE_ERROR.asString());
            return;
        }

        sender.sendMessage(Messages.PASSWORD_CHANGED.asString());
     }
}
