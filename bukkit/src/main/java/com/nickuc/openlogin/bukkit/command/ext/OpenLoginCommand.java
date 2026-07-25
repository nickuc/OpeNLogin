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
import com.nickuc.openlogin.common.http.HttpClient;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.util.FileUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenLoginCommand extends BukkitCommand {

    private final AtomicBoolean
            downloadLock = new AtomicBoolean(),
            confirmNLogin = new AtomicBoolean(),
            confirmOpenLogin = new AtomicBoolean();

    public OpenLoginCommand(OpenLoginBukkit plugin) {
        super(plugin, "openlogin");
    }

    protected void perform(CommandSender sender, String lb, String[] args) {
        if (args.length != 0) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "reload":
                case "rl":
                case "r": {
                    if (sender instanceof Player && !plugin.getLoginManagement().isAuthenticated(sender.getName())) {
                        return;
                    }

                    plugin.reloadConfig();
                    plugin.setupSettings();
                    ComponentSender.send(sender, Messages.PLUGIN_RELOAD_MESSAGE.asComponent());
                    return;
                }

                case "update": {
                    if (!(sender instanceof Player)) {
                        ComponentSender.send(sender, Messages.PLAYER_COMMAND_USAGE.asComponent());
                        return;
                    }

                    Player player = (Player) sender;
                    String name = player.getName();
                    if (!plugin.getLoginManagement().isAuthenticated(name)) {
                        return;
                    }

                    if (!plugin.isUpdateAvailable()) {
                        ComponentSender.send(sender, "<#E0575B>You are already using the latest version.");
                        return;
                    }

                    if (downloadLock.getAndSet(true)) {
                        ComponentSender.send(sender, "<#E0575B>Download in progress...");
                    } else if (!update(player)) {
                        downloadLock.set(false);
                    }
                    return;
                }

                case "setup": {
                    if (!(sender instanceof Player)) {
                        ComponentSender.send(sender, Messages.PLAYER_COMMAND_USAGE.asComponent());
                        return;
                    }

                    if (!plugin.isNewUser()) {
                        return;
                    }

                    if (!confirmOpenLogin.getAndSet(true)) {
                        ComponentSender.send(sender, "");
                        ComponentSender.send(sender, " <#E0575B>nLogin is generally a better solution for most users.");
                        ComponentSender.send(sender, " <#A0A0B0>If you want to install <white>OpeNLogin <#A0A0B0>anyway,");
                        ComponentSender.send(sender, " <#A0A0B0>please click on the message again.");
                        ComponentSender.send(sender, "");
                        return;
                    }

                    for (Player on : plugin.getServer().getOnlinePlayers()) {
                        plugin.getFoliaLib().runAtEntity(on, task -> on.kickPlayer(ComponentSender.toKickString("<#5EC26A>Please rejoin to complete the plugin installation.")));
                    }

                    plugin.setNewUser(false);
                    plugin.getPluginSettings().set("setup_date", Long.toString(System.currentTimeMillis()));

                    File newUserfile = new File(plugin.getDataFolder(), "new-user");
                    if (newUserfile.exists() && !newUserfile.delete()) {
                        newUserfile.deleteOnExit();
                    }
                    return;
                }

                case "nlogin": {
                    if (!(sender instanceof Player)) {
                        ComponentSender.send(sender, Messages.PLAYER_COMMAND_USAGE.asComponent());
                        return;
                    }

                    Player player = (Player) sender;
                    String name = player.getName();
                    if (!plugin.isNewUser()) {
                        if (!plugin.getLoginManagement().isAuthenticated(name)) return;

                        if (!sender.hasPermission("openlogin.admin")) {
                            ComponentSender.send(sender, Messages.INSUFFICIENT_PERMISSIONS.asComponent(Placeholder.unparsed("permission", "openlogin.admin")));
                            return;
                        }
                    }

                    if (downloadLock.get()) {
                        ComponentSender.send(sender, "<#E0575B>Download in progress...");
                        return;
                    }

                    boolean skip = args.length == 2 && args[1].equalsIgnoreCase("skip");
                    if (!skip && !confirmNLogin.getAndSet(true)) {
                        ComponentSender.send(sender, "");
                        ComponentSender.send(sender, " <#E8A33D>nLogin <#A0A0B0>is a <#E8A33D>proprietary <#A0A0B0>authentication plugin,");
                        ComponentSender.send(sender, " <#A0A0B0>updated and maintained by <#E0575B>nickuc.com<#A0A0B0>. This means that you");
                        ComponentSender.send(sender, " <#A0A0B0>cannot view and modify the source code of the plugin.");
                        ComponentSender.send(sender, "");
                        ComponentSender.send(sender, " <#F2C14E>If you still have questions, please contact us:");
                        ComponentSender.send(sender, " <#56CCF2>nickuc.com/discord");
                        ComponentSender.send(sender, "");
                        ComponentSender.send(sender, " <#A0A0B0>To proceed with the download, type <#56CCF2>/openlogin nlogin <#A0A0B0>again.");
                        ComponentSender.send(sender, "");
                    } else {
                        if (downloadLock.getAndSet(true)) {
                            ComponentSender.send(sender, "<#E0575B>Download already in progress!");
                            return;
                        }

                        Runnable callback = null;
                        if (skip && plugin.isNewUser()) {
                            callback = () -> {
                                for (Player on : plugin.getServer().getOnlinePlayers()) {
                                    plugin.getFoliaLib().runAtEntity(on, task -> {
                                        on.closeInventory();
                                        on.kickPlayer(ComponentSender.toKickString("<#5EC26A>nLogin was successfully installed. We are restarting the server to apply the changes."));
                                    });
                                }
                                plugin.getServer().shutdown();
                            };
                            ComponentSender.resetTitle(player);
                        }
                        if (!downloadNLogin(player, callback)) {
                            downloadLock.set(false);
                        }
                    }
                    return;
                }
            }
        }

        ComponentSender.send(sender, "");
        ComponentSender.send(sender, " <#F2C14E>This server is running <white>OpenLogin v " + plugin.getDescription().getVersion() + ".");
        ComponentSender.send(sender, " <#A0A0B0>Powered by <#56CCF2>www.nickuc.com<#A0A0B0>.");
        ComponentSender.send(sender, "");
        ComponentSender.send(sender, " <#A0A0B0>GitHub: <white>https://github.com/nickuc-com/OpeNLogin");
        ComponentSender.send(sender, "");
    }

    private boolean update(Player player) {
        File output = new File(plugin.getDataFolder().getParentFile(), "OpenLogin-" + plugin.getLatestVersion() + ".jar");
        return downloadActionbar(player, "https://github.com/nickuc-com/OpeNLogin/releases/download/" + plugin.getLatestVersion() + "/OpenLogin.jar", output, true, null);
    }

    private boolean downloadNLogin(Player player, Runnable callback) {
        File output = new File(plugin.getDataFolder().getParentFile(), "nLogin.jar");
        return downloadActionbar(player, "https://repo.nickuc.com/files/latest/nLogin.jar", output, false, callback);
    }

    private boolean downloadActionbar(Player player, String url, File output, boolean update, Runnable callback) {
        ComponentSender.send(player, "<#F2C14E>Downloading...");
        ComponentSender.sendActionbar(player, "<#F2C14E>Connecting...");

        final int barsCount = 40;
        final HttpClient.AsyncDownloadResult downloadResult;
        try {
            if ((downloadResult = HttpClient.DEFAULT.download(url, output)) == null) {
                ComponentSender.sendActionbar(player, "<#E0575B>Download failed!");
                ComponentSender.send(player, "<#E0575B>Download failed, could not delete old file.");
                return false;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        AtomicBoolean downloadFinished = new AtomicBoolean();
        AtomicBoolean downloadSuccessful = new AtomicBoolean();
        plugin.getFoliaLib().runAtEntityTimer(player, task -> {
            if (downloadFinished.get()) {
                if (downloadSuccessful.get()) {
                    ComponentSender.sendActionbar(player, "<#5EC26A>Download finished! <#A0A0B0>(<#5EC26A>" + repeatString("|", barsCount) + "<#A0A0B0>)");
                    ComponentSender.send(player, "<#5EC26A>Download finished. Please restart your server.");
                    if (callback != null) {
                        callback.run();
                    }
                } else {
                    ComponentSender.sendActionbar(player, "<#E0575B>Download failed! <#A0A0B0>(<#5EC26A>" + repeatString("|", barsCount) + "<#A0A0B0>)");
                    ComponentSender.send(player, "<#E0575B>Download failed, please try again.");
                }
                task.cancel();
                return;
            }
            int bars = (int) (barsCount * (downloadResult.downloaded() / downloadResult.contentLength()));
            String progressBar = "<#5EC26A>" + repeatString("|", bars) + "<#E0575B>" + repeatString("|", barsCount - bars);
            ComponentSender.sendActionbar(player, "<#F2C14E>Downloading... <#A0A0B0>(" + progressBar + "<#A0A0B0>)");
        }, 0, 200, TimeUnit.MILLISECONDS);

        try {
            downloadSuccessful.set(downloadResult.startDownload());
            if (downloadSuccessful.get()) {
                File pluginFile = FileUtils.getSelfJarFile();
                pluginFile.deleteOnExit();
            }
        } catch (IOException exception) {
            downloadLock.set(false);
            exception.printStackTrace();
            String consoleMsg = update ?
                    "§cFailed to download new version. Update manually at: https://github.com/nickuc-com/OpeNLogin/releases" :
                    "§cFailed to download nLogin :c. Download manually at: nickuc.com";
            String playerMsg = update ?
                    "<#E0575B>Failed to download new version. Update manually at: https://github.com/nickuc-com/OpeNLogin/releases" :
                    "<#E0575B>Failed to download nLogin :c. Download manually at: nickuc.com";
            plugin.sendMessage(consoleMsg);
            ComponentSender.send(player, playerMsg);
        } finally {
            downloadFinished.set(true);
        }
        return downloadSuccessful.get();
    }

    private String repeatString(String str, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(str);
        }
        return builder.toString();
    }
}
