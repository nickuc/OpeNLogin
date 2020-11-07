/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.model;

import com.nickuc.openlogin.common.utils.ChatColor;

public class Title {

    public static final Title EMPTY = new Title("", "", 0, 0, 0);

    public final String title, subtitle;
    public final int start, duration, end;

    public Title(String title, String subtitle, int start, int duration, int end) {
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
        this.start = start;
        this.duration = duration;
        this.end = end;
    }

}
