/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.commands.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.commands.BukkitAbstractCommand;
import com.nickuc.openlogin.bukkit.reflection.packets.ActionBarAPI;
import com.nickuc.openlogin.common.http.Http;
import com.nickuc.openlogin.common.settings.Messages;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class OpenLoginCommand extends BukkitAbstractCommand {

    private boolean confirmNLogin;

    public OpenLoginCommand(OpenLoginBukkit plugin) {
        super(plugin, "openlogin");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (args.length != 0) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "reload":
                case "rl":
                case "r":
                    plugin.reloadConfig();
                    plugin.setupSettings();
                    sender.sendMessage(Messages.PLUGIN_RELOAD_MESSAGE.asString());
                    return;

                case "update":
                    if (!plugin.isUpdateAvailable()) {
                        sender.sendMessage("§cYou are already using the latest version.");
                        return;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }
                    Player player = (Player) sender;
                    downloadActionbar(player, false);
                    return;

                case "nlogin":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }
                    player = (Player) sender;
                    if (!confirmNLogin) {
                        sender.sendMessage("");
                        sender.sendMessage(" §cnLogin §7is a §cproprietary §7authentication plugin,");
                        sender.sendMessage(" §7updated and maintained by §cnickuc.com§7. This means that you");
                        sender.sendMessage(" §7cannot view and modify the source code of the plugin.");
                        sender.sendMessage("");
                        sender.sendMessage(" §eIf you still have questions, please contact us:");
                        sender.sendMessage(" §bnickuc.com/discord");
                        sender.sendMessage("");
                        sender.sendMessage(" §7To proceed with the download, type §f'/openlogin nlogin' §7again.");
                        sender.sendMessage("");
                        confirmNLogin = true;
                    } else {
                        downloadActionbar(player, false);
                    }
                    return;
            }
        }

        sender.sendMessage("");
        sender.sendMessage(" §eThis server is running §fOpenLogin v " + plugin.getDescription().getVersion() + ".");
        sender.sendMessage(" §7Powered by §bwww.nickuc.com§7.");
        sender.sendMessage("");
        sender.sendMessage(" §7GitHub: §fhttps://github.com/nickuc/OpeNLogin");
        sender.sendMessage("");
    }

    private void downloadActionbar(Player player, boolean update) {
        player.sendMessage("§eDownloading...");
        ActionBarAPI.sendActionBar(player, "§eConnecting...");

        Http http = new Http(update ? "https://github.com/nickuc/OpeNLogin/releases/download/" + plugin.getLatestVersion() + "/OpeNLogin.jar" : "https://nickuc.com/repo/files/nLogin.jar");
        final int barsCount = 30;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (http.finished()) {
                    ActionBarAPI.sendActionBar(player, "§aDownload finished! §7(§a" + StringUtils.repeat("|", barsCount) + "§7)");
                    player.sendMessage("§aDownload finished. Please restart your server.");
                    cancel();
                    return;
                }
                int bars = (int) (barsCount * (http.downloaded() / http.contentLength()));
                String progressBar = "§a" + StringUtils.repeat("|", bars) + "§c" + StringUtils.repeat("|", barsCount - bars);
                ActionBarAPI.sendActionBar(player, "§eDownloading... §7(" + progressBar + "§7)");
            }
        }.runTaskTimer(plugin, 0, 4);
        try {
            File output = update ? new File(plugin.getDataFolder().getParentFile(), "OpeNLogin-" + plugin.getLatestVersion() + ".jar") : new File(plugin.getDataFolder().getParentFile(), "nLogin.jar");
            if (http.download(output)) {
                File pluginFile = getJarFile();
                pluginFile.deleteOnExit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            String msg = update ? "§cFailed to download new version. Update manually at: https://github.com/nickuc/OpeNLogin/releases" : "§cFailed to download nLogin :c. Download manually at: nickuc.com";
            plugin.sendMessage(msg);
            player.sendMessage(msg);
        }
    }

    public static File getJarFile() throws UnsupportedEncodingException {
        return new File(URLDecoder.decode(OpenLoginBukkit.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath(), "UTF-8"));
    }

}
