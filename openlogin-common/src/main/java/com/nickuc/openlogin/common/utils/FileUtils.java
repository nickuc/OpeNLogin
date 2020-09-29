/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.utils;

import lombok.Cleanup;

import javax.annotation.CheckReturnValue;
import java.io.*;

public class FileUtils {

    @CheckReturnValue
    public static InputStream getResourceAsStream(String path) {
        return FileUtils.class.getResourceAsStream("/" + path);
    }

    public static boolean copyFromJar(String copy, File paste) {
        try {
            File parent = paste.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (!paste.exists()) {
                paste.createNewFile();
            }
            @Cleanup InputStream is = getResourceAsStream(copy);
            if (is == null || is.available() <= 0) return false;

            @Cleanup OutputStream os = new BufferedOutputStream(new FileOutputStream(paste));
            byte[] buffer = new byte[4096];
            int size;
            while ((size = is.read(buffer)) != -1) {
                os.write(buffer, 0, size);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
