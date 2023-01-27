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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.huberb.h2tools.jdbc.Supports.ConsumerThrowingSQLException;

/**
 * Wrapper around jdbc operations.
 *
 * @author berni3
 */
public class JdbcSql implements AutoCloseable {

    public static final ConsumerThrowingSQLException<Integer> EMPTY_INTEGER_CONSUMER = null;
    public static final ConsumerThrowingSQLException<ResultSetMetaData> EMPTY_RESULTSETMETADATA_CONSUMER = null;
    public static final List<Object> EMPTY_PARAMS = null;

    /**
     * Create a new JdbcSql instance.
     *
     * @param iconnectionFactory
     * @return
     */
    public static JdbcSql newInstance(IConnectionFactory iconnectionFactory) {
        final JdbcSql javaSQL = new JdbcSql(iconnectionFactory);
        return javaSQL;
    }

    /**
     * Create a new JdbSql instance, and pass it to the consumer.
     *
     * @param iconnectionFactory
     * @param consumer
     * @throws SQLException
     */
    public static void withInstance(IConnectionFactory iconnectionFactory,
            ConsumerThrowingSQLException<JdbcSql> consumer) throws SQLException {
        try (JdbcSql jdbcSql = newInstance(iconnectionFactory)) {
            consumer.accept(jdbcSql);
        }
    }

    final IConnectionFactory connectionFactory;
    Optional<Connection> connectionOptional;

    JdbcSql(IConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.connectionOptional = Optional.empty();
    }

    /**
     * Pass a connection to the consumer.
     *
     * @param consumer
     * @throws SQLException
     */
    public void withConnection(ConsumerThrowingSQLException<Connection> consumer) throws SQLException {
        try (Connection connection = this._createOrGetConnection()) {
            consumer.accept(connection);
        }
    }

    /**
     * Pass a non-auto-commit connection to the consumer.
     * <p>
     * Only if consumer does not throw an expcetion, the db transaction is
     * committed. In case an exception is thrown, the transaction is rolled
     * back.
     *
     * @param consumer
     * @throws SQLException
     */
    public void withTransaction(ConsumerThrowingSQLException<Connection> consumer) throws SQLException {
        try (Connection connection = this._createOrGetConnection()) {
            try {
                connection.setAutoCommit(false);
                consumer.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                try {
                    if (ex instanceof SQLException) {
                        throw ex;
                    } else {
                        throw new SQLException("withTransaction", ex);
                    }
                } finally {
                    connection.rollback();
                }
            }
        }
    }

    /**
     * Execute an {@link PreparedStatement#executeQuery()} and set query
     * parameters.
     * <p>
     * Invoke resultSetConsumer for each available {@link ResultSet}.
     *
     * @param sql
     * @param preparedStatementConsumer
     * @param resultSetMetaDataConsumer
     * @param offset
     * @param maxRows
     * @param resultSetConsumer
     * @throws SQLException
     */
    public void eachRow(String sql,
            ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer,
            ConsumerThrowingSQLException<ResultSetMetaData> resultSetMetaDataConsumer,
            int offset, int maxRows,
            ConsumerThrowingSQLException<ResultSet> resultSetConsumer) throws SQLException {

        final boolean closeConnectionInFinally = !this.isConnectionActive();
        final Connection connection = _createOrGetConnection();
        try {
            try (PreparedStatement preparedStatement = _createPreparedStatement(sql, preparedStatementConsumer)) {
                try (ResultSet results = preparedStatement.executeQuery()) {
                    if (resultSetMetaDataConsumer != null) {
                        resultSetMetaDataConsumer.accept(results.getMetaData());
                    }
                    final boolean cursorAtRow = _moveCursor(results, offset);
                    if (!cursorAtRow) {
                        return;
                    }

                    int i = 0;
                    while ((maxRows <= 0 || i++ < maxRows) && results.next()) {
                        resultSetConsumer.accept(results);
                    }
                }
            } catch (SQLException e) {
                throw e;
            }
        } finally {
            if (closeConnectionInFinally) {
                connection.close();
            }
        }
    }

