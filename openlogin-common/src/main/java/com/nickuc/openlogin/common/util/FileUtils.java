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
