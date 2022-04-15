/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.command.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import com.nickuc.openlogin.bukkit.api.events.AsyncLoginEvent;
import com.nickuc.openlogin.bukkit.command.BukkitAbstractCommand;
import com.nickuc.openlogin.bukkit.reflection.packets.TitleAPI;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class LoginCommand extends BukkitAbstractCommand {

    public LoginCommand(OpenLoginBukkit plugin) {
        super(plugin, "login");
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

        if (args.length != 1) {
            sender.sendMessage(Messages.MESSAGE_LOGIN.asString());
            return;
        }

        AccountManagement accountManagement = plugin.getAccountManagement();
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
        if (!accountOpt.isPresent()) {
            sender.sendMessage(Messages.NOT_REGISTERED.asString());
            return;
        }

        Account account = accountOpt.get();
        String password = args[0];

        Player player = (Player) sender;
        if (!accountManagement.comparePassword(account, password)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> player.kickPlayer(Messages.INCORRECT_PASSWORD.asString()));
            return;
        }

        AsyncLoginEvent loginEvent = new AsyncLoginEvent(player);
        if (loginEvent.callEvt()) {
            plugin.getLoginManagement().setAuthenticated(name);

            player.sendMessage(Messages.SUCCESSFUL_LOGIN.asString());
            TitleAPI.sendTitle(player, Messages.TITLE_AFTER_LOGIN.asTitle());

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.setWalkSpeed(0.2F);
                player.setFlySpeed(0.1F);
            });

            new AsyncAuthenticateEvent(player).callEvt();
        }
    }
}
