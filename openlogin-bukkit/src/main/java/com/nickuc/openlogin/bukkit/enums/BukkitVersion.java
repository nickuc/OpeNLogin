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

package com.nickuc.openlogin.bukkit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;

@AllArgsConstructor
@Getter
public enum BukkitVersion {

    v1_19("1.19"),
    v1_18("1.18"),
    v1_17("1.17"),
    v1_16("1.16"),
    v1_15("1.15"),
    v1_14("1.14"),
    v1_13("1.13"),
    v1_12("1.12"),
    v1_11("1.11"),
    v1_10("1.10"),
    v1_9("1.9"),
    v1_8("1.8"),
    v1_7("1.7"),
    v1_6("1.6"),
    v1_5("1.5");

    private final String version;

    private static final BukkitVersion BUKKIT_VERSION;

    static {
        BukkitVersion detectedVersion = null;
        String bukkitVer = Bukkit.getVersion();
        BukkitVersion[] values = values();

        for (BukkitVersion bukkitVersion : values) {
            if (bukkitVersion.version != null && bukkitVer.contains("MC: " + bukkitVersion.version)) {
                detectedVersion = bukkitVersion;
                break;
            }
        }
        if (detectedVersion == null) {
            detectedVersion = values[0];
        }

        BUKKIT_VERSION = detectedVersion;
    }

    public boolean isNewerOrEqual(BukkitVersion other) {
        return ordinal() <= other.ordinal();
    }

    public boolean isOlderOrEqual(BukkitVersion other) {
        return ordinal() >= other.ordinal();
    }

    public static BukkitVersion getVersion() {
        return BUKKIT_VERSION;
    }
}