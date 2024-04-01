/*
 * The MIT License (MIT)
 *
 * Copyright © 2024 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.command.executors;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.command.BukkitAbstractCommand;
import com.nickuc.openlogin.bukkit.ui.chat.ActionbarAPI;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.http.HttpClient;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.util.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenLoginCommand extends BukkitAbstractCommand {

    private final AtomicBoolean
            downloadLock = new AtomicBoolean(),
            confirmNLogin = new AtomicBoolean(),
            confirmOpenLogin = new AtomicBoolean(),
            confirmAd = new AtomicBoolean();

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
                    sender.sendMessage(Messages.PLUGIN_RELOAD_MESSAGE.asString());
                    return;
                }

                case "update": {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }

                    Player player = (Player) sender;
                    String name = player.getName();
                    if (!plugin.getLoginManagement().isAuthenticated(name)) {
                        return;
                    }

                    if (!plugin.isUpdateAvailable()) {
                        sender.sendMessage("§cYou are already using the latest version.");
                        return;
                    }

                    if (downloadLock.getAndSet(true)) {
                        sender.sendMessage("§cDownload in progress...");
                    } else if (!update(player)) {
                        downloadLock.set(false);
                    }
                    return;
                }

                case "nlogin_ad": {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }

                    if (!confirmAd.getAndSet(true)) {
                        sender.sendMessage("");
                        sender.sendMessage(" §cnLogin is generally a better solution for most users.");
                        sender.sendMessage(" §7If you want to keep §fOpeNLogin §7anyway,");
                        sender.sendMessage(" §7please click on the message again.");
                        sender.sendMessage("");
                        return;
                    }

                    if (plugin.getPluginSettings().set("nlogin_ad", Long.toString(System.currentTimeMillis()))) {
                        sender.sendMessage("§eYou will not be notified again of the migration for a long time.");
                    } else {
                        sender.sendMessage("§cDatabase error :C, please try again.");
                    }
                    return;
                }

                case "setup": {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }

                    if (!plugin.isNewUser()) {
                        return;
                    }

                    if (!confirmOpenLogin.getAndSet(true)) {
                        sender.sendMessage("");
                        sender.sendMessage(" §cnLogin is generally a better solution for most users.");
                        sender.sendMessage(" §7If you want to install §fOpeNLogin §7anyway,");
                        sender.sendMessage(" §7please click on the message again.");
                        sender.sendMessage("");
                        return;
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Player on : plugin.getServer().getOnlinePlayers()) {
                                on.kickPlayer("§aPlease rejoin to complete the plugin installation.");
                            }
                        }
                    }.runTask(plugin);

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
                        sender.sendMessage(Messages.PLAYER_COMMAND_USAGE.asString());
                        return;
                    }

                    Player player = (Player) sender;
                    String name = player.getName();
                    if (!plugin.isNewUser() && !plugin.getLoginManagement().isAuthenticated(name)) {
                        return;
                    }

                    if (downloadLock.get()) {
                        sender.sendMessage("§cDownload in progress...");
                        return;
                    }

                    boolean skip = args.length == 2 && args[1].equalsIgnoreCase("skip");
                    if (!skip && !confirmNLogin.getAndSet(true)) {
                        sender.sendMessage("");
                        sender.sendMessage(" §6nLogin §7is a §6proprietary §7authentication plugin,");
                        sender.sendMessage(" §7updated and maintained by §cnickuc.com§7. This means that you");
                        sender.sendMessage(" §7cannot view and modify the source code of the plugin.");
                        sender.sendMessage("");
                        sender.sendMessage(" §eIf you still have questions, please contact us:");
                        sender.sendMessage(" §bnickuc.com/discord");
                        sender.sendMessage("");
                        sender.sendMessage(" §7To proceed with the download, type §b/openlogin nlogin §7again.");
                        sender.sendMessage("");
                    } else {
                        if (downloadLock.getAndSet(true)) {
                            sender.sendMessage("§cDownload already in progress!");
                            return;
                        }

                        Runnable callback = null;
                        if (skip && plugin.isNewUser()) {
                            callback = () -> new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for (Player on : plugin.getServer().getOnlinePlayers()) {
                                        on.closeInventory();
                                        on.kickPlayer("§anLogin was successfully installed. We are restarting the server to apply the changes.");
                                    }
                                    plugin.getServer().shutdown();
                                }
                            }.runTask(plugin);
                            TitleAPI.getApi().reset(player);
                        }
                        if (!downloadNLogin(player, callback)) {
                            downloadLock.set(false);
                        }
                    }
                    return;
                }
            }
        }

        sender.sendMessage("");
        sender.sendMessage(" §eThis server is running §fOpenLogin v " + plugin.getDescription().getVersion() + ".");
        sender.sendMessage(" §7Powered by §bwww.nickuc.com§7.");
        sender.sendMessage("");
        sender.sendMessage(" §7GitHub: §fhttps://github.com/nickuc/OpeNLogin");
        sender.sendMessage("");
    }

    private boolean update(Player player) {
        File output = new File(plugin.getDataFolder().getParentFile(), "OpenLogin-" + plugin.getLatestVersion() + ".jar");
        return downloadActionbar(player, "https://github.com/nickuc/OpeNLogin/releases/download/" + plugin.getLatestVersion() + "/OpenLogin.jar", output, true, null);
    }

    private boolean downloadNLogin(Player player, Runnable callback) {
        File output = new File(plugin.getDataFolder().getParentFile(), "nLogin.jar");
        return downloadActionbar(player, "https://repo.nickuc.com/files/latest/nLogin.jar", output, false, callback);
    }

    private boolean downloadActionbar(Player player, String url, File output, boolean update, Runnable callback) {
        player.sendMessage("§eDownloading...");
        ActionbarAPI.getApi().send(player, "§eConnecting...");

        final int barsCount = 40;
        final HttpClient.AsyncDownloadResult downloadResult;
        try {
            if ((downloadResult = HttpClient.DEFAULT.download(url, output)) == null) {
                ActionbarAPI.getApi().send(player, "§cDownload failed!");
                player.sendMessage("§cDownload failed, could not delete old file.");
                return false;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }

        AtomicBoolean downloadFinished = new AtomicBoolean();
        AtomicBoolean downloadSuccessful = new AtomicBoolean();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (downloadFinished.get()) {
                    if (downloadSuccessful.get()) {
                        ActionbarAPI.getApi().send(player, "§aDownload finished! §7(§a" + repeatString("|", barsCount) + "§7)");
                        player.sendMessage("§aDownload finished. Please restart your server.");
                        if (callback != null) {
                            callback.run();
                        }
                    } else {
                        ActionbarAPI.getApi().send(player, "§cDownload failed! §7(§a" + repeatString("|", barsCount) + "§7)");
                        player.sendMessage("§cDownload failed, please try again.");
                    }
                    cancel();
                    return;
                }
                int bars = (int) (barsCount * (downloadResult.downloaded() / downloadResult.contentLength()));
                String progressBar = "§a" + repeatString("|", bars) + "§c" + repeatString("|", barsCount - bars);
                ActionbarAPI.getApi().send(player, "§eDownloading... §7(" + progressBar + "§7)");
            }
        }.runTaskTimer(plugin, 0, 4);

        try {
            downloadSuccessful.set(downloadResult.startDownload());
            if (downloadSuccessful.get()) {
                File pluginFile = FileUtils.getSelfJarFile();
                pluginFile.deleteOnExit();
            }
        } catch (IOException e) {
            downloadLock.set(false);
            e.printStackTrace();
            String msg = update ?
                    "§cFailed to download new version. Update manually at: https://github.com/nickuc/OpeNLogin/releases" :
                    "§cFailed to download nLogin :c. Download manually at: nickuc.com";
            plugin.sendMessage(msg);
            player.sendMessage(msg);
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
