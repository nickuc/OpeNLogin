/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.model;

public class Title {

    public static final Title EMPTY = new Title("", "", 0, 0, 0);

    public final String title, subtitle;
    public final int start, duration, end;

    public Title(String title, String subtitle, int start, int duration, int end) {
        this.title = title.replace('&', '§');
        this.subtitle = subtitle.replace('&', '§');
        this.start = start;
        this.duration = duration;
        this.end = end;
    }

}
