/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.security.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public final class Log4JFilter extends AbstractFilter {

    public static void setupFilter() {
        Logger logger = (Logger) LogManager.getRootLogger();
        logger.addFilter(new Log4JFilter());
    }

    private Filter.Result validateMessage(Message message) {
        return message != null ? validateMessage(message.getFormattedMessage()) : Filter.Result.NEUTRAL;
    }

    private Filter.Result validateMessage(String message) {
        return message != null && LoggerFilterManager.isOpenLoginCommand(message) ? Filter.Result.DENY : Filter.Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent event) {
        return validateMessage(event != null ? event.getMessage() : null);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return validateMessage(msg);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return validateMessage(msg != null ? msg.toString() : null);
    }


}
