/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.settings;

import com.nickuc.openlogin.common.model.Title;
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

    @Getter private final String key;

    Messages(String key) {
        this.key = "Messages." + key;
    }

    /**
     * Add a message to settings map
     *
     * @param message the message to define
     * @param value the message value
     */
    public static void define(@NonNull Messages message, Object value) {
        if (value instanceof String) {
            value = translateAlternateColorCodes('&', (String) value);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            if (!list.isEmpty() && list.get(0) instanceof String) {
                list.replaceAll(a -> translateAlternateColorCodes('&', (String) a));
            }
        }
        Settings.SETTINGS.put(message.key, value);
    }

    private static final String CHARACTERS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final char COLOR_CODE = 167;

    /**
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar The alternate color code character to replace. Ex: {@literal &}
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the Messages.COLOR_CODE color code character.
     */
    public static String translateAlternateColorCodes(char altColorChar, @NonNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && CHARACTERS.indexOf(b[i+1]) > -1) {
                b[i] = COLOR_CODE;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

    public String asString() {
        Object obj = Settings.SETTINGS.get(key);
        return (String) (!(obj instanceof String) ? "§cMissing message: " + key : obj);
    }

    public Title asTitle() {
        Object obj = Settings.SETTINGS.get(key);
        return (Title) (!(obj instanceof Title) ? Title.EMPTY : obj);
    }

}
