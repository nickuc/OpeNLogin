/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
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
