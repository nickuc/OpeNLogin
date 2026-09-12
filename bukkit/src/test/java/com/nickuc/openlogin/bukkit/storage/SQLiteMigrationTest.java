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

package com.nickuc.openlogin.bukkit.storage;

import com.nickuc.openlogin.bukkit.storage.migration.SQLiteMigration;
import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.hashing.BCrypt;
import com.nickuc.openlogin.common.security.hashing.PlaintextPasswordSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class SQLiteMigrationTest {

    @TempDir
    File tempDir;

    private File sqliteDbFile;
    private File accountsYamlFile;
    private YamlAccountRepository repository;
    private AccountManagement accountManagement;

    @BeforeEach
    public void setUp() throws Exception {
        sqliteDbFile = new File(tempDir, "accounts.db");
        accountsYamlFile = new File(tempDir, "accounts.yml");

        repository = new YamlAccountRepository(accountsYamlFile, Logger.getLogger("TestLogger"));
        repository.load();
        accountManagement = new AccountManagement(repository, new PlaintextPasswordSecurity());

        // Populate mock SQLite database
        Class.forName("org.sqlite.JDBC");
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + sqliteDbFile.getAbsolutePath());
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS `openlogin` (`name` TEXT, `realname` TEXT, `password` TEXT, `address` TEXT, `lastlogin` INTEGER, `regdate` INTEGER)");

            // Valid Plaintext account
            stmt.executeUpdate("INSERT INTO `openlogin` VALUES ('steve', 'Steve', 'stevePlainPass', '127.0.0.1', 1000, 1000)");

            // Valid BCrypt hashed account
            String hashedAlexPass = BCrypt.hashpw("alexSecret", BCrypt.gensalt());
            stmt.executeUpdate("INSERT INTO `openlogin` VALUES ('alex', 'Alex', '" + hashedAlexPass + "', '127.0.0.2', 2000, 2000)");

            // Duplicate of Steve (different password)
            stmt.executeUpdate("INSERT INTO `openlogin` VALUES ('steve', 'Steve', 'duplicatePass', '127.0.0.3', 3000, 3000)");

            // Malformed: null realname and name
            stmt.executeUpdate("INSERT INTO `openlogin` VALUES (NULL, NULL, 'orphanPass', '127.0.0.4', 4000, 4000)");

            // Malformed: null password
            stmt.executeUpdate("INSERT INTO `openlogin` VALUES ('nopass', 'NoPass', NULL, '127.0.0.5', 5000, 5000)");
        }
    }

    @AfterEach
    public void tearDown() {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void testCompleteSQLiteMigration() {
        long originalDbSize = sqliteDbFile.length();
        assertTrue(originalDbSize > 0, "Original SQLite file should exist and have content");

        // Run migration
        boolean migrated = SQLiteMigration.migrateIfPresent(tempDir, repository, Logger.getLogger("TestLogger"));
        assertTrue(migrated, "Migration should report success");

        // 1. Verify original SQLite database was not destroyed or modified
        assertTrue(sqliteDbFile.exists(), "Original SQLite database file must remain intact");
        assertEquals(originalDbSize, sqliteDbFile.length(), "Original SQLite database size must not be modified");

        // 2. Verify account counts (Steve + Alex = 2; duplicate and malformed skipped)
        assertEquals(2, repository.count(), "Repository should contain exactly the 2 valid migrated accounts");

        // 3. Verify Steve's account
        Optional<Account> steveOpt = accountManagement.retrieveOrLoad("Steve");
        assertTrue(steveOpt.isPresent(), "Steve should be loaded");
        Account steve = steveOpt.get();
        assertEquals("Steve", steve.getRealName());
        assertEquals("stevePlainPass", steve.getPassword());
        assertTrue(accountManagement.comparePassword(steve, "stevePlainPass"));

        // 4. Verify Alex's account with BCrypt backward compatibility
        Optional<Account> alexOpt = accountManagement.retrieveOrLoad("Alex");
        assertTrue(alexOpt.isPresent(), "Alex should be loaded");
        Account alex = alexOpt.get();
        assertEquals("Alex", alex.getRealName());
        assertTrue(alex.getPassword().startsWith("$2"), "Alex's password should retain its BCrypt format");
        assertTrue(accountManagement.comparePassword(alex, "alexSecret"), "BCrypt password should authenticate correctly via modular security");

        // 5. Verify re-running migration handles all as duplicates without error
        boolean reRun = SQLiteMigration.migrateIfPresent(tempDir, repository, Logger.getLogger("TestLogger"));
        assertTrue(reRun);
        assertEquals(2, repository.count(), "Account count should remain 2 on re-running migration");
    }

    @Test
    public void testAuthenticationDoesNotDependOnSQLite() {
        // Register a new account entirely through repository
        UUID newUuid = UUID.randomUUID();
        boolean saved = accountManagement.update(newUuid, "IndependentUser", "indepPass", "127.0.0.1", false);
        assertTrue(saved);

        // Delete the SQLite database completely to prove authentication has zero dependency on SQLite
        assertTrue(sqliteDbFile.delete(), "Deleting SQLite file should succeed");

        // Test search, login, change password, delete
        Optional<Account> accOpt = accountManagement.retrieveOrLoad("IndependentUser");
        assertTrue(accOpt.isPresent());
        assertTrue(accountManagement.comparePassword(accOpt.get(), "indepPass"));

        assertTrue(accountManagement.update(newUuid, "IndependentUser", "newIndepPass", "127.0.0.1", true));
        Account updatedAcc = accountManagement.retrieveOrLoad("IndependentUser").orElseThrow(AssertionError::new);
        assertTrue(accountManagement.comparePassword(updatedAcc, "newIndepPass"));

        assertTrue(accountManagement.delete("IndependentUser"));
        assertFalse(accountManagement.retrieveOrLoad("IndependentUser").isPresent());
    }

}
