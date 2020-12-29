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

    LANGUAGE_FILE(
            "languageFile",
            "messages_en.yml"
    ),
    PASSWORD_SMALL(
            "Security.password.small",
            5
    ),
    PASSWORD_LARGE(
            "Security.password.large",
            15
    );

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
        return get(String.class);
    }

    public int asInt() {
        return get(Integer.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(@NonNull Class<T> clasz) {
        if (def != null && !clasz.isAssignableFrom(def.getClass())) {
            throw new ClassCastException("Setting " + key + " is not assignable to " + clasz.getCanonicalName() + "!");
        }
        Object obj = SETTINGS.get(key);
        return (T) (obj == null || !clasz.isAssignableFrom(obj.getClass()) ? def : obj);
    }

}
