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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author berni3
 */
public class JdbcSqlF {

    //---
    public void processResultSet(Connection conn,
            FunctionThrowingSQLException<Connection, PreparedStatement> f1,
            ConsumerThrowingSQLException<PreparedStatement> c0,
            FunctionThrowingSQLException<PreparedStatement, ResultSet> f2,
            ConsumerThrowingSQLException<ResultSet> c) throws SQLException {
        try (final PreparedStatement ps = f1.apply(conn)) {
            c0.accept(ps);
            try (final ResultSet rs = f2.apply(ps)) {
                c.accept(rs);
            }
        }
    }

    public void processResultSet(Connection conn,
            String sql,
            ConsumerThrowingSQLException<ResultSet> c
    ) throws SQLException {
        final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
        final ConsumerThrowingSQLException<PreparedStatement> c0 = JdbcSqlF.PreparedStatements.noop();
        final FunctionThrowingSQLException<PreparedStatement, ResultSet> f2 = PreparedStatements.executeQuery();
        processResultSet(conn, f1, c0, f2, c);
    }

    public void processResultSet(Connection conn,
            String sql, List<Object> params,
            ConsumerThrowingSQLException<ResultSet> c
    ) throws SQLException {
        final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
        final ConsumerThrowingSQLException<PreparedStatement> c0 = JdbcSqlF.PreparedStatements.params(params);
        final FunctionThrowingSQLException<PreparedStatement, ResultSet> f2 = PreparedStatements.executeQuery();

        processResultSet(conn, f1, c0, f2, c);
    }

    //---
    public int executeUpdate(Connection conn,
            FunctionThrowingSQLException<Connection, PreparedStatement> f1,
            ConsumerThrowingSQLException<PreparedStatement> c) throws SQLException {
        try (PreparedStatement ps = f1.apply(conn)) {
            int updateCount = ps.executeUpdate();
            c.accept(ps);
            return updateCount;
        }
    }

    public int executeUpdate(Connection conn,
            String sql,
            ConsumerThrowingSQLException<PreparedStatement> c) throws SQLException {
        final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
        return executeUpdate(conn, f1, c);
    }

    public int executeUpdate(Connection conn,
            String sql, List<Object> params,
            ConsumerThrowingSQLException<PreparedStatement> c) throws SQLException {
        final FunctionThrowingSQLException<Connection, PreparedStatement> f1 = Connections.createPreparedStatement(sql);
        final ConsumerThrowingSQLException<PreparedStatement> c0 = JdbcSqlF.PreparedStatements.params(params);
        return executeUpdate(conn, f1, c0.andThen(c));
    }

    public static class Connections {

        public static void withDataSource(DataSource dataSource,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                c.accept(connection);
            }
        }

        public static void withConnection(Connection connection,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            try (connection) {
                c.accept(connection);
            }
        }

        public static void withTransaction(Connection connection,
                ConsumerThrowingSQLException<Connection> c) throws SQLException {
            try {
                connection.setAutoCommit(false);
                c.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
            }
        }

        public static FunctionThrowingSQLException<Connection, PreparedStatement> createPreparedStatement(String sql) {
            return (connection) -> connection.prepareStatement(sql);
        }

        public static FunctionThrowingSQLException<Connection, Map<String, Object>> getConnectionInfos() {
            return (connection) -> {
                Map<String, Object> map = new HashMap<String, Object>() {
                    {
                        put("autoCommit", connection.getAutoCommit());
                        put("catalog", connection.getCatalog());
                        put("clientInfo", connection.getClientInfo());
                        put("holdability", connection.getHoldability());
                        put("metaData", connection.getMetaData());
                        put("networkTimeout", connection.getNetworkTimeout());
                        put("schema", connection.getSchema());
                        put("tansactionIsolation", connection.getTransactionIsolation());
                        put("warnings", connection.getWarnings());
                    }
                };
                return map;
            };
        }

    }

    public static class PreparedStatements {

        public static ConsumerThrowingSQLException<PreparedStatement> noop() {
            return (ps) -> {
            };
        }

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

        public static FunctionThrowingSQLException<PreparedStatement, ResultSet> executeQuery() {
            return (ps) -> ps.executeQuery();
        }
    }

    public static class ResultSetConsumers {

        static ConsumerThrowingSQLException<ResultSet> f1(ConsumerThrowingSQLException<ResultSet> innerConsumer) {
            ConsumerThrowingSQLException<ResultSet> outerConsumer = (rs) -> {
                while (rs.next()) {
                    innerConsumer.accept(rs);
                }

            };
            return outerConsumer;
        }

        static ConsumerThrowingSQLException<ResultSet> firstResultOnly(ConsumerThrowingSQLException<ResultSet> innerConsumer) {
            return (rs) -> {
                rs.next();
                innerConsumer.accept(rs);
            };
        }

    }

    @FunctionalInterface
    public static interface FunctionThrowingSQLException<T, R> {

        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws java.sql.SQLException
         */
        R apply(T t) throws SQLException;

    }

    public static class Holder<T> {

        private T t;

        public Holder() {
            this(null);
        }

        public Holder(T initial) {
            this.t = t;
        }

        public T get() {
            return t;
        }

        public void set(T t) {
            this.t = t;
        }
    }
}
