/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 - 2026 - OpenLogin Contributors
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

import com.nickuc.openlogin.bukkit.ui.title.TitleAPI;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SpigotTitleImpl implements TitleAPI {

    public SpigotTitleImpl() throws ReflectiveOperationException {
        Objects.requireNonNull(Player.class.getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class));
        Objects.requireNonNull(Player.class.getMethod("resetTitle"));
    }

    @Override
    public void send(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title.isEmpty() && subtitle.isEmpty()) {
            reset(player);
            return;
        }

        if (title.isEmpty()) title = "§r";
        if (subtitle.isEmpty()) subtitle = "§r";

        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void reset(Player player) {
        player.resetTitle();
    }
}
