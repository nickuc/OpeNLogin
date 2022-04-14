/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.api;

import com.nickuc.openlogin.common.model.Account;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Optional;

public interface OpenLoginAPI {

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
