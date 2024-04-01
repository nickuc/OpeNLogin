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

package com.nickuc.openlogin.common.database;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class PluginSettings {

    private final Database database;

    public String read(@NonNull String key, @NonNull String def) {
        String value = read(key);
        return value == null ? def : value;
    }

    @Nullable
    public String read(@NonNull String key) {
        try (Database.Query query = database.query("SELECT `value` FROM `settings` WHERE `key` = ?", key)) {
            ResultSet resultSet = query.resultSet;
            if (resultSet.next()) {
                return resultSet.getString("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean set(@NonNull String key, @NonNull String value) {
        try (Database.Query query = database.query("SELECT `value` FROM `settings` WHERE `key` = ?", key)) {
            ResultSet resultSet = query.resultSet;
            if (resultSet.next()) {
                database.update("UPDATE `settings` SET `value` = ? WHERE `key` = ?", value, key);
            } else {
                database.update("INSERT INTO `settings` (`key`, `value`) VALUES (?, ?)", key, value);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
