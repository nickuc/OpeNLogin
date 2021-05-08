/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.security.filter;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class ConsoleFilter implements Filter {

    @Override
    public boolean isLoggable(LogRecord logRecord) {
        if (logRecord == null || logRecord.getMessage() == null || LoggerFilterManager.isOpenLoginCommand(logRecord.getMessage())) {
            return true;
        }

        logRecord.setMessage("[OpeNLogin] This content has been filtered.");
        return false;
    }
}
