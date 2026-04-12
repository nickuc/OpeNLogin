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
import com.nickuc.openlogin.bukkit.i18n.LocaleManager;
import com.nickuc.openlogin.bukkit.task.LoginQueue;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.manager.LoginManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.model.Session;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.settings.Settings;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Manages player sessions: check, restore, and refresh.
 */
public final class SessionManager {

    private final OpenLoginBukkit plugin;
    private final AccountManagement accountManagement;
    private final LoginManagement loginManagement;

    public SessionManager(OpenLoginBukkit plugin) {
        this.plugin = plugin;
        this.accountManagement = plugin.getAccountManagement();
        this.loginManagement = plugin.getLoginManagement();
    }

    /**
     * Attempts to restore a player's session on join.
     *
     * @param player the player to check
     * @return true if session was valid and restored
     */
    public boolean tryRestore(Player player) {
        String currentIp = getPlayerIp(player);
        if (currentIp == null) return false;

        Optional<Account> opt = accountManagement.retrieveOrLoad(player.getName());
        if (!opt.isPresent()) return false;

        Account account = opt.get();
        Session session = new Session(account.getAddress(), account.getLastLogin());

        if (!session.isValid(currentIp, Settings.SESSION_TIMEOUT.asInt())) {
            return false;
        }

        // Session valid — auto-login
        loginManagement.setAuthenticated(player.getName());
        accountManagement.updateSession(player.getName(), currentIp);
        LoginQueue.removeFromQueue(player.getName());

        sendSessionMessage(player);
        plugin.getFoliaLib().runAsync(task -> new AsyncAuthenticateEvent(player).callEvt());
        return true;
    }

    /**
     * Refreshes session data after successful password login.
     */
    public void refresh(Player player) {
        String ip = getPlayerIp(player);
        if (ip != null) {
            accountManagement.updateSession(player.getName(), ip);
        }
    }

    private String getPlayerIp(Player player) {
        try {
            java.net.InetSocketAddress addr = player.getAddress();
            return addr != null ? addr.getAddress().getHostAddress() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendSessionMessage(Player player) {
        LocaleManager lm = plugin.getLocaleManager();
        player.sendMessage(lm.get(player, Messages.SESSION_RESTORED));
        TitleAPI.getApi().send(player,
                lm.getTitle(player, Messages.TITLE_SESSION_RESTORED.getKey(), Messages.TITLE_SESSION_RESTORED.asTitle()));
    }
}
