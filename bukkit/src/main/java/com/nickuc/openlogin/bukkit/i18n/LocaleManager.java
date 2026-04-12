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

package com.nickuc.openlogin.bukkit.i18n;

import com.nickuc.openlogin.common.model.Title;
import com.nickuc.openlogin.common.settings.Messages;
import com.nickuc.openlogin.common.util.ChatColor;
import com.nickuc.openlogin.common.util.FileUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages locale-based translations for players.
 */
public final class LocaleManager {

    private static final String LANG_RESOURCE_PATH = "com/nickuc/openlogin/config/lang/";

    /**
     * Bundled language files shipped with the plugin.
     */
    private static final String[] BUNDLED = {
            "messages_ar.yml", "messages_br.yml", "messages_cn.yml", "messages_cz.yml",
            "messages_de.yml", "messages_en.yml", "messages_es.yml", "messages_fr.yml",
            "messages_hr.yml", "messages_hu.yml", "messages_it.yml", "messages_lt.yml",
            "messages_pl.yml", "messages_pt.yml", "messages_ru.yml", "messages_sr.yml",
            "messages_tr.yml", "messages_ua.yml"
    };

    /**
     * Cache of loaded locale configurations.
     */
    private final Map<String, YamlConfiguration> localeCache = new ConcurrentHashMap<>();

    /**
     * Enables per-player locale resolution.
     */
    @Setter
    @Getter
    private boolean perPlayerLocaleEnabled = true;

    /**
     * Default fallback locale.
     */
    private String defaultLocale = "en";

    /**
     * Creates LocaleManager, copies bundled files, and loads all locales.
     */
    public LocaleManager(File langFolder, String defaultLangFile) {
        this.defaultLocale = normalize(defaultLangFile);
        copyBundledFiles(langFolder);
        loadLocales(langFolder);
        defineLegacyMessages(langFolder, defaultLangFile);
    }

    /**
     * Copies bundled language files from the JAR into the lang folder.
     */
    private void copyBundledFiles(File langFolder) {
        if (!langFolder.exists()) langFolder.mkdirs();

        for (String name : BUNDLED) {
            File f = new File(langFolder, name);
            if (!f.exists()) {
                FileUtils.copyFromJar(LANG_RESOURCE_PATH + name, f);
            }
        }
    }

    /**
     * Loads all YAML locale files into memory.
     */
    private void loadLocales(File langFolder) {
        if (!langFolder.exists()) return;

        File[] files = langFolder.listFiles((d, n) -> n.endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            localeCache.put(normalize(f.getName()),
                    YamlConfiguration.loadConfiguration(f));
        }
    }

    /**
     * Initializes legacy message system using the default locale file.
     */
    private void defineLegacyMessages(File langFolder, String langFile) {
        File f = new File(langFolder, langFile);
        if (!f.exists()) return;

        YamlConfiguration c = YamlConfiguration.loadConfiguration(f);

        for (Messages m : Messages.values()) {
            String p = m.getKey();

            if (p.startsWith("Messages.Title")
                    && c.isSet(p + ".title")
                    && c.isSet(p + ".subtitle")) {

                Messages.define(m, new Title(
                        c.getString(p + ".title"),
                        c.getString(p + ".subtitle"),
                        c.getInt(p + ".delays.start", 0),
                        c.getInt(p + ".delays.duration", 60),
                        c.getInt(p + ".delays.end", 6)
                ));

            } else if (c.isSet(p)) {
                Messages.define(m, c.get(p));
            }
        }
    }

    /**
     * Normalizes locale or file name into a standard key.
     */
    private String normalize(String name) {
        name = name.replace(".yml", "");
        if (name.startsWith("messages_")) {
            name = name.substring(9);
        }
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets a localized message for a player by key.
     */
    public String get(Player player, String key) {
        String locale = resolveLocale(player);

        String msg = resolve(locale, key);

        if (msg == null) {
            int i = locale.indexOf('_');
            if (i > 0) {
                msg = resolve(locale.substring(0, i), key);
            }
        }

        return msg != null ? msg : resolve(defaultLocale, key);
    }

    /**
     * Gets a localized message using a Messages enum.
     */
    public String get(Player player, Messages m) {
        return get(player, m.getKey());
    }

    /**
     * Gets and formats a localized message with arguments.
     */
    public String get(Player player, Messages m, Object... args) {
        String msg = get(player, m.getKey());

        if (args.length > 0 && msg != null) {
            try {
                return String.format(msg, args);
            } catch (Exception ignored) {
            }
        }

        return msg;
    }

    /**
     * Gets a localized title object.
     */
    public Title getTitle(Player player, String key, Title fallback) {
        String t = get(player, key + ".title");

        if (t == null || t.equals(key + ".title")) {
            return fallback;
        }

        return new Title(
                t,
                get(player, key + ".subtitle"),
                getInt(player, key + ".delays.start", 0),
                getInt(player, key + ".delays.duration", 60),
                getInt(player, key + ".delays.end", 6)
        );
    }

    /**
     * Resolves the effective locale for a player.
     */
    private String resolveLocale(Player player) {
        if (!perPlayerLocaleEnabled || player == null) {
            return defaultLocale;
        }

        String locale = rawLocale(player);
        if (locale == null || locale.trim().isEmpty()) {
            return defaultLocale;
        }

        return locale.toLowerCase(Locale.ROOT);
    }

    /**
     * Resolves a message from a specific locale.
     */
    private String resolve(String locale, String key) {
        YamlConfiguration c = localeCache.get(locale);
        if (c == null) return null;

        String v = c.getString(key);
        return v != null
                ? ChatColor.translateAlternateColorCodes('&', v)
                : null;
    }

    /**
     * Parses an integer value from a localized string.
     */
    private int getInt(Player player, String key, int def) {
        String v = get(player, key);

        if (v == null || v.equals(key)) return def;

        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Retrieves the raw locale from the player.
     */
    private String rawLocale(Player player) {
        try {
            return player.getLocale();
        } catch (NoSuchMethodError ignored) {
            try {
                return player.spigot().getLocale();
            } catch (NoSuchMethodError ignored2) {
                return null;
            }
        }
    }
}