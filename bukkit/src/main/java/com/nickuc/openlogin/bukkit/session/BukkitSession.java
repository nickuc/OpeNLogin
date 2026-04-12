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

package com.nickuc.openlogin.bukkit.session;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.bukkit.api.events.AsyncAuthenticateEvent;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.manager.SessionManagement;
import com.nickuc.openlogin.common.model.Session;
import com.nickuc.openlogin.common.settings.Messages;
import org.bukkit.entity.Player;

/**
 * Bukkit session facade.
 * Extracts platform data and delegates to {@link SessionManagement}.
 */
public final class BukkitSession {

    private final OpenLoginBukkit plugin;
    private final SessionManagement sessionManagement;

    public BukkitSession(OpenLoginBukkit plugin) {
        this.plugin = plugin;
        AccountManagement accountManagement = plugin.getAccountManagement();
        LoginManagement loginManagement = plugin.getLoginManagement();

        this.sessionManagement = new SessionManagement(
                accountManagement,
                loginManagement,
                LoginQueue::removeFromQueue
        );
    }

    public boolean tryRestore(Player player) {
        String ip = Session.extractIp(player.getAddress());
        if (ip == null) return false;

        SessionManagement.SessionResult result = sessionManagement.tryRestore(player.getName(), ip);
        if (!result.isRestored()) return false;

        player.sendMessage(Messages.SUCCESSFUL_SESSION_LOGIN.asString());
        TitleAPI.getApi().send(player, Messages.TITLE_SESSION_LOGIN.asTitle());

        plugin.getFoliaLib().runAsync(task -> new AsyncAuthenticateEvent(player).callEvt());
        return true;
    }

    public void refresh(Player player) {
        String ip = Session.extractIp(player.getAddress());
        if (ip != null) {
            sessionManagement.refresh(player.getName(), ip);
        }
    }
}
