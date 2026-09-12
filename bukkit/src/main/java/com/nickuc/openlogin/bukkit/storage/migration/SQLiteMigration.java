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

package com.nickuc.openlogin.bukkit.storage.migration;

import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.storage.AccountRepository;
import lombok.NonNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteMigration {

    /**
     * Attempts to find and migrate legacy SQLite account data into the target repository.
     *
     * @param dataFolder the plugin data directory
     * @param repository the target account repository
     * @param logger     the plugin logger for logging summaries
     * @return true if migration was attempted and completed
     */
    public static boolean migrateIfPresent(@NonNull File dataFolder, @NonNull AccountRepository repository, Logger logger) {
        File sqliteFile = findSQLiteDatabase(dataFolder);
        if (sqliteFile == null) {
            return false;
        }

        log(logger, Level.INFO, "Legacy SQLite database detected at: " + sqliteFile.getAbsolutePath());
        log(logger, Level.INFO, "Starting account data migration to YAML format...");

        int totalProcessed = 0;
        int migratedCount = 0;
        int duplicateCount = 0;
        int malformedCount = 0;

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log(logger, Level.SEVERE, "SQLite JDBC driver not found. Skipping SQLite migration.", e);
            return false;
        }

        String jdbcUrl = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            // Check if openlogin table exists
            DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet tables = meta.getTables(null, null, "openlogin", null)) {
                if (!tables.next()) {
                    log(logger, Level.INFO, "Table 'openlogin' not found in SQLite database. Nothing to migrate.");
                    return false;
                }
            }

            String query = "SELECT name, realname, password, address, lastlogin, regdate FROM `openlogin`";
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(query)) {

                while (rs.next()) {
                    totalProcessed++;

                    String name = rs.getString("name");
                    String realName = rs.getString("realname");
                    String password = rs.getString("password");
                    String address = rs.getString("address");
                    long lastLogin = rs.getLong("lastlogin");
                    long regDate = rs.getLong("regdate");

                    String effectiveName = (realName != null && !realName.trim().isEmpty()) ? realName : name;
                    if (effectiveName == null || effectiveName.trim().isEmpty()) {
                        log(logger, Level.WARNING, "Skipping malformed SQLite record without player name (row " + totalProcessed + ")");
                        malformedCount++;
                        continue;
                    }

                    if (password == null || password.trim().isEmpty()) {
                        log(logger, Level.WARNING, "Skipping malformed SQLite record without password for player: " + effectiveName);
                        malformedCount++;
                        continue;
                    }

                    // Generate UUID based on offline-mode UUID standard for player realname
                    UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + effectiveName).getBytes(StandardCharsets.UTF_8));

                    // Check for duplicate username or UUID
                    if (repository.findByUsername(effectiveName).isPresent() || repository.findByUuid(uuid).isPresent()) {
                        duplicateCount++;
                        continue;
                    }

                    Account account = new Account(
                            uuid,
                            effectiveName,
                            password,
                            address != null ? address : "127.0.0.1",
                            lastLogin > 0 ? lastLogin : System.currentTimeMillis(),
                            regDate > 0 ? regDate : System.currentTimeMillis()
                    );

                    if (repository.save(account)) {
                        migratedCount++;
                    } else {
                        log(logger, Level.WARNING, "Failed to save migrated account for player: " + effectiveName);
                        malformedCount++;
                    }
                }
            }

            repository.flush();

            // Print clear migration summary
            log(logger, Level.INFO, "==================== MIGRATION SUMMARY ====================");
            log(logger, Level.INFO, " Source SQLite database: " + sqliteFile.getName());
            log(logger, Level.INFO, " Accounts processed: " + totalProcessed);
            log(logger, Level.INFO, " Successfully migrated to YAML: " + migratedCount);
            log(logger, Level.INFO, " Duplicate / already existing skipped: " + duplicateCount);
            log(logger, Level.INFO, " Malformed / invalid records skipped: " + malformedCount);
            log(logger, Level.INFO, " Original SQLite database preserved untouched.");
            log(logger, Level.INFO, "==========================================================");

            return true;
        } catch (SQLException exception) {
            log(logger, Level.SEVERE, "An error occurred while migrating accounts from SQLite database.", exception);
            return false;
        }
    }

    private static File findSQLiteDatabase(File dataFolder) {
        File direct = new File(dataFolder, "accounts.db");
        if (direct.exists() && direct.isFile() && direct.length() > 0) {
            return direct;
        }

        File sub = new File(new File(dataFolder, "database"), "accounts.db");
        if (sub.exists() && sub.isFile() && sub.length() > 0) {
            return sub;
        }

        return null;
    }

    private static void log(Logger logger, Level level, String message) {
        log(logger, level, message, null);
    }

    private static void log(Logger logger, Level level, String message, Throwable throwable) {
        if (logger != null) {
            if (throwable != null) {
                logger.log(level, message, throwable);
            } else {
                logger.log(level, message);
            }
        }
    }

}
