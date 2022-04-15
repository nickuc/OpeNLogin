/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.util;

import com.nickuc.openlogin.common.OpenLogin;
import lombok.Cleanup;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.net.URLDecoder;

public class FileUtils {

    @CheckReturnValue
    public static InputStream getResourceAsStream(String path) {
        return FileUtils.class.getResourceAsStream("/" + path);
    }

    @CheckReturnValue
    public static boolean copyFromJar(String copy, File paste) {
        try {
            File parent = paste.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                return false;
            }
            if (!paste.exists() && !paste.createNewFile()) {
                return false;
            }

            InputStream inputStream = getResourceAsStream(copy);
            if (inputStream != null && inputStream.available() > 0) {
                @Cleanup OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(paste));
                byte[] buffer = new byte[4096];
                int size;
                while ((size = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, size);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File getSelfJarFile() throws UnsupportedEncodingException {
        return new File(URLDecoder.decode(OpenLogin.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath(), "UTF-8"));
    }
}
