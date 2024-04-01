/*
 * The MIT License (MIT)
 *
 * Copyright © 2024 - OpenLogin Contributors
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

package com.nickuc.openlogin.bukkit.ui.title.impl;

import com.nickuc.openlogin.bukkit.reflection.BukkitReflection;
import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

public class SpigotTitleImpl implements TitleAPI {

    public SpigotTitleImpl() throws ReflectiveOperationException {
        BukkitReflection.getMethod(Player.class, "sendTitle", String.class, String.class, int.class, int.class, int.class);
        BukkitReflection.getMethod(Player.class, "resetTitle");
    }

    @Override
    public void send(Player player, Title title) {
        if (title.title.isEmpty() && title.subtitle.isEmpty()) {
            reset(player);
        } else {
            String finalTitle = "§r";
            if (!title.title.isEmpty()) {
                finalTitle = title.title;
            }
            String finalSubtitle = "§r";
            if (!title.subtitle.isEmpty()) {
                finalSubtitle = title.subtitle;
            }
            player.sendTitle(finalTitle, finalSubtitle, title.start, title.duration, title.end);
        }
    }

    @Override
    public void reset(Player player) {
        player.resetTitle();
    }
}
