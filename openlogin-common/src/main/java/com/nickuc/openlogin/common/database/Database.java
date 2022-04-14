/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.database;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface Database {

    /**
     * Open the connection.
     *
     * @throws SQLException on failure
     */
    void openConnection() throws SQLException;

    /**
     * Close the connection.
     *
     * @throws SQLException on failure
     */
    void closeConnection() throws SQLException;

    /**
     * Executes an update.
     *
     * @param command the command to be executed
     * @param args    the command arguments
     * @throws SQLException on failure
     */
    void update(String command, Object... args) throws SQLException;

    /**
     * Executes a query.
     *
     * @param command the command to be executed
     * @param args    the command arguments
     * @return an instance of {@link Query}
     * @throws SQLException on failure
     */
    Query query(String command, Object... args) throws SQLException;

    // Query class
    class Query implements Closeable {

        private final PreparedStatement preparedStatement;
        public final ResultSet resultSet;

        public Query(Connection connection, String command, Object... args) throws SQLException {
            try {
                preparedStatement = connection.prepareStatement(command);
                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);
                }
                resultSet = preparedStatement.executeQuery();
            } catch (SQLException e) {
                close();
                throw new SQLException("Failed to execute query statement: '" + command + "'", e);
            }
        }

        @Override
        public void close() {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException ignored) {
            }
        }

    }

}
