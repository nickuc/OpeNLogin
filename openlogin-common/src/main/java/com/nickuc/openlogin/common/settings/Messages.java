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

import com.nickuc.openlogin.common.model.Title;
import com.nickuc.openlogin.common.util.ChatColor;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public enum Messages {

    // title messages
    TITLE_BEFORE_LOGIN("Title.before-login"),
    TITLE_BEFORE_REGISTER("Title.before-register"),
    TITLE_AFTER_LOGIN("Title.after-login"),
    TITLE_AFTER_REGISTER("Title.after-register"),

    // delay kick
    DELAY_KICK_LOGIN("delay-kick.login-kick"),
    DELAY_KICK_REGISTER("delay-kick.register-kick"),

    // successful operations
    PASSWORD_CHANGED("successful-operations.password-changed"),
    SUCCESSFUL_LOGIN("successful-operations.successful-login"),
    SUCCESSFUL_REGISTER("successful-operations.successful-register"),
    UNREGISTER_KICK("successful-operations.unregister-kick"),

    // kick messages
    NICK_ALREADY_REGISTERED("kick-messages.nick-already-registered"),
    FAILED_MANY_TIMES("kick-messages.failed-many-times"),
    INCORRECT_PASSWORD("kick-messages.incorrect-password"),
    INVALID_NICKNAME("kick-messages.invalid-nickname"),

    // error messages
    ALREADY_LOGIN("error-messages.already-login"),
    ALREADY_REGISTERED("error-messages.already-registered"),
    NOT_REGISTERED("error-messages.not-registered"),
    PASSWORDS_DONT_MATCH("error-messages.passwords-dont-match"),
    PASSWORD_SAME_AS_OLD("error-messages.password-same-as-old"),
    PASSWORD_TOO_LARGE("error-messages.password-too-large"),
    PASSWORD_TOO_SMALL("error-messages.password-too-small"),
    INSUFFICIENT_PERMISSIONS("error-messages.insufficient-permissions"),
    ALREADY_ONLINE("error-messages.already-online"),
    PLAYER_COMMAND_USAGE("error-messages.player-command-usage"),
    PLUGIN_RELOAD_MESSAGE("error-messages.plugin-reload-message"),
    DATABASE_ERROR("error-messages.database-error"),

    // other messages
    MESSAGE_LOGIN("other-messages.message-login"),
    MESSAGE_REGISTER("other-messages.message-register"),
    MESSAGE_CHANGEPASSWORD("other-messages.message-changepassword"),
    MESSAGE_UNREGISTER("other-messages.message-unregister"),
    ;

    @Getter
    private final String key;

    Messages(String key) {
        this.key = "Messages." + key;
    }

    /**
     * Add a message to settings map
     *
     * @param message the message to define
     * @param value   the message value
     */
    public static void define(@NonNull Messages message, Object value) {
        if (value instanceof String) {
            value = ChatColor.translateAlternateColorCodes('&', (String) value);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            if (!list.isEmpty() && list.get(0) instanceof String) {
                list.replaceAll(a -> ChatColor.translateAlternateColorCodes('&', (String) a));
            }
        }
        Settings.SETTINGS.put(message.key, value);
    }

    public String asString() {
        return asString("§cMissing message: " + key);
    }

    public String asString(@NonNull String def) {
        Object obj = Settings.SETTINGS.get(key);
        return (String) (!(obj instanceof String) ? def : obj);
    }

    public Title asTitle() {
        Object obj = Settings.SETTINGS.get(key);
        return (Title) (!(obj instanceof Title) ? Title.EMPTY : obj);
    }

}
