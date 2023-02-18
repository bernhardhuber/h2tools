/*
 * Copyright 2023 berni3.
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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.huberb.h2tools.jdbc.Supports.ConsumerThrowingSQLException;
import org.huberb.h2tools.jdbc.Supports.FunctionThrowingSQLException;

/**
 *
 * @author berni3
 */
public class JdbcSqlF {
    
    public static class ResultSetCommands {
        //---

        public static void consumeResultSet(Connection conn,
                FunctionThrowingSQLException<Connection, PreparedStatement> createPreparedStatement,
                ConsumerThrowingSQLException<PreparedStatement> consumerPreparedStatement,
                FunctionThrowingSQLException<PreparedStatement, ResultSet> createResultSet,
                ConsumerThrowingSQLException<ResultSet> consumeResult) throws SQLException {
            try (final PreparedStatement ps = createPreparedStatement.apply(conn)) {
                consumerPreparedStatement.accept(ps);
                try (final ResultSet rs = createResultSet.apply(ps)) {
                    consumeResult.accept(rs);
                }
            }
        }
        
        public static void processResultSet(Connection conn,
                String sql,
                ConsumerThrowingSQLException<ResultSet> consumeResultSet
        ) throws SQLException {
            final FunctionThrowingSQLException<Connection, PreparedStatement> createPreparedStatement = Connections.createPreparedStatement(sql);
            final ConsumerThrowingSQLException<PreparedStatement> consumePreparedStatement = JdbcSqlF.PreparedStatements.noop();
            final FunctionThrowingSQLException<PreparedStatement, ResultSet> createResultSet = PreparedStatements.executeQuery();
            consumeResultSet(conn,
                    createPreparedStatement,
                    consumePreparedStatement,
                    createResultSet,
                    consumeResultSet);
        }
        
        public static void processResultSet(Connection conn,
                String sql, List<Object> params,
                ConsumerThrowingSQLException<ResultSet> consumeResultSet
        ) throws SQLException {
            final FunctionThrowingSQLException<Connection, PreparedStatement> createPreparedStatement = Connections.createPreparedStatement(sql);
            final ConsumerThrowingSQLException<PreparedStatement> consumePreparedStatement = JdbcSqlF.PreparedStatements.params(params);
            final FunctionThrowingSQLException<PreparedStatement, ResultSet> createResultSet = PreparedStatements.executeQuery();
            
            consumeResultSet(conn,
                    createPreparedStatement,
                    consumePreparedStatement,
                    createResultSet,
                    consumeResultSet);
        }
    }
    //---

    public static class UpdateCommands {
        
        public static int executeUpdate(Connection conn,
                FunctionThrowingSQLException<Connection, PreparedStatement> f1,
                ConsumerThrowingSQLException<PreparedStatement> c) throws SQLException {
            try (PreparedStatement ps = f1.apply(conn)) {
                c.accept(ps);
                return ps.executeUpdate();
            }
        }
        
        public static int executeUpdate(Connection conn,
                String sql) throws SQLException {
            final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
            return executeUpdate(conn, f1, JdbcSqlF.PreparedStatements.noop());
        }
        
        public static int executeUpdate(Connection conn,
                String sql, List<Object> params) throws SQLException {
            final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
            final ConsumerThrowingSQLException<PreparedStatement> c0 = JdbcSqlF.PreparedStatements.params(params);
            return executeUpdate(conn, f1, c0);
        }
    }
    //---

    public static class BatchCommands {
        
        public static int[] executeBatch(Connection conn,
                FunctionThrowingSQLException<Connection, PreparedStatement> f1,
                ConsumerThrowingSQLException<PreparedStatement> c) throws SQLException {
            
            try (PreparedStatement ps = f1.apply(conn)) {
                c.accept(ps);
                return ps.executeBatch();
            }
        }
        
        public static int[] executeBatch(Connection conn,
                String sql, List<List<Object>> paramsList) throws SQLException {
            FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
            ConsumerThrowingSQLException<PreparedStatement> c0 = JdbcSqlF.PreparedStatements.batchParamsList(paramsList);
            return executeBatch(conn, f1, c0);
        }
    }
    
    public static class Connections {

