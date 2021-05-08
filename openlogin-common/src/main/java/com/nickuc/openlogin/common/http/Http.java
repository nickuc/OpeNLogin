/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.http;

import lombok.*;
import lombok.experimental.Accessors;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Getter
@Accessors(chain = true, fluent = true)
public final class Http {

    @NonNull
    private final String url;
    @Setter
    private String userAgent = "OpeNLogin";
    private String result, cookie = "";
    @Setter
    private int connectTimeout = 10000, readTimeout = 16000;
    private long contentLength;
    private double downloaded;
    private boolean finished;

    public Http addCookie(String name, String value) {
        this.cookie += name + "=" + value + ";";
        return this;
    }

    private void buildHttp(HttpURLConnection http, String requestMethod) throws IllegalArgumentException {
        http.setInstanceFollowRedirects(false);
        if (requestMethod != null && !requestMethod.isEmpty()) {
            try {
                http.setRequestMethod(requestMethod);
            } catch (ProtocolException e) {
                throw new IllegalArgumentException("Method '" + requestMethod + "' does not exists!", e);
            }
        }
        http.setRequestProperty("User-Agent", userAgent);
        http.setConnectTimeout(connectTimeout);
        http.setReadTimeout(readTimeout);
        if (!cookie.isEmpty()) {
            http.setRequestProperty("Cookie", cookie);
        }
    }

    public Http get() throws IOException {
        try {
            HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
            buildHttp(http, "GET");

            boolean redirect;
            do {
                int status = http.getResponseCode();
                redirect = status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;

                if (redirect) {
                    String newUrl = http.getHeaderField("Location");
                    http = (HttpURLConnection) new URL(newUrl).openConnection();
                    buildHttp(http, "GET");
                }

            } while (redirect);

            @Cleanup InputStream inputStream = http.getInputStream();
            @Cleanup BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                response.append((char) cp);
            }
            result = response.toString();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("[GET] The connection took too long to be answered.");
        }
        return this;
    }

    public boolean download(File output) throws IOException {
        try {
            HttpURLConnection http = (HttpURLConnection) new URL(this.url).openConnection();
            buildHttp(http, "GET");

            boolean redirect;
            do {
                int status = http.getResponseCode();
                redirect = status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;

                if (redirect) {
                    String newUrl = http.getHeaderField("Location");
                    http = (HttpURLConnection) new URL(newUrl).openConnection();
                    buildHttp(http, "GET");
                }

            } while (redirect);

            long completeFileSize = contentLength = http.getContentLengthLong();
            if (output.exists()) {
                output.delete();
            }

            @Cleanup BufferedInputStream bin = new BufferedInputStream(http.getInputStream());
            @Cleanup FileOutputStream fos = new FileOutputStream(output);
            @Cleanup BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

            byte[] data = new byte[1024];
            int cp;
            while ((cp = bin.read(data, 0, 1024)) >= 0) {
                bout.write(data, 0, cp);
                downloaded += cp;
            }
            if (output.exists() && output.length() > 0) {
                return true;
            }
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("[GET] The connection took too long to be answered.");
        } finally {
            finished = true;
        }
        return false;
    }

}
