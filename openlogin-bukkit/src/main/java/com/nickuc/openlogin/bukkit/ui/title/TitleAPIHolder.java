/*
 * The MIT License (MIT)
 *
 * A practical, secure and friendly authentication plugin.
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

package com.nickuc.openlogin.bukkit.ui.title;

import com.nickuc.openlogin.bukkit.ui.title.impl.PacketTitleImpl;
import com.nickuc.openlogin.bukkit.ui.title.impl.SpigotTitleImpl;
import com.nickuc.openlogin.common.model.Title;
import org.bukkit.entity.Player;

public class TitleAPIHolder {

    private static TitleAPI api;

    static {
        try {
            api = new SpigotTitleImpl();
        } catch (Throwable ignored) {
            try {
                api = new PacketTitleImpl();
            } catch (Throwable ignored2) {
                api = new TitleAPI() {
                    @Override
                    public void send(Player player, Title title) {
                    }

                    @Override
                    public void reset(Player player) {
                    }
                };
            }
        }
    }

    static TitleAPI getApi() {
        return api;
    }
}
