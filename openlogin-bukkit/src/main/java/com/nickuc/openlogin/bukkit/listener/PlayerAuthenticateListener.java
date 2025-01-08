/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.listener;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import com.nickuc.openlogin.bukkit.util.TextComponentMessage;
import com.nickuc.openlogin.common.util.ClassUtils;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class PlayerAuthenticateListener implements Listener {

    private final OpenLoginBukkit plugin;
    private boolean welcomeMessage;

    @EventHandler
    public void onAsyncAuthenticate(AsyncAuthenticateEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("openlogin.admin")) {
            if (welcomeMessage) {
                player.sendMessage("");
                player.sendMessage(" §eWelcome to OpeNLogin!");
                player.sendMessage("");
                player.sendMessage(" §7Documentation:");
                player.sendMessage(" §bhttps://github.com/nickuc/OpeNLogin/tree/master/docs");
                player.sendMessage("");
                player.sendMessage(" §7If you need help, fell free to contact our support:");
                player.sendMessage(" §bhttps://www.nickuc.com/discord");
                player.sendMessage("");
                welcomeMessage = false;
            } else if (plugin.isUpdateAvailable()) {
                player.sendMessage("");
                player.sendMessage(" §7A new version of §aOpeNLogin §7is available §a(v" + plugin.getDescription().getVersion() + " -> " + plugin.getLatestVersion() + ")§7.");
                player.sendMessage(" §7Use the command §f'/openlogin update' §7to download new version.");
                player.sendMessage("");
            } else if (!plugin.isNewUser() &&
                    ClassUtils.exists("net.md_5.bungee.api.chat.TextComponent") &&
                    System.currentTimeMillis() - Long.parseLong(plugin.getPluginSettings().read("setup_date", "0")) > 7 * 86400 * 1000L) { // 7 days
                String value = plugin.getPluginSettings().read("nlogin_ad");
                if (value != null) {
                    long timestamp = Long.parseLong(value);
                    if (timestamp != -1 && System.currentTimeMillis() - timestamp > 30 * 86400 * 1000L) { // 30 days
                        TextComponentMessage.sendPluginAd(player);
                    }
                } else {
                    TextComponentMessage.sendPluginAd(player);
                }
            }
        }
    }
}
