/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
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
        if (!(sender instanceof Player)) {
            if (args.length != 1) {
                sender.sendMessage("§cUsage: /" + lb + " <player>");
                return;
            }
            AccountManagement accountManagement = plugin.getAccountManagement();
            String name = args[0];
            Optional<Account> accountOpt = accountManagement.retrieveOrLoad(name);
            if (!accountOpt.isPresent()) {
                sender.sendMessage(Messages.NOT_REGISTERED.asString());
                return;
            }

            if (!accountManagement.delete(name)) {
                sender.sendMessage(Messages.DATABASE_ERROR.asString());
                return;
            }

            Player player = plugin.getServer().getPlayer(name);
            if (player != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> player.kickPlayer(Messages.UNREGISTER_KICK.asString()));
            }
            sender.sendMessage("§aSuccess!");
            return;
        }

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

        Player player = (Player) sender;
        plugin.getServer().getScheduler().runTask(plugin, () -> player.kickPlayer(Messages.UNREGISTER_KICK.asString()));
    }
}
