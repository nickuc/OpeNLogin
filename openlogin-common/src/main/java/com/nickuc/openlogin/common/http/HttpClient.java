/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.http;

import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(chain = true, fluent = true)
public final class HttpClient {

    public static HttpClient DEFAULT = new HttpClient(
            "OpeNLogin (+https://github.com/nickuc/OpeNLogin)",
            10000,
            16000
    );

    @NonNull
    private final String userAgent;
    private final int connectTimeout, readTimeout;

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
    }

    public String get(String url) throws IOException {
        try {
            HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
            buildHttp(http, "GET");

            boolean redirect;
            int loopCount = 0;
            do {
                int status = http.getResponseCode();
                redirect = status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;

                if (redirect) {
                    loopCount++;
                    if (loopCount >= 20) {
                        throw new IOException("Too many redirects!");
                    }
                    String newUrl = http.getHeaderField("Location");
                    http = (HttpURLConnection) new URL(newUrl).openConnection();
                    buildHttp(http, "GET");
                }

            } while (redirect);

            @Cleanup InputStream inputStream = http.getInputStream();
            @Cleanup BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            int read;
            while ((read = bufferedReader.read()) != -1) {
                response.append((char) read);
            }

            return response.toString();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("[GET] The connection took too long to be answered.");
        }
    }

    @Nullable
    public AsyncDownloadResult download(String url, File output) throws IOException {
        try {
            if (output.exists() && !output.delete()) {
                return null;
            }

            HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
            buildHttp(http, "GET");

            boolean redirect;
            int loopCount = 0;
            do {
                int status = http.getResponseCode();
                redirect = status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER;

                if (redirect) {
                    loopCount++;
                    if (loopCount >= 20) {
                        throw new IOException("Too many redirects!");
                    }
                    String newUrl = http.getHeaderField("Location");
                    http = (HttpURLConnection) new URL(newUrl).openConnection();
                    buildHttp(http, "GET");
                }

            } while (redirect);

            return new AsyncDownloadResult(http, output);
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException("[GET] The connection took too long to be answered.");
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class AsyncDownloadResult {

        private final long contentLength;
        private final File output;
        private final HttpURLConnection http;
        private double downloaded;

        public AsyncDownloadResult(HttpURLConnection http, File output) {
            this.contentLength = http.getContentLengthLong();
            this.output = output;
            this.http = http;
        }

        public boolean startDownload() throws IOException {
            @Cleanup BufferedInputStream bin = new BufferedInputStream(http.getInputStream());
            @Cleanup FileOutputStream fos = new FileOutputStream(output);
            @Cleanup BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);

            byte[] data = new byte[1024];
            int size;
            while ((size = bin.read(data, 0, data.length)) >= 0) {
                bout.write(data, 0, size);
                downloaded += size;
            }
            return output.exists() && output.length() > 0;
        }
    }

}
