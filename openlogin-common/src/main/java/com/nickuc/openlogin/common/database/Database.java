/*
 * This file is part of a NickUC project
 *
 * Copyright (c) NickUC <nickuc.com>
 * https://github.com/nickuc
 */

package com.nickuc.openlogin.common.database;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public interface Database {

    /**
     * Open the connection
     *
     * @throws SQLException on failure
     */
    void openConnection() throws SQLException;

    /**
     * Close the connection
     *
     * @throws SQLException on failure
     */
    void closeConnection() throws SQLException;

    /**
     * Execute a update
     *
     * @param command the command to be executed
     * @throws SQLException on failure
     */
    void update(String command) throws SQLException;

    /**
     * Executes a query
     *
     * @param command the command to be executed
     * @return an instance of {@link Query}
     * @throws SQLException on failure
     */
    Query query(String command) throws SQLException;

    // Query class
    class Query implements Closeable {

        private final Statement statement;
        public final ResultSet resultSet;

        public Query(Connection connection, String command) throws SQLException {
            try {
                statement = connection.createStatement();
                resultSet = statement.executeQuery(command);
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
            } catch (SQLException e) {
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
            }
        }

    }

}
