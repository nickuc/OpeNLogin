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
import com.tcoded.folialib.FoliaLib;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class UnregisterCommand extends BukkitAbstractCommand {
    final FoliaLib foliaLib = OpenLoginBukkit.getFoliaLib();

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

        foliaLib.getImpl().runAtEntity(sender, wrappedTask -> sender.kickPlayer(Messages.UNREGISTER_KICK.asString()));
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
            foliaLib.getImpl().runAtEntity(playerIfOnline, wrappedTask -> playerIfOnline.kickPlayer(Messages.UNREGISTER_KICK.asString()));
        }

        sender.sendMessage("§aSuccess!");
    }
}
