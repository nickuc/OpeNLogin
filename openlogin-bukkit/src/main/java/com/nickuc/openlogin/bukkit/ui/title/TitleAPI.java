/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.bukkit.ui.title;

import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

public interface TitleAPI {

    static TitleAPI getApi() {
        return TitleAPIHolder.getApi();
    }

    void send(Player player, Title title);

    void reset(Player player);

}
