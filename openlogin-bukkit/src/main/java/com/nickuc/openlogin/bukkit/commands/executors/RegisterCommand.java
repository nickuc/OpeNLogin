/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.commands.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import com.nickuc.openlogin.bukkit.api.events.AsyncRegisterEvent;
import com.nickuc.openlogin.bukkit.commands.BukkitAbstractCommand;
import com.nickuc.openlogin.bukkit.reflection.packets.TitleAPI;
import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.encryption.BCrypt;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.settings.Settings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand extends BukkitAbstractCommand {

    public RegisterCommand(OpenLoginBukkit plugin) {
        super(plugin, "register");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
            return;
        }

        String name = sender.getName();
        LoginManagement loginManagement = plugin.getLoginManagement();
        if (loginManagement.isAuthenticated(name)) {
            sender.sendMessage(Messages.ALREADY_LOGIN.asString());
            return;
        }

        if (args.length != 2) {
            sender.sendMessage(Messages.MESSAGE_REGISTER.asString());
            return;
        }

        String password = args[0];
        int length = password.length();

        if (length <= Settings.PASSWORD_SMALL.asInt()) {
            sender.sendMessage(Messages.PASSWORD_TOO_SMALL.asString());
            return;
        }

        if (length >= Settings.PASSWORD_LARGE.asInt()) {
            sender.sendMessage(Messages.PASSWORD_TOO_LARGE.asString());
            return;
        }

        if (!password.equals(args[1])) {
            sender.sendMessage(Messages.PASSWORDS_DONT_MATCH.asString());
            return;
        }

        boolean exists = loginManagement.retrieveOrLoad(name).isPresent();
        if (exists) {
            sender.sendMessage(Messages.ALREADY_REGISTERED.asString());
            return;
        }

        Player player = (Player) sender;
        Database database = plugin.getDatabase();
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
        String address = player.getAddress().getAddress().getHostAddress();
        if (!Account.update(database, name, hashedPassword, address, false)) {
            sender.sendMessage(Messages.DATABASE_ERROR.asString());
            return;
        }

        AsyncRegisterEvent registerEvent = new AsyncRegisterEvent(player);
        if (registerEvent.callEvt()) {
            plugin.getLoginManagement().setAuthenticated(name);

            TitleAPI.sendTitle(player, Messages.TITLE_AFTER_REGISTER.asTitle());
            sender.sendMessage(Messages.SUCCESSFUL_REGISTER.asString());

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.setWalkSpeed(0.2F);
                player.setFlySpeed(0.2F);
            });

            new AsyncAuthenticateEvent(player).callEvt();
        }
    }
}
