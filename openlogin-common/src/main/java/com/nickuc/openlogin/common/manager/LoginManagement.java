/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
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
