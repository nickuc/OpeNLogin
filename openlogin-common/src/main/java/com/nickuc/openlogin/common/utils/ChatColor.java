/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.utils;

import lombok.NonNull;

public class ChatColor {

    private static final String CHARACTERS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private static final char COLOR_CODE = 167;

    /**
     * This method was taken from Bukkit-API (class: org.bukkit.ChatColor)
     * <p>
     * Translates a string using an alternate color code character into a
     * string that uses the internal ChatColor.COLOR_CODE color code
     * character. The alternate color code character will only be replaced if
     * it is immediately followed by 0-9, A-F, a-f, K-O, k-o, R or r.
     *
     * @param altColorChar    The alternate color code character to replace. Ex: {@literal &}
     * @param textToTranslate Text containing the alternate color code character.
     * @return Text containing the Messages.COLOR_CODE color code character.
     */
    public static String translateAlternateColorCodes(char altColorChar, @NonNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && CHARACTERS.indexOf(b[i + 1]) > -1) {
                b[i] = COLOR_CODE;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

}
