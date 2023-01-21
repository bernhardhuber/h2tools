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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author berni3
 */
public class JdbcSqlTest {

    enum SqlStatements {
        dropTable("DROP TABLE IF EXISTS TEST"),
        createTable("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))"),
        insertID_1("INSERT INTO TEST VALUES(1, 'Hello')"),
        insertID_2("INSERT INTO TEST VALUES(2, 'World')"),
        insertID_3("INSERT INTO TEST VALUES(3, 'H2')"),
        selectAll("SELECT * FROM TEST ORDER BY ID"),
        selectCountAll("SELECT COUNT(*) FROM TEST"),
        updateID_1("UPDATE TEST SET NAME='Hi' WHERE ID=1"),
        deleteID_2("DELETE FROM TEST WHERE ID=2");

        final String sql;

        private SqlStatements(String sql) {
            this.sql = sql;
        }

        String sql() {
            return this.sql;
        }
    }

    public static Stream<IConnectionFactory> streamOfIConnectionFactory() {
        final DefaultDataSourceOrConnectionCreator defaultDataSourceOrConnectionCreator = new DefaultDataSourceOrConnectionCreator();
        final DataSource jdbcConnectionPool = defaultDataSourceOrConnectionCreator.createJdbcConnectionPool();
        final DataSource jdbcDataSource = defaultDataSourceOrConnectionCreator.createJdbcDataSource();
        final Stream<IConnectionFactory> streamOfIConnectionFactory = Stream.of(
                defaultDataSourceOrConnectionCreator.createConnectionFactoryWithDataSource(jdbcConnectionPool),
                defaultDataSourceOrConnectionCreator.createConnectionFactoryWithDataSource(jdbcDataSource),
                defaultDataSourceOrConnectionCreator.createConnectionFactoryWithMap()
        );
        return streamOfIConnectionFactory;
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testNewInstance(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        try (final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory)) {
            assertNotNull(jdbcSql);
            assertFalse(jdbcSql.isConnectionActive());
            jdbcSql.withConnection((final Connection connection) -> {
                assertTrue(jdbcSql.isConnectionActive());
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));
            });
            assertFalse(jdbcSql.isConnectionActive());
        }
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testWithInstance(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        JdbcSql.withInstance(iconnectionFactory, (final JdbcSql jdbcSql) -> {
            assertFalse(jdbcSql.isConnectionActive());
            assertNotNull(jdbcSql);
            jdbcSql.withConnection((final Connection connection) -> {
                assertTrue(jdbcSql.isConnectionActive());
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));
            });
            assertFalse(jdbcSql.isConnectionActive());
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testWithTransaction_using_createConnectionFactoryWithMap(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        JdbcSql.withInstance(iconnectionFactory, (final JdbcSql jdbcSql) -> {
            assertFalse(jdbcSql.isConnectionActive());
            assertNotNull(jdbcSql);
            jdbcSql.withTransaction((final Connection connection) -> {
                assertTrue(jdbcSql.isConnectionActive());
                assertNotNull(connection);
                assertFalse(connection.getAutoCommit());

                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));
            });
            assertFalse(jdbcSql.isConnectionActive());
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testWithTransactionThrowingException(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        JdbcSql.withInstance(iconnectionFactory, (final JdbcSql jdbcSql) -> {
            assertFalse(jdbcSql.isConnectionActive());
            assertNotNull(jdbcSql);

            boolean catchedSQLException = false;
            try {
                jdbcSql.withTransaction((final Connection connection) -> {
                    assertTrue(jdbcSql.isConnectionActive());
                    assertNotNull(connection);
                    assertFalse(connection.getAutoCommit());

                    assertFalse(connection.isClosed());
                    assertFalse(connection.isReadOnly());
                    assertTrue(connection.isValid(5));

                    throw new RuntimeException("Exception inside withTransaction");
                });
            } catch (SQLException sqlException) {
                catchedSQLException = true;
            }
            assertTrue(catchedSQLException);
            assertFalse(jdbcSql.isConnectionActive());
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testWithTransactionExecuteUpdateAndExecuteQuery(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql(),
                        SqlStatements.insertID_1.sql(),
                        SqlStatements.insertID_2.sql())) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            {
                final String sql = SqlStatements.selectCountAll.sql();
                final List<Object> params = Collections.emptyList();
                jdbcSql.executeQuery(sql, params, (ResultSet resultSet) -> {

                    resultSet.next();
                    final int result = resultSet.getInt(1);
                    final String m = "" + sql;
                    assertEquals(2, result, m);
                });
            }
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testExecuteUpdateWithParams(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql())) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            {
                final String sql = "INSERT INTO TEST VALUES(?, ?)";
                jdbcSql.executeUpdate(sql, Arrays.asList(1, "HELLO"), JdbcSql.EMPTY_INTEGER_CONSUMER);
                jdbcSql.executeUpdate(sql, Arrays.asList(2, "WORLD"), JdbcSql.EMPTY_INTEGER_CONSUMER);
            }
            {
                final String sql = SqlStatements.selectCountAll.sql();
                final List<Object> params = Collections.emptyList();
                jdbcSql.executeQuery(sql, params, (ResultSet resultSet) -> {
                    resultSet.next();
                    final int result = resultSet.getInt(1);
                    final String m = "" + sql;
                    assertEquals(2, result, m);
                });
            }
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testExecuteBatchWithParams(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql())) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            {
                final String sql = "INSERT INTO TEST VALUES(?, ?)";
                final List<List<Object>> paramsList = new ArrayList<>();
                paramsList.add(Arrays.asList(1, "hello"));
                paramsList.add(Arrays.asList(2, "Hello"));
                paramsList.add(Arrays.asList(3, "HELLO"));
                paramsList.add(Arrays.asList(4, "world"));
                paramsList.add(Arrays.asList(5, "World"));
                paramsList.add(Arrays.asList(6, "WORLD"));

                int[] updates = jdbcSql.executeBatch(sql, paramsList, null);
            }
            {
                final String sql = SqlStatements.selectCountAll.sql();
                final List<Object> params = Collections.emptyList();
                jdbcSql.eachRow(sql, params,
                        JdbcSql.EMPTY_RESULTSETMETADATA_CONSUMER,
                        0, 1, (ResultSet resultSet) -> {
                            final int result = resultSet.getInt(1);
                            final String m = "" + sql;
                            assertEquals(6, result, m);
                        });
            }
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testEachRow(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql(),
                        SqlStatements.insertID_1.sql(),
                        SqlStatements.insertID_2.sql()
                )) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            final List<Map<String, String>> l = new ArrayList<>();
            jdbcSql.eachRow(SqlStatements.selectAll.sql,
                    JdbcSql.EMPTY_PARAMS,
                    JdbcSql.EMPTY_RESULTSETMETADATA_CONSUMER,
                    0, 0,
                    (ResultSet resultSet) -> {
                        Map<String, String> m = new HashMap<>();
                        m.put("ID", "" + resultSet.getInt("ID"));
                        m.put("NAME", "" + resultSet.getString("NAME"));
                        l.add(m);
                    });
            assertEquals(2, l.size());
            assertEquals("1", l.get(0).getOrDefault("ID", "-"));
            assertEquals("Hello", l.get(0).getOrDefault("NAME", "-"));
            assertEquals("2", l.get(1).getOrDefault("ID", "-"));
            assertEquals("World", l.get(1).getOrDefault("NAME", "-"));
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testEachRowInExpanded(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql(),
                        SqlStatements.insertID_1.sql(),
                        SqlStatements.insertID_2.sql()
                )) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            final List<Map<String, String>> l = new ArrayList<>();
            final String sql = "SELECT * FROM TEST WHERE ID IN (?,?) ORDER BY ID";
            final ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer = (PreparedStatement preparedStatement) -> {
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, 2);

            };
            jdbcSql.eachRow(sql,
                    preparedStatementConsumer,
                    JdbcSql.EMPTY_RESULTSETMETADATA_CONSUMER,
                    0, 0,
                    (ResultSet resultSet) -> {
                        Map<String, String> m = new HashMap<>();
                        m.put("ID", "" + resultSet.getInt("ID"));
                        m.put("NAME", "" + resultSet.getString("NAME"));
                        l.add(m);
                    });
            assertEquals(2, l.size());
            assertEquals("1", l.get(0).getOrDefault("ID", "-"));
            assertEquals("Hello", l.get(0).getOrDefault("NAME", "-"));
            assertEquals("2", l.get(1).getOrDefault("ID", "-"));
            assertEquals("World", l.get(1).getOrDefault("NAME", "-"));
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testEachRowInArray(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql(),
                        SqlStatements.insertID_1.sql(),
                        SqlStatements.insertID_2.sql(),
                        SqlStatements.insertID_3.sql()
                )) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            final List<Map<String, String>> l = new ArrayList<>();
            final String sql = "SELECT * FROM TEST WHERE ARRAY_CONTAINS(?, ID) ORDER BY ID";
            final ConsumerThrowingSQLException<PreparedStatement> preparedStatementConsumer = (PreparedStatement preparedStatement) -> {
                java.sql.Array array = preparedStatement.getConnection().createArrayOf("INT", new Integer[]{1, 2});
                preparedStatement.setArray(1, array);
            };
            jdbcSql.eachRow(sql,
                    preparedStatementConsumer,
                    JdbcSql.EMPTY_RESULTSETMETADATA_CONSUMER,
                    0, 0,
                    (ResultSet resultSet) -> {
                        Map<String, String> m = new HashMap<>();
                        m.put("ID", "" + resultSet.getInt("ID"));
                        m.put("NAME", "" + resultSet.getString("NAME"));
                        l.add(m);
                    });
            assertEquals(2, l.size());
            assertEquals("1", l.get(0).getOrDefault("ID", "-"));
            assertEquals("Hello", l.get(0).getOrDefault("NAME", "-"));
            assertEquals("2", l.get(1).getOrDefault("ID", "-"));
            assertEquals("World", l.get(1).getOrDefault("NAME", "-"));
        });
    }

    @ParameterizedTest
    @MethodSource(value = "streamOfIConnectionFactory")
    public void testEachRowUsingCreateMapFromResultSet(IConnectionFactory iconnectionFactory) throws SQLException {
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(iconnectionFactory);
        jdbcSql.withTransaction((final Connection connection) -> {
            {
                for (final String sql : Arrays.asList(
                        SqlStatements.dropTable.sql(),
                        SqlStatements.createTable.sql(),
                        SqlStatements.insertID_1.sql(),
                        SqlStatements.insertID_2.sql()
                )) {
                    final int executeUpdateCount = jdbcSql.executeUpdate(sql, JdbcSql.EMPTY_PARAMS, JdbcSql.EMPTY_INTEGER_CONSUMER);
                    final String m = "" + sql;
                    assertTrue(executeUpdateCount == 0 || executeUpdateCount == 1, m);
                }
            }
            final List<Map<String, Object>> l = new ArrayList<>();
            jdbcSql.eachRow(SqlStatements.selectAll.sql,
                    JdbcSql.EMPTY_PARAMS,
                    JdbcSql.EMPTY_RESULTSETMETADATA_CONSUMER,
                    0, 0,
                    (ResultSet resultSet) -> {
                        final Map<String, Object> m = jdbcSql.createMapFromResultSet(resultSet);
                        l.add(m);
                    });
            assertEquals(2, l.size());
            assertEquals("1", "" + l.get(0).getOrDefault("ID", "-"));
            assertEquals("-", "" + l.get(0).getOrDefault("id", "-"));
            assertEquals("Hello", l.get(0).getOrDefault("NAME", "-"));
            assertEquals("-", l.get(0).getOrDefault("name", "-"));
            assertEquals("2", "" + l.get(1).getOrDefault("ID", "-"));
            assertEquals("World", l.get(1).getOrDefault("NAME", "-"));
        });
    }

}
