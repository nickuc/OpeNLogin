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
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.settings.Messages;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PlayerKickListeners implements Listener {

    private static final Pattern VALID_NICK = Pattern.compile("([a-zA-Z0-9_]{3,16})|(\\*[a-zA-Z0-9_]{3,17})");

    private final OpenLoginBukkit plugin;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(AsyncPlayerPreLoginEvent event) {
        String name = event.getName();
        Player player = Bukkit.getPlayerExact(name);

        // prevent double online nickname
        if (player != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ComponentSender.toKickString(Messages.ALREADY_ONLINE.asComponent()));
            return;
        }

        // prevent invalid nicknames
        if (!VALID_NICK.matcher(name).matches()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ComponentSender.toKickString(Messages.INVALID_NICKNAME.asComponent()));
            return;
        }

        Optional<Account> accountOpt = plugin.getAccountManagement().retrieveOrLoad(name);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            String realname = account.getRealName();
            if (!name.equals(realname)) {
                String kickMessage = ComponentSender.toKickString(Messages.NICK_ALREADY_REGISTERED.asComponent(
                        Placeholder.unparsed("nickname", name),
                        Placeholder.unparsed("realname", realname)));
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayer().getName());

        // prevent double online nickname
        if (player != null) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ComponentSender.toKickString(Messages.ALREADY_ONLINE.asComponent()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKick(PlayerKickEvent event) {
        String reason = event.getReason();

        // prevent kick online players
        if (reason.contains("You logged in from another location")) {
            event.setCancelled(true);
        }
    }

}
