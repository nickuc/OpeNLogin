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

package com.nickuc.openlogin.common.manager;

import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.model.Session;
import com.nickuc.openlogin.common.settings.Settings;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Platform-agnostic session management.
 * Works with raw name and ip — each platform extracts them on its own.
 */
public final class SessionManagement {

    private final AccountManagement accountManagement;
    private final LoginManagement loginManagement;
    private final Consumer<String> removeFromQueue;

    public SessionManagement(@NonNull AccountManagement accountManagement,
                             @NonNull LoginManagement loginManagement,
                             @NonNull Consumer<String> removeFromQueue) {
        this.accountManagement = accountManagement;
        this.loginManagement = loginManagement;
        this.removeFromQueue = removeFromQueue;
    }

    /**
     * Attempts to restore a player's session on join.
     *
     * @param name      the player name
     * @param currentIp the player's current IP
     * @return result of the session restoration attempt
     */
    public SessionResult tryRestore(@NonNull String name, @NonNull String currentIp) {
        Optional<Account> opt = accountManagement.retrieveOrLoad(name);
        if (!opt.isPresent()) return SessionResult.noAccount();

        Account account = opt.get();
        Session session = new Session(account.getAddress(), account.getLastLogin());

        if (!session.isValid(currentIp, Settings.SESSION_TIMEOUT.asInt())) {
            return SessionResult.expired(account);
        }

        loginManagement.setAuthenticated(name);
        accountManagement.updateSession(name, currentIp);
        removeFromQueue.accept(name);
        return SessionResult.restored(account);
    }

    /**
     * Refreshes session data after successful password login.
     *
     * @param name the player name
     * @param ip   the player's current IP
     */
    public void refresh(@NonNull String name, @NonNull String ip) {
        accountManagement.updateSession(name, ip);
    }

    /**
     * Result of a session restoration attempt.
     */
    public static final class SessionResult {
        private final Account account;
        private final State state;

        private SessionResult(Account account, State state) {
            this.account = account;
            this.state = state;
        }

        public static SessionResult noAccount() {
            return new SessionResult(null, State.NO_ACCOUNT);
        }

        public static SessionResult expired(Account account) {
            return new SessionResult(account, State.EXPIRED);
        }

        public static SessionResult restored(Account account) {
            return new SessionResult(account, State.RESTORED);
        }

        public boolean isRestored() {
            return state == State.RESTORED;
        }

        public Account account() {
            return account;
        }

        public enum State {
            NO_ACCOUNT,
            EXPIRED,
            RESTORED
        }
    }
}
