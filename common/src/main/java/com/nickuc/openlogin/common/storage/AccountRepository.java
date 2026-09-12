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

package com.nickuc.openlogin.common.storage;

import com.nickuc.openlogin.common.model.Account;

import java.io.Closeable;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends Closeable {

    /**
     * Loads account data safely from storage.
     */
    void load();

    /**
     * Finds an account by its unique identifier.
     *
     * @param uuid the player's unique identifier
     * @return an Optional containing the Account if found
     */
    Optional<Account> findByUuid(UUID uuid);

    /**
     * Finds an account by player username (case-insensitive).
     *
     * @param username the player's username
     * @return an Optional containing the Account if found
     */
    Optional<Account> findByUsername(String username);

    /**
     * Returns all registered accounts.
     *
     * @return a collection of all accounts
     */
    Collection<Account> findAll();

    /**
     * Persists or updates an account.
     *
     * @param account the account to save
     * @return true on success
     */
    boolean save(Account account);

    /**
     * Deletes an account by its unique identifier.
     *
     * @param uuid the unique identifier of the account to delete
     * @return true if deleted
     */
    boolean delete(UUID uuid);

    /**
     * Deletes an account by player username.
     *
     * @param username the username of the account to delete
     * @return true if deleted
     */
    boolean deleteByUsername(String username);

    /**
     * Returns the total count of registered accounts.
     *
     * @return count of accounts
     */
    int count();

    /**
     * Flushes any pending changes to disk.
     */
    void flush();

    @Override
    void close();

}
