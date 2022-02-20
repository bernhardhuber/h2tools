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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.huberb.h2tools.jdbc.JdbcSql.ConnectionFactoryWithMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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

    @Test
    public void testNewInstance() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        try (final JdbcSql jdbcSql = JdbcSql.newInstance(connectionFactoryWithMap)) {
            assertNotNull(jdbcSql);
            assertFalse(jdbcSql.isConnectionActive());
            jdbcSql.withConnection((final Connection connection) -> {
                assertTrue(jdbcSql.isConnectionActive());
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));
            });
        }
    }

    @Test
    public void testWithInstance() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        JdbcSql.withInstance(connectionFactoryWithMap, (final JdbcSql jdbcSql) -> {
            assertFalse(jdbcSql.isConnectionActive());

            assertNotNull(jdbcSql);
            jdbcSql.withConnection((final Connection connection) -> {
                assertTrue(jdbcSql.isConnectionActive());
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));
            });
        });

    }

    @Test
    public void testWithTransaction() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        JdbcSql.withInstance(connectionFactoryWithMap, (final JdbcSql jdbcSql) -> {
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
        });
    }

    @Test
    public void testWithTransactionExecuteUpdateAndExecuteQuery() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(connectionFactoryWithMap);
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

    @Test
    public void testExecuteUpdateWithParams() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(connectionFactoryWithMap);
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

    @Test
    public void testEachRow() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(connectionFactoryWithMap);
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

    @Test
    public void testEachRowUsingCreateMapFromResultSet() throws SQLException {
        final ConnectionFactoryWithMap connectionFactoryWithMap = createConnectionFactoryWithMap();
        //---
        final JdbcSql jdbcSql = JdbcSql.newInstance(connectionFactoryWithMap);
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
            assertEquals("1", ""+l.get(0).getOrDefault("ID", "-"));
            assertEquals("-", ""+l.get(0).getOrDefault("id", "-"));
            assertEquals("Hello", l.get(0).getOrDefault("NAME", "-"));
            assertEquals("-", l.get(0).getOrDefault("name", "-"));
            assertEquals("2", ""+l.get(1).getOrDefault("ID", "-"));
            assertEquals("World", l.get(1).getOrDefault("NAME", "-"));
        });
    }

    //----
    ConnectionFactoryWithMap createConnectionFactoryWithMap() {
        final Map<String, Object> m = new HashMap<>();
        m.put("url", "jdbc:h2:mem:test1");
        m.put("user", "sa1");
        m.put("password", "sa1");
        final ConnectionFactoryWithMap connectionFactoryWithMap = new ConnectionFactoryWithMap(m);
        return connectionFactoryWithMap;
    }
}