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

package com.nickuc.openlogin.common.security.filter;

import com.nickuc.openlogin.common.util.ClassUtils;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LoggerFilterManager {

    private static final Set<String> OPEN_LOGIN_COMMANDS = new HashSet<>();
    private static final String ISSUED_COMMAND = "issued server command";

    /**
     * Adds an OpenLogin command to filter.
     *
     * @param command the command to add
     */
    public static void addOpenLoginCommand(@NonNull String command) {
        if (!command.startsWith("/")) {
            throw new IllegalArgumentException("The provided value is not a command! " + command);
        }
        OPEN_LOGIN_COMMANDS.add(command);
    }

    /**
     * Checks if the provided message is an OpeNLogin command.
     *
     * @param fullMessage the message to analyze
     * @return true if the message contains an OpeNLogin command
     */
    public static boolean isOpenLoginCommand(@NonNull String fullMessage) {
        String toLowerCase = fullMessage.toLowerCase();
        return toLowerCase.contains(ISSUED_COMMAND) && OPEN_LOGIN_COMMANDS.stream().anyMatch(toLowerCase::contains);
    }

    public static void setup(Logger logger) {
        if (ClassUtils.exists("org.apache.logging.log4j.core.filter.AbstractFilter")) {
            Log4JFilter.setupFilter();
        } else {
            ConsoleFilter filter = new ConsoleFilter();
            logger.setFilter(filter);
            if (ClassUtils.exists("org.bukkit.Bukkit")) {
                Logger.getLogger("Minecraft").setFilter(filter);
            }
        }
    }

}
