/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.api;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.common.api.OpenLoginAPI;
import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class OLBukkitAPI implements OpenLoginAPI {

    private final OpenLoginBukkit plugin;

    @Override
    public Optional<Account> getAccount(@NonNull String player) {
        Database database = plugin.getDatabase();
        return Account.search(database, player);
    }

    @Override
    public boolean comparePassword(@NonNull String player, @NonNull String password) {
        Optional<Account> account = plugin.getLoginManagement().retrieveOrLoad(player);
        return account.isPresent() && account.get().comparePassword(password);
    }

    @Override
    public boolean isRegistered(@NonNull String player) {
        return plugin.getLoginManagement().retrieveOrLoad(player).isPresent();
    }

    @Override
    public boolean update(@NonNull String player, @NonNull String password, String address, boolean replace) {
        Database database = plugin.getDatabase();
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
        return Account.update(database, player, hashedPassword, address, replace);
    }
}
