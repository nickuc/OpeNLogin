/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
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
