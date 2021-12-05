/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.model;

import com.nickuc.openlogin.common.database.Database;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Account {

    private final String realname, hashedPassword, address;
    private final long lastlogin, regdate;

    /**
     * Checks if the password provided is valid
     *
     * @param password the password to compare
     * @return true if the passwords match
     */
    public boolean comparePassword(@NonNull String password) {
        if (!hashedPassword.startsWith("$2")) {
            throw new IllegalArgumentException("Invalid hashed password for " + realname + "! " + password);
        }
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * Searches for saved accounts
     *
     * @param database the database to use
     * @param name     the name of the player
     * @return optional of {@link Account}
     */
    public static Optional<Account> search(@NonNull Database database, @NonNull String name) {
        try (Database.Query query = database.query("SELECT * FROM `openlogin` WHERE `name` = '" + name.toLowerCase() + "'")) {
            ResultSet resultSet = query.resultSet;
            if (resultSet.next()) {
                String realname = resultSet.getString("realname");
                String hashedPassword = resultSet.getString("password");
                String address = resultSet.getString("address");
                long lastlogin = Long.parseLong(resultSet.getString("lastlogin"));
                long regdate = Long.parseLong(resultSet.getString("regdate"));
                return Optional.of(new Account(realname, hashedPassword, address, lastlogin, regdate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Update the player's database column
     *
     * @param database       the database to use
     * @param name           the name of the player (realname)
     * @param hashedPassword the hashed password
     * @param address        the player address
     * @return true on success
     */
    public static boolean update(@NonNull Database database, @NonNull String name, @Nullable String hashedPassword, @Nullable String address) {
        return update(database, name, hashedPassword, address, true);
    }

    /**
     * Update the player's data
     *
     * @param database       the database to use
     * @param name           the name of the player (realname)
     * @param hashedPassword the hashed password
     * @param address        the player address
     * @param replace        forces update if player data exists
     * @return true on success
     */
    public static boolean update(@NonNull Database database, @NonNull String name, @Nullable String hashedPassword, @Nullable String address, boolean replace) {
        boolean exists = search(database, name).isPresent();
        if (exists) {
            if (!replace) {
                return false;
            }
        } else if (hashedPassword == null) {
            return false;
        }

        long current = System.currentTimeMillis();

        String command = exists ?
                "UPDATE `openlogin` SET " + (hashedPassword == null ? "" : "`password` = '" + hashedPassword + "', ") + (address == null ? "" : "`address` = '" + address + "', ") + " `lastlogin` = '" + current + "' WHERE `name` = '" + name.toLowerCase() + "'" :
                "INSERT INTO `openlogin` (`name`, `realname`, `password`, `address`, `lastlogin`, `regdate`) VALUES ('" + name.toLowerCase() + "', '" + name + "', '" + hashedPassword + "', '" + (address == null ? "127.0.0.1" : address) + "', '" + current + "', '" + current + "')";

        try {
            database.update(command);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete all of the player's data
     *
     * @param database the database to use
     * @param name     the name of the player
     * @return true on success
     */
    public static boolean delete(@NonNull Database database, @NonNull String name) {
        boolean exists = search(database, name).isPresent();
        if (!exists) {
            return false;
        }

        String command = "DELETE FROM `openlogin` WHERE `name` = '" + name.toLowerCase() + "'";

        try {
            database.update(command);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
