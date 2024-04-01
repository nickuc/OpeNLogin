/*
 * The MIT License (MIT)
 *
 * A practical, secure and friendly authentication plugin.
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

package com.nickuc.openlogin.common.manager;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RequiredArgsConstructor
public class LoginManagement {

    private final Map<String, Long> lock = new HashMap<>();
    private final HashSet<String> logged = new HashSet<>();

    private final AccountManagement accountManagement;

    /**
     * Clears the player cache
     *
     * @param name the name of the player
     */
    public void cleanup(@NonNull String name) {
        String nameLower = name.toLowerCase();
        synchronized (lock) {
            lock.remove(nameLower);
        }
        synchronized (logged) {
            logged.remove(nameLower);
        }
        accountManagement.invalidateCache(nameLower);
    }

    /**
     * Set the player authenticated.
     *
     * @param name the name of the player
     */
    public void setAuthenticated(@NonNull String name) {
        synchronized (logged) {
            logged.add(name.toLowerCase());
        }
    }

    /**
     * Check if the player is authenticated.
     *
     * @param name the name of the player
     * @return true if authenticated
     */
    public boolean isAuthenticated(@NonNull String name) {
        synchronized (logged) {
            return logged.contains(name.toLowerCase());
        }
    }

    /**
     * Checks if the player is unlocked and lock it.
     *
     * @param name the name of the player
     */
    public boolean isUnlocked(@NonNull String name) {
        String toLower = name.toLowerCase();
        synchronized (lock) {
            Long millis = lock.get(toLower);
            if (millis == null || millis - System.currentTimeMillis() < 0) {
                lock.put(name.toLowerCase(), System.currentTimeMillis() + 750L);
                return true;
            }
            return false;
        }
    }
}
