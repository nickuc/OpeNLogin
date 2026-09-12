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
import com.nickuc.openlogin.common.security.hashing.PasswordSecurity;
import com.nickuc.openlogin.common.security.hashing.PlaintextPasswordSecurity;
import com.nickuc.openlogin.common.storage.AccountRepository;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AccountManagement {

    private final Map<String, Account> accountCache = new HashMap<>();

    @Getter
    private final AccountRepository accountRepository;
    @Getter
    private final PasswordSecurity passwordSecurity;

    public AccountManagement(@NonNull AccountRepository accountRepository, @NonNull PasswordSecurity passwordSecurity) {
        this.accountRepository = accountRepository;
        this.passwordSecurity = passwordSecurity;
    }

    public AccountManagement(@NonNull AccountRepository accountRepository) {
        this(accountRepository, new PlaintextPasswordSecurity());
    }

    /**
     * Checks if the password provided is valid.
     *
     * @param account  the target account
     * @param password the password to compare
     * @return true if the passwords match
     */
    public boolean comparePassword(@NonNull Account account, @NonNull String password) {
        String storedPassword = account.getPassword();
        if (storedPassword == null) {
            return false;
        }
        return passwordSecurity.matches(password, storedPassword);
    }

    /**
     * Retrieve or load an account by name.
     *
     * @param name the name of the player
     * @return the player's {@link Account}. Failing, will return empty Optional.
     */
    public Optional<Account> retrieveOrLoad(@NonNull String name) {
        synchronized (accountCache) {
            Account account = accountCache.get(name.toLowerCase());
            if (account == null) {
                Optional<Account> accountOpt = search(name);
                if (accountOpt.isPresent()) {
                    account = accountOpt.get();
                    accountCache.put(name.toLowerCase(), account);
                }
            }
            return Optional.ofNullable(account);
        }
    }

    /**
     * Retrieve or load an account by UUID.
     *
     * @param uuid the unique identifier of the player
     * @return the player's {@link Account}. Failing, will return empty Optional.
     */
    public Optional<Account> retrieveOrLoad(@NonNull UUID uuid) {
        Optional<Account> accountOpt = search(uuid);
        accountOpt.ifPresent(this::addToCache);
        return accountOpt;
    }

    /**
     * Add an account to cache.
     *
     * @param account the account to add
     */
    public void addToCache(@NonNull Account account) {
        synchronized (accountCache) {
            accountCache.put(account.getRealName().toLowerCase(), account);
        }
    }

    /**
     * Invalidate an account from cache.
     *
     * @param key the key to invalidate
     */
    public void invalidateCache(@NonNull String key) {
        synchronized (accountCache) {
            accountCache.remove(key.toLowerCase());
        }
    }

    /**
     * Searches for saved accounts by username.
     *
     * @param name the name of the player
     * @return optional of {@link Account}
     */
    public Optional<Account> search(@NonNull String name) {
        return accountRepository.findByUsername(name);
    }

    /**
     * Searches for saved accounts by UUID.
     *
     * @param uuid the unique identifier of the player
     * @return optional of {@link Account}
     */
    public Optional<Account> search(@NonNull UUID uuid) {
        return accountRepository.findByUuid(uuid);
    }

    /**
     * Update the player's database column.
     *
     * @param name     the name of the player (realname)
     * @param password the password
     * @param address  the player address
     * @return true on success
     */
    public boolean update(@NonNull String name, @NonNull String password, @Nullable String address) {
        return update(name, password, address, true);
    }

    /**
     * Update the player's data.
     *
     * @param name     the name of the player (realname)
     * @param password the password
     * @param address  the player address
     * @param replace  forces update if player data exists
     * @return true on success
     */
    public boolean update(@NonNull String name, @NonNull String password, @Nullable String address, boolean replace) {
        return update(null, name, password, address, replace);
    }

    /**
     * Update the player's data with UUID awareness.
     *
     * @param uuid     the unique identifier (optional, will be resolved or generated if null)
     * @param name     the name of the player (realname)
     * @param password the password
     * @param address  the player address
     * @param replace  forces update if player data exists
     * @return true on success
     */
    public boolean update(@Nullable UUID uuid, @NonNull String name, @NonNull String password, @Nullable String address, boolean replace) {
        Optional<Account> existingOpt = search(name);
        boolean exists = existingOpt.isPresent();
        if (exists && !replace) {
            return false;
        }

        if (password.trim().isEmpty()) {
            return false;
        }

        long current = System.currentTimeMillis();
        UUID accountUuid = uuid;
        long regDate = current;

        if (exists) {
            Account existing = existingOpt.get();
            if (accountUuid == null) {
                accountUuid = existing.getUuid();
            }
            regDate = existing.getRegDate();
        } else if (accountUuid == null) {
            accountUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }

        String preparedPassword = passwordSecurity.prepareForStorage(password);
        Account account = new Account(
                accountUuid,
                name,
                preparedPassword,
                address == null ? "127.0.0.1" : address,
                current,
                regDate
        );

        boolean saved = accountRepository.save(account);
        if (saved) {
            addToCache(account);
        }
        return saved;
    }

    /**
     * Delete all of the player's data by username.
     *
     * @param name the name of the player
     * @return true on success
     */
    public boolean delete(@NonNull String name) {
        boolean deleted = accountRepository.deleteByUsername(name);
        if (deleted) {
            invalidateCache(name.toLowerCase());
        }
        return deleted;
    }

    /**
     * Delete all of the player's data by UUID.
     *
     * @param uuid the unique identifier of the player
     * @return true on success
     */
    public boolean delete(@NonNull UUID uuid) {
        Optional<Account> accountOpt = accountRepository.findByUuid(uuid);
        boolean deleted = accountRepository.delete(uuid);
        if (deleted) {
            accountOpt.ifPresent(account -> invalidateCache(account.getRealName().toLowerCase()));
        }
        return deleted;
    }
}
