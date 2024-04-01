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

package com.nickuc.openlogin.common.api;

import com.nickuc.openlogin.common.OpenLogin;
import com.nickuc.openlogin.common.model.Account;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

public interface OpenLoginAPI {

    static OpenLoginAPI getApi() {
        return OpenLogin.getApi();
    }

    /**
     * Get the player account.
     *
     * @param player the name of player
     * @return Optional of {@link Account}
     */
    Optional<Account> getAccount(@NonNull String player);

    /**
     * Checks if the password provided is valid.
     *
     * @param player   the name of the player
     * @param password the password to compare
     * @return true if the passwords match
     */
    boolean comparePassword(@NonNull String player, @NonNull String password);

    /**
     * Checks if the player is registered.
     *
     * @param player the name of the player
     * @return true if registered
     */
    boolean isRegistered(@NonNull String player);

    /**
     * Update the player's data.
     *
     * @param player   the name of the player
     * @param password the password to use
     * @param address  the player address
     * @param replace  forces update if player data exists
     * @return true on success
     */
    boolean update(@NonNull String player, @NonNull String password, @Nullable String address, boolean replace);

    /**
     * Update the player's data.
     *
     * @param player   the name of the player
     * @param password the password to use
     * @param replace  forces update if player data exists
     * @return true on success
     */
    default boolean update(@NonNull String player, @NonNull String password, boolean replace) {
        return update(player, password, null, replace);
    }

    /**
     * Update the player's data.
     *
     * @param player   the name of the player
     * @param password the password to use
     * @return true on success
     */
    default boolean update(@NonNull String player, @NonNull String password, @Nullable String address) {
        return update(player, password, null, true);
    }

    /**
     * Update the player's data.
     *
     * @param player   the name of the player
     * @param password the password to use
     * @return true on success
     */
    default boolean update(@NonNull String player, @NonNull String password) {
        return update(player, password, true);
    }

}
