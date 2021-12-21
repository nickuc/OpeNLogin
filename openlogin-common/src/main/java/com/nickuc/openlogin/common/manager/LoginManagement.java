/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LoginManagement {

    private final Cache<String, Long> lock = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private final HashSet<String> logged = new HashSet<>();

    private final AccountManagement accountManagement;

    /**
     * Clears the player cache
     *
     * @param name the name of the player
     */
    public void cleanup(@NonNull String name) {
        String toLower = name.toLowerCase();
        lock.invalidate(toLower);
        logged.remove(toLower);
        accountManagement.invalidateCache(toLower);
    }

    /**
     * Set the player authenticated
     *
     * @param name the name of the player
     */
    public void setAuthenticated(@NonNull String name) {
        logged.add(name.toLowerCase());
    }

    /**
     * Check if the player is authenticated
     *
     * @param name the name of the player
     * @return true if authenticated
     */
    public boolean isAuthenticated(@NonNull String name) {
        return logged.contains(name.toLowerCase());
    }

    /**
     * Lock the commands
     *
     * @param name the name of the player
     */
    public void setLock(@NonNull String name) {
        lock.put(name.toLowerCase(), System.currentTimeMillis() + 750L);
    }

    /**
     * Check if the player is locked
     *
     * @param name the name of the player
     * @return true if locked
     */
    public boolean isLocked(@NonNull String name) {
        String toLower = name.toLowerCase();
        Long millis = lock.getIfPresent(toLower);
        return millis != null && millis - System.currentTimeMillis() >= 0;
    }
}
