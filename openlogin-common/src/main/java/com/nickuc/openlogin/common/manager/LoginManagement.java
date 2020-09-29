/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.model.Account;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class LoginManagement {

    private final Cache<String, Account> accountCache = CacheBuilder.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();

    private final HashMap<String, Long> lock = new HashMap<>();
    private final HashSet<String> logged = new HashSet<>();
    private final Database database;

    /**
     * Clears the player cache
     *
     * @param name the name of the player
     */
    public void cleanup(@NonNull String name) {
        String toLower = name.toLowerCase();
        lock.remove(toLower);
        logged.remove(toLower);
        accountCache.invalidate(toLower);
    }

    /**
     * Add an account to cache
     *
     * @param account the account to add
     */
    public void addToCache(@NonNull Account account) {
        accountCache.put(account.getRealname().toLowerCase(), account);
    }

    /**
     * Retrieve or load an account
     *
     * @param name the name of the player
     * @return the player's {@link Account}. Failing, will return empty Optional.
     */
    public Optional<Account> retrieveOrLoad(@NonNull String name) {
        Account account = accountCache.getIfPresent(name.toLowerCase());
        if (account == null) {
            Optional<Account> accountOpt = Account.search(database, name);
            if (accountOpt.isPresent()) {
                account = accountOpt.get();
                accountCache.put(name.toLowerCase(), account);
            }
        }
        return Optional.ofNullable(account);
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
    public void setLock(@NonNull String name, boolean locked) {
        String toLower = name.toLowerCase();
        if (locked)
            lock.put(toLower, System.currentTimeMillis() + 750L);
        else
            lock.remove(toLower);
    }

    /**
     * Check if the player is locked
     *
     * @param name the name of the player
     * @return true if locked
     */
    public boolean isLocked(@NonNull String name) {
        Long millis = lock.get(name);
        return millis != null && millis - System.currentTimeMillis() >= 0;
    }
}
