/*
 * Copyright 2022 berni3.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.h2tools.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;

/**
 *
 * @author berni3
 */
public class JdbcSql implements AutoCloseable {

    public static final ConsumerThrowingSQLException<Integer> EMPTY_INTEGER_CONSUMER = null;
    public static final ConsumerThrowingSQLException<ResultSetMetaData> EMPTY_RESULTSETMETADATA_CONSUMER = null;
    public static final List<Object> EMPTY_PARAMS = null;

    final IConnectionFactory connectionFactory;
    Optional<Connection> connectionOptional;

    JdbcSql(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.connectionOptional = Optional.empty();
    }

    public static interface IConnectionFactory {

        Connection createConnection() throws SQLException;
    }

    public static class ConnectionFactoryWithMap implements IConnectionFactory {

        final Map<String, Object> m;

        public ConnectionFactoryWithMap(Map<String, Object> m) {
            this.m = m;
        }

        Optional<String> extractUrl() {
            final Optional<String> urlOptional = Optional.ofNullable((String) m.getOrDefault("url", null));
            return urlOptional;
        }

        Optional<String[]> extractUserPassword() {
            final Optional<String[]> userPasswordOptional;
            final String user = (String) m.getOrDefault("user", null);
            final String password = (String) m.getOrDefault("password", null);

            if (user != null && password != null) {
                userPasswordOptional = Optional.of(new String[]{user, password});
            } else {
                userPasswordOptional = Optional.empty();
            }
            return userPasswordOptional;
        }

        Optional<Properties> extractProperties() {
            final Optional<Properties> propertiesOptional = Optional.ofNullable((Properties) m.getOrDefault("properties", null));
            return propertiesOptional;
        }

        @Override
        public Connection createConnection() throws SQLException {
            final Connection connection;

            final Optional<String> urlOptional = extractUrl();
            final Optional<String[]> userPasswordOptional = extractUserPassword();
            final Optional<Properties> propertiesOptional = extractProperties();
            if (urlOptional.isPresent()) {
                if (propertiesOptional.isPresent()) {
                    connection = DriverManager.getConnection(urlOptional.get(), propertiesOptional.get());
                } else if (userPasswordOptional.isPresent()) {
                    connection = DriverManager.getConnection(urlOptional.get(), userPasswordOptional.get()[0], userPasswordOptional.get()[1]);
                } else {
                    connection = DriverManager.getConnection(urlOptional.get());
                }
            } else {
                throw new SQLException("Cannot create connection from " + m);
            }
            return connection;
        }
    }

    public static class ConnectionFactoryWithDataSource implements IConnectionFactory {

        final DataSource dataSource;

        public ConnectionFactoryWithDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Connection createConnection() throws SQLException {
            Connection connection = this.dataSource.getConnection();
            return connection;
        }
    }

    public static JdbcSql newInstance(IConnectionFactory iconnectionFactory) {
        final JdbcSql javaSQL = new JdbcSql(iconnectionFactory);
        return javaSQL;
    }

    public static void withInstance(IConnectionFactory iconnectionFactory,
            ConsumerThrowingSQLException<JdbcSql> consumer) throws SQLException {
        try (JdbcSql jdbcSql = newInstance(iconnectionFactory)) {
            consumer.accept(jdbcSql);
        }
    }

    public void withConnection(ConsumerThrowingSQLException<Connection> consumer) throws SQLException {
        try (Connection connection = this._createOrGetConnection()) {
            consumer.accept(connection);
        }
    }

    public void withTransaction(ConsumerThrowingSQLException<Connection> consumer) throws SQLException {
        try (Connection connection = this._createOrGetConnection()) {
            try {
                connection.setAutoCommit(false);
                consumer.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        }
    }

    public void eachRow(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<ResultSetMetaData> metaClosure,
            int offset, int maxRows,
            ConsumerThrowingSQLException<ResultSet> rowClosure) throws SQLException {

        final Connection connection = _createOrGetConnection();
        try (Statement preparedStatement = _createTheStatement(connection, sql, params)) {
            try (ResultSet results = preparedStatement.executeQuery(sql)) {
                if (metaClosure != null) {
                    metaClosure.accept(results.getMetaData());
                }
                final boolean cursorAtRow = _moveCursor(results, offset);
                if (!cursorAtRow) {
                    return;
                }

                int i = 0;
                while ((maxRows <= 0 || i++ < maxRows) && results.next()) {
                    rowClosure.accept(results);
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public void executeQuery(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<ResultSet> resultSetConsumer) throws SQLException {
        final Connection connection = _createOrGetConnection();
        try (PreparedStatement preparedStatement = _createPreparedStatement(connection, sql, params)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSetConsumer.accept(resultSet);
            } catch (SQLException e) {
                throw e;
            }
        }
    }

    public int executeUpdate(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<Integer> resultSetConsumer) throws SQLException {
        final Connection connection = _createOrGetConnection();
        try (PreparedStatement preparedStatement = _createPreparedStatement(connection, sql, params)) {
            int updateCount = preparedStatement.executeUpdate();
            if (resultSetConsumer != null) {
                resultSetConsumer.accept(updateCount);
            }
            return updateCount;
        } catch (SQLException e) {
            throw e;
        }
    }

    //=========================================================================
    boolean isConnectionActive() {
        return this.connectionOptional.isPresent();
    }

    void doWithConnection(ConsumerThrowingSQLException<Connection> consumer) throws SQLException {
        if (this.connectionOptional.isPresent()) {
            consumer.accept(connectionOptional.get());
        }
    }

    Map<String, Object> createMapFromResultSet(ResultSet resultSet) throws SQLException {
        final Map<String, Object> m = new HashMap<>();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            final String label = metaData.getColumnLabel(i);
            final String name = metaData.getColumnLabel(i);
            final Object val = resultSet.getObject(i);
            if (label.equals(name)) {
                m.put(name, val);
            } else {
                m.put(name, val);
                m.put(label, val);
            }
        }
        return m;
    }

    //=========================================================================
    private Connection _createOrGetConnection() throws SQLException {
        final Connection connection;
        if (connectionOptional.isPresent()) {
            connection = connectionOptional.get();
        } else {
            connection = this.connectionFactory.createConnection();
            connectionOptional = Optional.of(connection);
        }
        return connection;
    }

    private Statement _createTheStatement(Connection connection, String sql, List<Object> params) throws SQLException {
        final Statement statement;
        if (params == null) {
            statement = _createSimpleStatement(connection, sql);
        } else {
            statement = _createPreparedStatement(connection, sql, params);
        }
        return statement;
    }

    private Statement _createSimpleStatement(Connection connection, String sql) throws SQLException {
        final Statement statement = connection.createStatement();
        return statement;
    }

    private PreparedStatement _createPreparedStatement(Connection connection, String sql, List<Object> params) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                final int jdbcIndex = i + 1;
                preparedStatement.setObject(jdbcIndex, params.get(i));
            }
        }
        return preparedStatement;
    }

    private boolean _moveCursor(ResultSet results, int offset) throws SQLException {
        boolean cursorAtRow = true;
        if (results.getType() == ResultSet.TYPE_FORWARD_ONLY) {
            int i = 1;
            while (i++ < offset && cursorAtRow) {
                cursorAtRow = results.next();
            }
        } else if (offset > 1) {
            cursorAtRow = results.absolute(offset - 1);
        }
        return cursorAtRow;
    }

    @Override
    public void close() throws SQLException {
        if (connectionOptional.isPresent()) {
            connectionOptional.get().close();
        }
    }

}
