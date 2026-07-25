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

package com.nickuc.openlogin.bukkit.listener;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.adventure.ComponentSender;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@AllArgsConstructor
public class PlayerAuthenticateListener implements Listener {

    private final OpenLoginBukkit plugin;
    private boolean welcomeMessage;

    @EventHandler
    public void onAsyncAuthenticate(AsyncAuthenticateEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("openlogin.admin")) return;

        if (welcomeMessage) {
            ComponentSender.send(player, "");
            ComponentSender.send(player, " <gradient:#7F5AF0:#2CB67D><bold>Welcome to OpeNLogin!</bold></gradient>");
            ComponentSender.send(player, "");
            ComponentSender.send(player, " <#A0A0B0>Documentation:");
            ComponentSender.send(player, " <#56CCF2>https://github.com/nickuc/OpeNLogin/tree/master/docs");
            ComponentSender.send(player, "");
            ComponentSender.send(player, " <#A0A0B0>If you need help, feel free to contact our support:");
            ComponentSender.send(player, " <#56CCF2>https://www.nickuc.com/discord");
            ComponentSender.send(player, "");
            welcomeMessage = false;
        } else if (plugin.isUpdateAvailable()) {
            ComponentSender.send(player, "");
            ComponentSender.send(player, " <#A0A0B0>A new version of <#5EC26A>OpeNLogin <#A0A0B0>is available <#5EC26A>(v" + plugin.getDescription().getVersion() + " -> " + plugin.getLatestVersion() + ")<#A0A0B0>.");
            ComponentSender.send(player, " <#A0A0B0>Use the command <white>'/openlogin update' <#A0A0B0>to download new version.");
            ComponentSender.send(player, "");
        }
    }
}
