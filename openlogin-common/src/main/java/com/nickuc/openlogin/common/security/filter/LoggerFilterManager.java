/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.security.filter;

import com.nickuc.openlogin.common.utils.ClassUtils;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class LoggerFilterManager {

    private static final Set<String> OPEN_LOGIN_COMMANDS = new HashSet<>();
    private static final String ISSUED_COMMAND = "issued server command";

    /**
     * Adds an OpenLogin command to filter
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
     * Checks if the provided message is an OpeNLogin command
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
