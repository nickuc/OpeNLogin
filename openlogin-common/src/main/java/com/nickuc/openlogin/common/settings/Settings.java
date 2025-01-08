/*
 * The MIT License (MIT)
 *
 * Copyright © 2025 - OpenLogin Contributors
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
    ALLOW_ADVERTISING(
            "allow-advertising",
            true
    ),
    TIME_TO_LOGIN(
            "Security.time-to-login",
            45
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

    @Getter
    private final String key;
    private final Object def;

    /**
     * Add a setting to map.
     *
     * @param setting the setting to define
     * @param value   the setting value
     */
    public static void define(@NonNull Settings setting, Object value) {
        SETTINGS.put(setting.key, value);
    }

    /**
     * Clears the settings map.
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