    /**
     * Execute an {@link PreparedStatement#executeQuery()} and set query
     * parameters.
     * <p>
     * Invoke resultSetConsumer for each available {@link ResultSet}.
     *
     * @param sql
     * @param params
     * @param resultSetMetaDataConsumer
     * @param offset
     * @param maxRows
     * @param resultSetConsumer
     * @throws SQLException
     */
    public void eachRow(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<ResultSetMetaData> resultSetMetaDataConsumer,
            int offset, int maxRows,
            ConsumerThrowingSQLException<ResultSet> resultSetConsumer) throws SQLException {

        final ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer = (PreparedStatement preparedStatement) -> {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    final int jdbcIndex = i + 1;
                    preparedStatement.setObject(jdbcIndex, params.get(i));
                }
            }
        };
        eachRow(sql, preparedStatementConsumer, resultSetMetaDataConsumer, offset, maxRows, resultSetConsumer);
    }

    /**
     * Execute an {@link PreparedStatement#executeQuery()} and set query
     * parameters.
     * <p>
     * Invoke resultSetConsumer once with the {@link ResultSet}. Thus iterating
     * through the result-set is the responsibility of the resultSetConsumer.
     *
     *
     * @param sql the sql statement
     * @param preparedStatementConsumer a consumer accepting a
     * {@link  PreparedStatement}, the consumer shall set the prepared statement
     * parameters.
     * @param resultSetConsumer a consumer accepts the {@link ResultSet} of the
     * query.
     * @throws SQLException
     */
    public void executeQuery(String sql,
            ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer,
            ConsumerThrowingSQLException<ResultSet> resultSetConsumer) throws SQLException {
        final boolean closeConnectionInFinally = !this.isConnectionActive();
        final Connection connection = _createOrGetConnection();
        try {
            try (PreparedStatement preparedStatement = _createPreparedStatement(sql, preparedStatementConsumer)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    resultSetConsumer.accept(resultSet);
                } catch (SQLException e) {
                    throw e;
                }
            }
        } finally {
            if (closeConnectionInFinally) {
                connection.close();
            }
        }
    }

    /**
     * Execute an execute query.
     * <p>
     * Invoke resultSetConsumer once with the {@link ResultSet}. Thus iterating
     * through the result-set is the responsibility of the resultSetConsumer.
     *
     * @param sql the sql statement.
     * @param params optional parameters, if no parameters pass
     * {@link JdbcSql#EMPTY_PARAMS}.
     * @param resultSetConsumer a consumer accepts the {@link ResultSet} of the
     * query.
     * @throws SQLException
     */
    public void executeQuery(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<ResultSet> resultSetConsumer) throws SQLException {

        final ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer = (PreparedStatement preparedStatement) -> {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    final int jdbcIndex = i + 1;
                    preparedStatement.setObject(jdbcIndex, params.get(i));
                }
            }
        };
        executeQuery(sql, preparedStatementConsumer, resultSetConsumer);
    }

    /**
     * Execute an execute update.
     *
     * @param sql the sql statement.
     * @param preparedStatementConsumer a consumer accepting a
     * {@link  PreparedStatement}, the consumer shall set the prepared statement
     * parameters.
     * @param resultSetConsumer
     * @return count of updates
     * @throws SQLException
     */
    public int executeUpdate(String sql,
            ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer,
            ConsumerThrowingSQLException<Integer> resultSetConsumer) throws SQLException {
        final boolean closeConnectionInFinally = !this.isConnectionActive();
        final Connection connection = _createOrGetConnection();
        try {
            try (PreparedStatement preparedStatement = _createPreparedStatement(sql, preparedStatementConsumer)) {
                int updateCount = preparedStatement.executeUpdate();
                if (resultSetConsumer != null) {
                    resultSetConsumer.accept(updateCount);
                }
                return updateCount;
            } catch (SQLException e) {
                throw e;
            }
        } finally {
            if (closeConnectionInFinally) {
                connection.close();
            }
        }
    }

    /**
     * Execute an execute update.
     *
     * @param sql the sql statement.
     * @param params optional parameters, if no parameters pass
     * {@link JdbcSql#EMPTY_PARAMS}.
     * @param resultSetConsumer
     * @return count of updates
     * @throws SQLException
     */
    public int executeUpdate(String sql,
            List<Object> params,
            ConsumerThrowingSQLException<Integer> resultSetConsumer) throws SQLException {
        final ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer = (PreparedStatement preparedStatement) -> {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    final int jdbcIndex = i + 1;
                    preparedStatement.setObject(jdbcIndex, params.get(i));
                }
            }
        };
        return executeUpdate(sql, preparedStatementConsumer, resultSetConsumer);
    }

    /**
     * Execute an sql statement in batch mode.
     *
     * @param sql the sql statement
     * @param paramsList list of parameters
     * @param resultSetConsumer
     * @return
     * @throws SQLException
     */
    public int[] executeBatch(String sql,
            List<List<Object>> paramsList,
            ConsumerThrowingSQLException<int[]> resultSetConsumer) throws SQLException {
        final boolean closeConnectionInFinally = !this.isConnectionActive();
        final Connection connection = _createOrGetConnection();
        try {
            try (PreparedStatement preparedStatement = _createPreparedStatement(connection, sql, null)) {
                for (List<Object> params : paramsList) {
                    for (int i = 0; i < params.size(); i++) {
                        final int jdbcIndex = i + 1;
                        preparedStatement.setObject(jdbcIndex, params.get(i));
                    }
                    preparedStatement.addBatch();
                    preparedStatement.clearParameters();
                }

                int[] updates = preparedStatement.executeBatch();
                if (resultSetConsumer != null) {
                    resultSetConsumer.accept(updates);
                }
                return updates;
            } catch (SQLException e) {
                throw e;
            }
        } finally {
            if (closeConnectionInFinally) {
                connection.close();
            }
        }
    }

    //=========================================================================
    /**
     * Create a map from a given result set.
     *
     * @param resultSet given result set.
     * @return a map created from the result set.
     * @throws SQLException
     */
    public Map<String, Object> createMapFromResultSet(ResultSet resultSet) throws SQLException {
        final Map<String, Object> m = new HashMap<>();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final int columnCount = metaData.getColumnCount();
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
    public boolean isConnectionActive() throws SQLException {
        boolean isConnectionActive = this.connectionOptional.map((c) -> {
            try {
                return !c.isClosed();
            } catch (SQLException sqlex) {
                return false;
            }
        }).orElse(Boolean.FALSE);
        return isConnectionActive;
    }

    public Optional<Connection> connectionOptional() {
        return this.connectionOptional;
    }

    private Connection _createOrGetConnection() throws SQLException {
        final Connection connection;
        if (isConnectionActive()) {
            connection = connectionOptional.get();
        } else {
            connection = this.connectionFactory.createConnection();
            connectionOptional = Optional.of(connection);
        }
        return connection;
    }

    /**
     * Create a {@link PreparedStatement}.
     *
     * @param connection
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    private PreparedStatement _createPreparedStatement(Connection connection,
            String sql,
            List<Object> params) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                final int jdbcIndex = i + 1;
                preparedStatement.setObject(jdbcIndex, params.get(i));
            }
        }
        return preparedStatement;
    }

    /**
     * Create a {@link PreparedStatement}.
     *
     * @param sql
     * @param preparedStatementConsumer
     * @return
     * @throws SQLException
     */
    private PreparedStatement _createPreparedStatement(String sql,
            ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer) throws SQLException {
        final Connection connection = _createOrGetConnection();
        final PreparedStatement preparedStatement = _createPreparedStatement(connection, sql, null);
        preparedStatementConsumer.accept(preparedStatement);
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
