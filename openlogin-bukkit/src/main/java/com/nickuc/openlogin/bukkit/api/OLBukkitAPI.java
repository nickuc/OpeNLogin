/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.api;

import com.nickuc.openlogin.bukkit.OpenLoginBukkit;
import com.nickuc.openlogin.common.api.OpenLoginAPI;
import com.nickuc.openlogin.common.manager.AccountManagement;
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
        return plugin.getAccountManagement().search(player);
    }

    @Override
    public boolean comparePassword(@NonNull String player, @NonNull String password) {
        AccountManagement accountManagement = plugin.getAccountManagement();
        Optional<Account> account = accountManagement.retrieveOrLoad(player);
        return account.isPresent() && accountManagement.comparePassword(account.get(), password);
    }

    @Override
    public boolean isRegistered(@NonNull String player) {
        return plugin.getAccountManagement().retrieveOrLoad(player).isPresent();
    }

    @Override
    public boolean update(@NonNull String player, @NonNull String password, String address, boolean replace) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);
        return plugin.getAccountManagement().update(player, hashedPassword, address, replace);
    }
}
