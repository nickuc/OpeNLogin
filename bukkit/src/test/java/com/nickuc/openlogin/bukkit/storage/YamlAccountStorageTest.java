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

import com.nickuc.openlogin.common.manager.AccountManagement;
import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.security.hashing.PlaintextPasswordSecurity;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class YamlAccountStorageTest {

    @TempDir
    File tempDir;

    private File accountsFile;
    private YamlAccountRepository repository;
    private AccountManagement accountManagement;

    @BeforeEach
    public void setUp() {
        accountsFile = new File(tempDir, "accounts.yml");
        repository = new YamlAccountRepository(accountsFile, Logger.getLogger("TestLogger"));
        repository.load();
        accountManagement = new AccountManagement(repository, new PlaintextPasswordSecurity());
    }

    @AfterEach
    public void tearDown() {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void testMissingFileAndDirectoryCreation() {
        File nestedDir = new File(tempDir, "deeply/nested/plugins/OpenNLogin");
        File nestedAccounts = new File(nestedDir, "accounts.yml");

        assertFalse(nestedAccounts.exists());
        YamlAccountRepository nestedRepo = new YamlAccountRepository(nestedAccounts, Logger.getLogger("TestLogger"));
        nestedRepo.load();

        assertTrue(nestedAccounts.exists(), "accounts.yml should be automatically created");
        assertEquals(0, nestedRepo.count());
        nestedRepo.close();
    }

    @Test
    public void testFreshAccountRegistrationAndPlaintextStorage() throws IOException {
        UUID uuid = UUID.randomUUID();
        String username = "AlexPlayer";
        String password = "PlaintextPass#2026!";
        String address = "192.168.1.100";

        boolean registered = accountManagement.update(uuid, username, password, address, false);
        assertTrue(registered, "Account registration should succeed");

        // Verify account retrieved from memory
        Optional<Account> accountOpt = accountManagement.retrieveOrLoad(username);
        assertTrue(accountOpt.isPresent(), "Account should be retrievable");

        Account account = accountOpt.get();
        assertEquals(uuid, account.getUuid());
        assertEquals(username, account.getRealName());
        assertEquals(password, account.getPassword());
        assertEquals(password, account.getHashedPassword(), "getHashedPassword() should return the stored value for backward compatibility");

        // Verify direct YAML content on disk has plaintext password and correct structure
        YamlConfiguration diskYaml = YamlConfiguration.loadConfiguration(accountsFile);
        String yamlContent = new String(Files.readAllBytes(accountsFile.toPath()), StandardCharsets.UTF_8);

        assertTrue(diskYaml.contains("accounts." + uuid), "accounts.yml should contain the UUID key");
        assertEquals(username, diskYaml.getString("accounts." + uuid + ".username"));
        assertEquals(password, diskYaml.getString("accounts." + uuid + ".password"), "Password must be stored directly in plaintext");
        assertTrue(yamlContent.contains(password), "Raw password must exist unaltered in the YAML file content");
    }

    @Test
    public void testLoginWithCorrectAndIncorrectCredentials() {
        UUID uuid = UUID.randomUUID();
        String username = "SteveMiner";
        String correctPassword = "DiamondSword99";

        accountManagement.update(uuid, username, correctPassword, "127.0.0.1", false);
        Account account = accountManagement.retrieveOrLoad(username).orElseThrow(AssertionError::new);

        // Correct password
        assertTrue(accountManagement.comparePassword(account, "DiamondSword99"), "Correct password must match");

        // Incorrect password
        assertFalse(accountManagement.comparePassword(account, "WrongPass"), "Incorrect password must be rejected");
        assertFalse(accountManagement.comparePassword(account, "diamondsword99"), "Password check must be case-sensitive");
        assertFalse(accountManagement.comparePassword(account, ""), "Empty password must be rejected");
    }

    @Test
    public void testPasswordChangePersistence() {
        UUID uuid = UUID.randomUUID();
        String username = "ChangeMeUser";
        String initialPassword = "initialPassword1";
        String newPassword = "updatedPassword2";

        accountManagement.update(uuid, username, initialPassword, "127.0.0.1", false);
        Account account = accountManagement.retrieveOrLoad(username).orElseThrow(AssertionError::new);
        assertTrue(accountManagement.comparePassword(account, initialPassword));

        // Update password
        boolean updated = accountManagement.update(uuid, username, newPassword, "127.0.0.2", true);
        assertTrue(updated, "Password update should succeed");

        Account updatedAccount = accountManagement.retrieveOrLoad(username).orElseThrow(AssertionError::new);
        assertFalse(accountManagement.comparePassword(updatedAccount, initialPassword));
        assertTrue(accountManagement.comparePassword(updatedAccount, newPassword));

        // Check YAML on disk
        YamlConfiguration diskYaml = YamlConfiguration.loadConfiguration(accountsFile);
        assertEquals(newPassword, diskYaml.getString("accounts." + uuid + ".password"));
    }

    @Test
    public void testAccountDeletion() {
        UUID uuid = UUID.randomUUID();
        String username = "DeleteMePlayer";
        accountManagement.update(uuid, username, "deletePass", "127.0.0.1", false);

        assertTrue(accountManagement.search(username).isPresent());
        assertTrue(accountManagement.search(uuid).isPresent());

        // Delete account
        boolean deleted = accountManagement.delete(username);
        assertTrue(deleted, "Account deletion should succeed");

        assertFalse(accountManagement.search(username).isPresent());
        assertFalse(accountManagement.search(uuid).isPresent());
        assertFalse(accountManagement.retrieveOrLoad(username).isPresent());

        // Check YAML on disk
        YamlConfiguration diskYaml = YamlConfiguration.loadConfiguration(accountsFile);
        assertNull(diskYaml.get("accounts." + uuid), "Account section must be removed from YAML on disk");
    }

    @Test
    public void testServerRestartPersistence() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        accountManagement.update(uuid1, "PlayerOne", "passOne", "1.1.1.1", false);
        accountManagement.update(uuid2, "PlayerTwo", "passTwo", "2.2.2.2", false);

        // Close current repository to simulate server shutdown
        repository.close();

        // Simulate server restart by instantiating fresh repository and management
        YamlAccountRepository restartedRepo = new YamlAccountRepository(accountsFile, Logger.getLogger("TestLogger"));
        restartedRepo.load();
        AccountManagement restartedManagement = new AccountManagement(restartedRepo, new PlaintextPasswordSecurity());

        assertEquals(2, restartedRepo.count(), "Reconstructed cache should contain 2 accounts");

        Optional<Account> acc1 = restartedManagement.retrieveOrLoad("PlayerOne");
        assertTrue(acc1.isPresent());
        assertEquals("passOne", acc1.get().getPassword());
        assertTrue(restartedManagement.comparePassword(acc1.get(), "passOne"));

        Optional<Account> acc2 = restartedManagement.retrieveOrLoad("playertwo");
        assertTrue(acc2.isPresent());
        assertEquals("passTwo", acc2.get().getPassword());
        assertTrue(restartedManagement.comparePassword(acc2.get(), "passTwo"));

        restartedRepo.close();
    }

    @Test
    public void testMultipleAccountsAndCaseInsensitiveLookup() {
        accountManagement.update(UUID.randomUUID(), "AlphaGamer", "p1", "127.0.0.1", false);
        accountManagement.update(UUID.randomUUID(), "BetaCrafter", "p2", "127.0.0.1", false);
        accountManagement.update(UUID.randomUUID(), "GammaBuilder", "p3", "127.0.0.1", false);

        assertEquals(3, repository.count());

        // Case-insensitive retrieval
        assertTrue(accountManagement.retrieveOrLoad("alphagamer").isPresent());
        assertTrue(accountManagement.retrieveOrLoad("ALPHAGAMER").isPresent());
        assertTrue(accountManagement.retrieveOrLoad("AlPhAgAmEr").isPresent());

        // Preserved case in model
        Account alpha = accountManagement.retrieveOrLoad("alphagamer").get();
        assertEquals("AlphaGamer", alpha.getRealName());
    }

    @Test
    public void testMalformedYamlRecovery() throws IOException {
        // Corrupt the accounts.yml file with invalid syntax
        try (FileWriter writer = new FileWriter(accountsFile)) {
            writer.write("accounts: [ broken YAML syntax %%% {{{ ]\n  invalid: @@@\n");
        }

        YamlAccountRepository corruptRepo = new YamlAccountRepository(accountsFile, Logger.getLogger("TestLogger"));
        assertDoesNotThrow(corruptRepo::load, "Loading malformed YAML should recover gracefully without crashing");

        // Should recover to clean state
        assertEquals(0, corruptRepo.count());

        // Verify a corrupted backup file was created
        File[] backupFiles = tempDir.listFiles((dir, name) -> name.contains("corrupted"));
        assertNotNull(backupFiles);
        assertTrue(backupFiles.length > 0, "A corrupted backup file should have been created");

        // Should be able to register new accounts safely
        corruptRepo.save(new Account(UUID.randomUUID(), "RecoveredUser", "pass", "127.0.0.1", 0L, 0L));
        assertEquals(1, corruptRepo.count());
        corruptRepo.close();
    }

    @Test
    public void testConcurrentWritesSafety() throws InterruptedException, ExecutionException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Callable<Boolean>> tasks = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            tasks.add(() -> {
                UUID uuid = UUID.randomUUID();
                String username = "ConcurrentPlayer" + index;
                String password = "password" + index;
                return accountManagement.update(uuid, username, password, "127.0.0.1", false);
            });
        }

        List<Future<Boolean>> futures = executor.invokeAll(tasks);
        for (Future<Boolean> future : futures) {
            assertTrue(future.get(), "Each concurrent registration should succeed");
        }
        executor.shutdown();

        assertEquals(threadCount, repository.count(), "All accounts should be persisted in memory");

        // Verify YAML on disk is valid and complete
        YamlConfiguration diskYaml = YamlConfiguration.loadConfiguration(accountsFile);
        for (int i = 0; i < threadCount; i++) {
            String username = "ConcurrentPlayer" + i;
            Account acc = accountManagement.retrieveOrLoad(username).orElseThrow(AssertionError::new);
            assertEquals("password" + i, diskYaml.getString("accounts." + acc.getUuid() + ".password"));
        }
    }
}
