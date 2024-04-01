/*
 * The MIT License (MIT)
 *
 * A practical, secure and friendly authentication plugin.
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

package com.nickuc.openlogin.common.database;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public class SQLite implements Database {

    @NonNull
    private final File file;
    private Connection connection;

    /**
     * Open the connection.
     *
     * @throws SQLException on failure
     */
    public void openConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Failed to find class 'org.sqlite.JDBC'", e);
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new RuntimeException("Failed to create '" + parentFile + "'");
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.toString());
        }
    }

    /**
     * Close the connection.
     *
     * @throws SQLException on failure
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    /**
     * Executes an update.
     *
     * @param command the command to be executed
     * @param args    the command arguments
     * @throws SQLException on failure
     */
    public void update(String command, Object... args) throws SQLException {
        openConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(command)) {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Failed to execute update statement: '" + command + "'", e);
        }
    }

    /**
     * Executes a query.
     *
     * @param command the command to be executed
     * @param args    the command arguments
     * @return returns an instance of {@link com.nickuc.openlogin.common.database.Database.Query}
     * @throws SQLException on failure
     */
    public Query query(String command, Object... args) throws SQLException {
        openConnection();
        return new Query(connection, command, args);
    }
}
