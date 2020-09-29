/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common;

import com.nickuc.openlogin.common.api.OpenLoginAPI;
import lombok.NonNull;

public class OpenLogin {

    private static OpenLoginAPI api;

    public static OpenLoginAPI getApi() {
        if (api == null) {
            throw new IllegalStateException("The api instance has not yet been defined.");
        }
        return api;
    }

    public static void setApi(@NonNull OpenLoginAPI api) {
        if (OpenLogin.api != null) {
            throw new IllegalStateException("The api instance has already been defined.");
        }
        OpenLogin.api = api;
    }
}