        /**
         * Create a {@link Connection} from a {@link DataSource}, and use the
         * connection via a consumer function.
         *
         * @param dataSource
         * @param connectionConsumer
         * @throws SQLException if creating connections fails, or consumer
         * fails.
         */
        public static void withDataSource(DataSource dataSource,
                ConsumerThrowingSQLException<Connection> connectionConsumer) throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                connectionConsumer.accept(connection);
            }
        }
        
        public static void withConnection(Connection connection,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            try (connection) {
                c.accept(connection);
            }
        }
        
        public static void withTransactionSavepoint(Connection connection,
                String savePointName,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            final Savepoint savePoint = connection.setSavepoint(savePointName);
            try {
                connection.setAutoCommit(false);
                c.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback(savePoint);
            }
        }

        /**
         * Begin a connection transaction and use the connection via a consumer
         * function.
         * <p>
         * This implementation commits the transaction if no exception is thrown
         * otherwise the started transaction is rollback.
         * <p>
         * As a side effect this implementation set {@link Connection#setAutoCommit(boolean)
         * } always to {@code false}.
         *
         * @param connection
         * @param c
         * @throws SQLException
         */
        public static void withTransaction(Connection connection,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            try {
                connection.setAutoCommit(false);
                c.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                if (ex instanceof SQLException) {
                    throw ex;
                } else {
                    throw new SQLException("withTransaction", ex);
                }
            }
        }

        /**
         * Create a {@link PreparedStatement}.
         *
         * @param sql
         * @return function that creates a {@link PreparedStatement} from a
         * {@link Connection}.
         */
        public static FunctionThrowingSQLException<Connection, PreparedStatement> createPreparedStatement(String sql) {
            return (connection) -> connection.prepareStatement(sql);
        }
        
        public static FunctionThrowingSQLException<Connection, Map<String, Object>> getConnectionInfos() {
            return (connection) -> {
                Map<String, Object> metaDataMap = new HashMap<>() {
                    {
                        DatabaseMetaData metaData = connection.getMetaData();
                        put("metaData.CatalogSeparator", metaData.getCatalogSeparator());
                        put("metaData.CatalogTerm", metaData.getCatalogTerm());
                        put("metaData.DatabaseMajorVersion", metaData.getDatabaseMajorVersion());
                        put("metaData.DatabaseMinorVersion", metaData.getDatabaseMinorVersion());
                        put("metaData.DatabaseProductName", metaData.getDatabaseProductName());
                        put("metaData.DatabaseProductVersion", metaData.getDatabaseProductVersion());
                        put("metaData.DefaultTransactionIsolation", metaData.getDefaultTransactionIsolation());
                        put("metaData.DriverMajorVersion", metaData.getDriverMajorVersion());
                        put("metaData.DriverMinorVersion", metaData.getDriverMinorVersion());
                        put("metaData.DriverName", metaData.getDriverName());
                        put("metaData.DriverVersion", metaData.getDriverVersion());
                        put("metaData.ExtraNameCharacters", metaData.getExtraNameCharacters());
                        put("metaData.IdentifierQuoteString", metaData.getIdentifierQuoteString());
                        put("metaData.JDBCMajorVersion", metaData.getJDBCMajorVersion());
                        put("metaData.JDBCMinorVersion", metaData.getJDBCMinorVersion());
                        put("metaData.MaxBinaryLiteralLength", metaData.getMaxBinaryLiteralLength());
                        put("metaData.MaxCatalogNameLength", metaData.getMaxCatalogNameLength());
                        put("metaData.MaxCharLiteralLength", metaData.getMaxCharLiteralLength());
                        put("metaData.MaxColumnNameLength", metaData.getMaxColumnNameLength());
                        put("metaData.MaxColumnsInGroupBy", metaData.getMaxColumnsInGroupBy());
                        put("metaData.MaxColumnsInIndex", metaData.getMaxColumnsInIndex());
                        put("metaData.MaxColumnsInOrderBy", metaData.getMaxColumnsInOrderBy());
                        put("metaData.MaxColumnsInSelect", metaData.getMaxColumnsInSelect());
                        put("metaData.MaxColumnsInTable", metaData.getMaxColumnsInTable());
                        put("metaData.MaxConnections", metaData.getMaxConnections());
                        put("metaData.MaxCursorNameLength", metaData.getMaxCursorNameLength());
                        put("metaData.MaxIndexLength", metaData.getMaxIndexLength());
                        put("metaData.MaxLogicalLobSize", metaData.getMaxLogicalLobSize());
                        put("metaData.MaxProcedureNameLength", metaData.getMaxProcedureNameLength());
                        put("metaData.MaxRowSize", metaData.getMaxRowSize());
                        put("metaData.MaxSchemaNameLength", metaData.getMaxSchemaNameLength());
                        put("metaData.MaxStatementLength", metaData.getMaxStatementLength());
                        put("metaData.MaxStatements", metaData.getMaxStatements());
                        put("metaData.MaxTableNameLength", metaData.getMaxTableNameLength());
                        put("metaData.MaxTablesInSelect", metaData.getMaxTablesInSelect());
                        put("metaData.MaxUserNameLength", metaData.getMaxUserNameLength());
                    }
                };
                Map<String, Object> map = new HashMap<>() {
                    {
                        put("autoCommit", connection.getAutoCommit());
                        put("catalog", connection.getCatalog());
                        put("clientInfo", connection.getClientInfo());
                        put("holdability", connection.getHoldability());
                        put("networkTimeout", connection.getNetworkTimeout());
                        put("schema", connection.getSchema());
                        put("transactionIsolation", connection.getTransactionIsolation());
                        put("warnings", connection.getWarnings());
                    }
                };
                map.putAll(metaDataMap);
                return map;
            };
        }
    }
    
    public static class PreparedStatements {
        
        public static ConsumerThrowingSQLException<PreparedStatement> noop() {
            return (ps) -> {
            };
        }

        /**
         * Consume a {@link PreparedStatement}for setting its parameters.
         *
         * @param params
         * @return a {@link Consumer} setting the parameters.
         */
        public static ConsumerThrowingSQLException<PreparedStatement> params(List<Object> params) {
            return (PreparedStatement ps) -> {
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        final int jdbcIndex = i + 1;
                        ps.setObject(jdbcIndex, params.get(i));
                    }
                }
            };
        }
        
        public static ConsumerThrowingSQLException<PreparedStatement> batchParamsList(List<List<Object>> paramsList) {
            return (PreparedStatement ps) -> {
                if (paramsList != null) {
                    for (List<Object> params : paramsList) {
                        for (int i = 0; i < params.size(); i++) {
                            final int jdbcIndex = i + 1;
                            ps.setObject(jdbcIndex, params.get(i));
                        }
                        ps.addBatch();
                        ps.clearParameters();
                    }
                }
            };
        }
        
        public static FunctionThrowingSQLException<PreparedStatement, ResultSet> executeQuery() {
            return (ps) -> ps.executeQuery();
        }
    }
    
    public static class ResultSets {
        
        static ConsumerThrowingSQLException<ResultSet> allResultSets(ConsumerThrowingSQLException<ResultSet> innerConsumer) {
            ConsumerThrowingSQLException<ResultSet> outerConsumer = (rs) -> {
                while (rs.next()) {
                    innerConsumer.accept(rs);
                }
                
            };
            return outerConsumer;
        }
        
        static ConsumerThrowingSQLException<ResultSet> firstResultSetOnly(ConsumerThrowingSQLException<ResultSet> innerConsumer) {
            return (rs) -> {
                rs.next();
                innerConsumer.accept(rs);
            };
        }

        //---
        static <T> List<T> convertResultSets(ResultSet rs, int offset, int limit,
                FunctionThrowingSQLException<ResultSet, T> f) throws SQLException {
            final List<T> l = new ArrayList<>();
            for (Iterator<ResultSet> it = resultSetIterator(rs, offset, limit); it.hasNext();) {
                ResultSet itNext = it.next();
                T t = f.apply(itNext);
                l.add(t);
            }
            return l;
        }
        
        static <T> List<T> convertResultSets(ResultSet rs, FunctionThrowingSQLException<ResultSet, T> f) throws SQLException {
            return convertResultSets(rs, 0, -1, f);
        }
        
        static Iterator<ResultSet> resultSetIterator(ResultSet rs, int offset, int limit) {
            return new ResultSetIterator(rs, offset, limit);
        }
        
        static class ResultSetIterator implements Iterator<ResultSet> {
            
            final ResultSet rs;
            final int offset;
            final int limit;
            int countNextCalled = 0;
            
            public ResultSetIterator(ResultSet rs, int offset, int limit) {
                this.rs = rs;
                this.offset = offset;
                this.limit = limit > 0 ? limit : -1;
                
                try {
                    this.rs.absolute(this.offset);
                } catch (SQLException sqlex) {
                    throw new RuntimeException("hasNext", sqlex);
                }
            }
            
            @Override
            public boolean hasNext() {
                try {
                    return this.limit >= 1
                            && countNextCalled < this.limit
                            && rs.isBeforeFirst() && !rs.isAfterLast();
                } catch (SQLException sqlex) {
                    throw new RuntimeException("hasNext", sqlex);
                }
            }
            
            @Override
            public ResultSet next() {
                try {
                    boolean afterLastRowMoved = rs.next();
                    countNextCalled += 1;
                    return rs;
                } catch (SQLException sqlex) {
                    throw new RuntimeException("next", sqlex);
                }
            }
            
        }

        //---
        static FunctionThrowingSQLException<ResultSet, List<Object>> convertResultSetToList() {
            return (rs) -> {
                final int columnCount = rs.getMetaData().getColumnCount();
                final List<Object> l = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i += 1) {
                    final Object columnValue = rs.getObject(i);
                    l.add(columnValue);
                }
                return l;
            };
        }
        
        static FunctionThrowingSQLException<ResultSet, Map<String, Object>> convertResultSetToMap() {
            return (rs) -> {
                final ResultSetMetaData metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();
                final Map<String, Object> m = new HashMap<>(2 * columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    final String label = metaData.getColumnLabel(i);
                    final String name = metaData.getColumnLabel(i);
                    final Object val = rs.getObject(i);
                    if (label.equals(name)) {
                        m.put(name, val);
                    } else {
                        m.put(name, val);
                        m.put(label, val);
                    }
                }
                return m;
            };
        }
    }
    
}
