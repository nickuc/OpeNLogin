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

package com.nickuc.openlogin.common.model;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Getter
public class Account {

    private final UUID uuid;
    private final String realName;
    private final String password;
    private final String address;
    private final long lastLogin;
    private final long regDate;

    public Account(UUID uuid, String realName, String password, String address, long lastLogin, long regDate) {
        this.uuid = uuid != null ? uuid : UUID.nameUUIDFromBytes(("OfflinePlayer:" + realName).getBytes(StandardCharsets.UTF_8));
        this.realName = realName;
        this.password = password;
        this.address = address;
        this.lastLogin = lastLogin;
        this.regDate = regDate;
    }

    public Account(String realName, String password, String address, long lastLogin, long regDate) {
        this(UUID.nameUUIDFromBytes(("OfflinePlayer:" + realName).getBytes(StandardCharsets.UTF_8)),
                realName, password, address, lastLogin, regDate);
    }

    /**
     * Backward-compatibility alias for {@link #getPassword()}.
     *
     * @return the stored password representation
     */
    public String getHashedPassword() {
        return password;
    }

}
