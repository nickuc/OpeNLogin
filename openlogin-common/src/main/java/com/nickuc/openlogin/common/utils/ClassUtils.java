/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.utils;

public class ClassUtils {

    /**
     * Checks if a class exists
     *
     * @param clasz the class to check
     * @return true if exists
     */
    public static boolean exists(String clasz) {
        try {
            Class.forName(clasz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
