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

import com.nickuc.openlogin.common.model.Account;
import com.nickuc.openlogin.common.storage.AccountRepository;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlAccountRepository implements AccountRepository {

    private final File file;
    private final Logger logger;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final Map<UUID, Account> accountsByUuid = new ConcurrentHashMap<>();
    private final Map<String, UUID> usernameToUuid = new ConcurrentHashMap<>();
    private YamlConfiguration configuration = new YamlConfiguration();

    public YamlAccountRepository(@NonNull File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public YamlAccountRepository(@NonNull File file) {
        this(file, null);
    }

    @Override
    public void load() {
        rwLock.writeLock().lock();
        try {
            accountsByUuid.clear();
            usernameToUuid.clear();

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    log(Level.SEVERE, "Failed to create directory structure for: " + parent.getAbsolutePath(), null);
                }
            }

            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        configuration = new YamlConfiguration();
                        configuration.createSection("accounts");
                        saveConfigurationDirect(configuration, file);
                        log(Level.INFO, "Created new accounts storage file: " + file.getName(), null);
                    }
                } catch (IOException e) {
                    log(Level.SEVERE, "Failed to create accounts storage file: " + file.getAbsolutePath(), e);
                }
                return;
            }

            try {
                configuration = YamlConfiguration.loadConfiguration(file);
            } catch (Exception exception) {
                log(Level.SEVERE, "Failed to load accounts from " + file.getName() + " due to malformed YAML syntax.", exception);
                backupCorruptedFile();
                configuration = new YamlConfiguration();
                configuration.createSection("accounts");
                saveConfigurationDirect(configuration, file);
                return;
            }

            ConfigurationSection accountsSection = configuration.getConfigurationSection("accounts");
            if (accountsSection == null) {
                accountsSection = configuration.createSection("accounts");
            }

            for (String key : accountsSection.getKeys(false)) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(key);
                } catch (IllegalArgumentException ex) {
                    log(Level.WARNING, "Skipping malformed UUID key in accounts.yml: '" + key + "'", null);
                    continue;
                }

                ConfigurationSection userSection = accountsSection.getConfigurationSection(key);
                String username;
                String password;
                String address;
                long lastLogin;
                long regDate;

                if (userSection != null) {
                    username = userSection.getString("username");
                    password = userSection.getString("password");
                    address = userSection.getString("address", "127.0.0.1");
                    lastLogin = userSection.getLong("lastlogin", 0L);
                    regDate = userSection.getLong("regdate", System.currentTimeMillis());
                } else {
                    username = accountsSection.getString(key + ".username");
                    password = accountsSection.getString(key + ".password");
                    address = accountsSection.getString(key + ".address", "127.0.0.1");
                    lastLogin = accountsSection.getLong(key + ".lastlogin", 0L);
                    regDate = accountsSection.getLong(key + ".regdate", System.currentTimeMillis());
                }

                if (username == null || username.trim().isEmpty()) {
                    log(Level.WARNING, "Skipping account record with missing username for UUID: " + uuid, null);
                    continue;
                }

                if (password == null) {
                    log(Level.WARNING, "Skipping account record with missing password for username: " + username, null);
                    continue;
                }

                Account account = new Account(uuid, username, password, address, lastLogin, regDate);
                accountsByUuid.put(uuid, account);
                usernameToUuid.put(username.toLowerCase(), uuid);
            }

            log(Level.INFO, "Successfully loaded " + accountsByUuid.size() + " account(s) from " + file.getName(), null);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private void backupCorruptedFile() {
        if (!file.exists()) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File backup = new File(file.getParentFile(), file.getName() + ".corrupted_" + timestamp);
        try {
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log(Level.WARNING, "Backed up corrupted accounts file to: " + backup.getName(), null);
        } catch (IOException e) {
            log(Level.SEVERE, "Failed to backup corrupted accounts file.", e);
        }
    }

    @Override
    public Optional<Account> findByUuid(@NonNull UUID uuid) {
        rwLock.readLock().lock();
        try {
            return Optional.ofNullable(accountsByUuid.get(uuid));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Optional<Account> findByUsername(@NonNull String username) {
        rwLock.readLock().lock();
        try {
            UUID uuid = usernameToUuid.get(username.toLowerCase());
            if (uuid == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(accountsByUuid.get(uuid));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public Collection<Account> findAll() {
        rwLock.readLock().lock();
        try {
            return Collections.unmodifiableCollection(new ArrayList<>(accountsByUuid.values()));
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public boolean save(@NonNull Account account) {
        rwLock.writeLock().lock();
        try {
            // Handle rename if username changed for this UUID
            Account existing = accountsByUuid.get(account.getUuid());
            if (existing != null && !existing.getRealName().equalsIgnoreCase(account.getRealName())) {
                usernameToUuid.remove(existing.getRealName().toLowerCase());
            }

            accountsByUuid.put(account.getUuid(), account);
            usernameToUuid.put(account.getRealName().toLowerCase(), account.getUuid());

            String path = "accounts." + account.getUuid().toString();
            configuration.set(path + ".username", account.getRealName());
            configuration.set(path + ".password", account.getPassword());
            configuration.set(path + ".address", account.getAddress());
            configuration.set(path + ".lastlogin", account.getLastLogin());
            configuration.set(path + ".regdate", account.getRegDate());

            return persistToFile();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean delete(@NonNull UUID uuid) {
        rwLock.writeLock().lock();
        try {
            Account account = accountsByUuid.remove(uuid);
            if (account == null) {
                return false;
            }
            usernameToUuid.remove(account.getRealName().toLowerCase());
            configuration.set("accounts." + uuid.toString(), null);
            return persistToFile();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public boolean deleteByUsername(@NonNull String username) {
        rwLock.writeLock().lock();
        try {
            UUID uuid = usernameToUuid.remove(username.toLowerCase());
            if (uuid == null) {
                return false;
            }
            accountsByUuid.remove(uuid);
            configuration.set("accounts." + uuid.toString(), null);
            return persistToFile();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public int count() {
        rwLock.readLock().lock();
        try {
            return accountsByUuid.size();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    @Override
    public void flush() {
        rwLock.writeLock().lock();
        try {
            persistToFile();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public void close() {
        flush();
    }

    private boolean persistToFile() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            log(Level.SEVERE, "Failed to create directory structure for: " + parent.getAbsolutePath(), null);
            return false;
        }

        File tempFile = new File(parent != null ? parent : new File("."), file.getName() + ".tmp");
        try {
            configuration.save(tempFile);
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            log(Level.SEVERE, "Failed to atomically persist YAML accounts file: " + file.getAbsolutePath(), e);
            if (tempFile.exists() && !tempFile.delete()) {
                tempFile.deleteOnExit();
            }
            return false;
        }
    }

    private void saveConfigurationDirect(YamlConfiguration config, File targetFile) {
        try {
            config.save(targetFile);
        } catch (IOException e) {
            log(Level.SEVERE, "Failed to write YAML configuration to: " + targetFile.getAbsolutePath(), e);
        }
    }

    private void log(Level level, String message, Throwable throwable) {
        if (logger != null) {
            if (throwable != null) {
                logger.log(level, message, throwable);
            } else {
                logger.log(level, message);
            }
        }
    }

}
