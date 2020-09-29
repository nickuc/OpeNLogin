/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.settings;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
public enum Settings {

    LANGUAGE_FILE("languageFile", "messages_en.yml");

    static final HashMap<String, Object> SETTINGS = new HashMap<>();

    @Getter private final String key;
    private final Object def;

    /**
     * Add a setting to map
     *
     * @param setting the setting to define
     * @param value the setting value
     */
    public static void define(@NonNull Settings setting, Object value) {
        SETTINGS.put(setting.key, value);
    }

    /**
     * Clears the settings map
     */
    public static void clear() {
        SETTINGS.clear();
    }

    public String asString() {
        if (def != null && !(def instanceof String)) {
            throw new ClassCastException("Setting " + key + " is not a string!");
        }
        Object obj = SETTINGS.get(key);
        return (String) (!(obj instanceof String) ? def : obj);
    }

}
