/*
 * The MIT License (MIT)
 *
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

import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class AccountManagement {

    private final Map<String, Account> accountCache = new HashMap<>();

    private final Database database;

    /**
     * Checks if the password provided is valid.
     *
     * @param password the password to compare
     * @return true if the passwords match
     */
    public boolean comparePassword(@NonNull Account account, @NonNull String password) {
        String hashedPassword = account.getHashedPassword();
        if (hashedPassword == null) {
            return false;
        }
        if (!hashedPassword.startsWith("$2")) {
            throw new IllegalArgumentException("Invalid hashed password for " + account.getRealName() + "! " + hashedPassword);
        }
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * Retrieve or load an account.
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
            accountCache.remove(key);
        }
    }

    /**
     * Searches for saved accounts.
     *
     * @param name the name of the player
     * @return optional of {@link Account}
     */
    public Optional<Account> search(@NonNull String name) {
        try (Database.Query query = database.query("SELECT * FROM `openlogin` WHERE `name` = ?", name.toLowerCase())) {
            ResultSet resultSet = query.resultSet;
            if (resultSet.next()) {
                String realName = resultSet.getString("realname");
                String hashedPassword = resultSet.getString("password");
                String address = resultSet.getString("address");
                long lastLogin = resultSet.getLong("lastlogin");
                long regdate = resultSet.getLong("regdate");
                return Optional.of(new Account(realName, hashedPassword, address, lastLogin, regdate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Update the player's database column.
     *
     * @param name           the name of the player (realname)
     * @param hashedPassword the hashed password
     * @param address        the player address
     * @return true on success
     */
    public boolean update(@NonNull String name, @NonNull String hashedPassword, @Nullable String address) {
        return update(name, hashedPassword, address, true);
    }

    /**
     * Update the player's data.
     *
     * @param name           the name of the player (realname)
     * @param hashedPassword the hashed password
     * @param address        the player address
     * @param replace        forces update if player data exists
     * @return true on success
     */
    public boolean update(@NonNull String name, @NonNull String hashedPassword, @Nullable String address, boolean replace) {
        boolean exists = search(name).isPresent();
        if (exists) {
            if (!replace) {
                return false;
            }
        }

        if (hashedPassword.trim().isEmpty()) {
            return false;
        }

        long current = System.currentTimeMillis();

        try {
            if (exists) {
                database.update(
                        "UPDATE `openlogin` SET `password` = ?, `address` = ?, `lastlogin` = ? WHERE `name` = ?",
                        hashedPassword,
                        address == null ? "127.0.0.1" : address,
                        current,
                        name.toLowerCase()
                );
            } else {
                database.update(
                        "INSERT INTO `openlogin` (`name`, `realname`, `password`, `address`, `lastlogin`, `regdate`) VALUES (?, ?, ?, ?, ?, ?)",
                        name.toLowerCase(),
                        name,
                        hashedPassword,
                        address == null ? "127.0.0.1" : address,
                        current,
                        current
                );
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete all of the player's data.
     *
     * @param name the name of the player
     * @return true on success
     */
    public boolean delete(@NonNull String name) {
        boolean exists = search(name).isPresent();
        if (!exists) {
            return false;
        }

        try {
            database.update("DELETE FROM `openlogin` WHERE `name` = ?", name.toLowerCase());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
