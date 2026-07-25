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
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.util.TextComponentMessage;
import com.nickuc.openlogin.common.model.Title;
import com.nickuc.openlogin.common.settings.Messages;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinListeners implements Listener {

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();

        if (plugin.isNewUser()) {
            plugin.getFoliaLib().runLater(() -> {
                if (!player.isOnline()) {
                    return;
                }

                ComponentSender.send(player, "");
                ComponentSender.send(player, " <#F2C14E>Hello, " + player.getName() + "!");
                ComponentSender.send(player, "");
                ComponentSender.send(player, "  <#A0A0B0>Before we start, please select");
                ComponentSender.send(player, "  <#A0A0B0>your favorite login plugin.");
                ComponentSender.send(player, "");
                TextComponentMessage.sendPluginChoice(player);
                ComponentSender.send(player, "");

                ComponentSender.sendTitle(player,
                        new Title("", "<#A0A0B0>Please answer the question sent in the chat.", 0, 9999, 10));
            }, 30L);

            event.setJoinMessage("");
            return;
        }

        boolean registered = plugin.getAccountManagement().retrieveOrLoad(name).isPresent();
        LoginQueue.addToQueue(name, registered);

        player.setWalkSpeed(0F);
        player.setFlySpeed(0F);

        if (registered) {
            ComponentSender.send(player, Messages.MESSAGE_LOGIN.asComponent());
            ComponentSender.sendTitle(player, Messages.TITLE_BEFORE_LOGIN.asTitle());
        } else {
            ComponentSender.send(player, Messages.MESSAGE_REGISTER.asComponent());
            ComponentSender.sendTitle(player, Messages.TITLE_BEFORE_REGISTER.asTitle());
        }
    }
}
