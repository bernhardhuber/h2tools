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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.huberb.h2tools.jdbc.Supports.ConsumerThrowingSQLException;
import org.huberb.h2tools.jdbc.Supports.Holder;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class JdbcSqlFTest {

    enum SqlStatements {
        //---
        createTable("CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))"),
        dropTable("DROP TABLE IF EXISTS TEST"),
        //--- 
        insertID_1("INSERT INTO TEST VALUES(1, 'Hello')"),
        insertID_2("INSERT INTO TEST VALUES(2, 'World')"),
        insertID_3("INSERT INTO TEST VALUES(3, 'H2')"),
        insertID_4_PARAM("INSERT INTO TEST VALUES(4, ?)"),
        selectAll("SELECT * FROM TEST ORDER BY ID"),
        selectByID("SELECT ID, NAME FROM TEST WHERE ID = ?"),
        selectCountAll("SELECT COUNT(*) FROM TEST"),
        updateID_1_Hi("UPDATE TEST SET NAME='Hi' WHERE ID=1"),
        updateID_1_PARAM("UPDATE TEST SET NAME=? WHERE ID=1"),
        deleteID_2("DELETE FROM TEST WHERE ID=2");

        final String sql;

        SqlStatements(String sql) {
            this.sql = sql;
        }

        String sql() {
            return this.sql;
        }
    }
    final ConsumerThrowingSQLException<Connection> createTable = (connection) -> {
        final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                SqlStatements.createTable.sql());
        assertEquals(0, updateCount);

    };
    final ConsumerThrowingSQLException<Connection> dropTable = (connection) -> {
        final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                SqlStatements.dropTable.sql());
        assertEquals(0, updateCount);
    };

    JdbcConnectionPool jdbcConnectionPool;

    @BeforeEach
    public void setUp() {
        final DefaultDataSourceOrConnectionCreator defaultDataSourceOrConnectionCreator = new DefaultDataSourceOrConnectionCreator();
        jdbcConnectionPool = defaultDataSourceOrConnectionCreator.createJdbcConnectionPool();
        assertNotNull(jdbcConnectionPool);
    }

    @AfterEach
    public void tearDown() {
        assertEquals(0, jdbcConnectionPool.getActiveConnections());
        jdbcConnectionPool.dispose();
    }

    @Test
    public void test_insert_query_count_resultset() throws SQLException {

        final ConsumerThrowingSQLException<Connection> insertValues = (connection) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(
                    SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                        sqlStatement.sql());
                assertEquals(1, updateCount);
            }
        };

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);

            JdbcSqlF.Connections.withTransaction(connection1, (Connection connectionInTransaction) -> {
                // 2. insert data
                insertValues.accept(connectionInTransaction);

                // 3. query rows
                final AtomicInteger ai = new AtomicInteger(0);
                JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                        SqlStatements.selectAll.sql(),
                        JdbcSqlF.ResultSets.allResultSets((rs) -> ai.incrementAndGet()));
                assertEquals(3, ai.intValue());

            });

            // 4. drop table
            dropTable.accept(connection1);
        });
    }

    @Test
    public void test_insert_query_count() throws SQLException {

        final ConsumerThrowingSQLException<Connection> insertValues = (connection) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(
                    SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                        sqlStatement.sql());
                assertEquals(1, updateCount);
            }
        };

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);

            JdbcSqlF.Connections.withTransaction(connection1, (Connection connectionInTransaction) -> {
                // 2. insert data
                insertValues.accept(connectionInTransaction);

                // 3. query rows
                Holder<Integer> hi = new Holder<>(0);
                JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                        SqlStatements.selectCountAll.sql(),
                        JdbcSqlF.ResultSets.firstResultSetOnly((rs) -> {
                            int value = rs.getInt(1);
                            hi.set(value);
                        })
                );
                assertEquals(3, hi.get());
            });

            // 4. drop table
            dropTable.accept(connection1);
        });
    }

    @Test
    public void test_insert_query_names() throws SQLException {

        final ConsumerThrowingSQLException<Connection> insertValues = (connection) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(
                    SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                        sqlStatement.sql());
                assertEquals(1, updateCount);
            }
        };

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);

            JdbcSqlF.Connections.withTransaction(connection1, (Connection connectionInTransaction) -> {
                // 2. insert data
                insertValues.accept(connectionInTransaction);

                // 3. query rows
                final List<String> names = new ArrayList<>();
                JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                        SqlStatements.selectAll.sql,
                        JdbcSqlF.ResultSets.allResultSets((rs) -> names.add(rs.getString("NAME"))));
                String m = String.format("names %s", names);
                assertAll(
                        () -> assertEquals(3, names.size(), m),
                        () -> assertTrue(names.contains("Hello")),
                        () -> assertTrue(names.contains("World")),
                        () -> assertTrue(names.contains("H2"))
                );

            });

            // 4. drop table
            dropTable.accept(connection1);
        });
    }

    @Test
    public void test_insert_query_names_with_params() throws SQLException {

        final ConsumerThrowingSQLException<Connection> insertValues = (connection) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(
                    SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                        sqlStatement.sql());
                assertEquals(1, updateCount);
            }
        };

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);

            JdbcSqlF.Connections.withTransaction(connection1, (Connection connectionInTransaction) -> {
                // 2. insert data
                insertValues.accept(connectionInTransaction);

                // 3. query row 1
                {
                    final List<String> names = new ArrayList<>();
                    JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                            SqlStatements.selectByID.sql, Arrays.asList(1),
                            JdbcSqlF.ResultSets.allResultSets((rs) -> names.add(rs.getString("NAME")))
                    );
                    final String m = String.format("names %s", names);
                    assertAll(
                            () -> assertEquals(1, names.size(), m),
                            () -> assertTrue(names.contains("Hello"))
                    );
                }
                {
                    // 4. update row 1
                    int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connectionInTransaction,
                            SqlStatements.updateID_1_PARAM.sql(),
                            Arrays.asList("HELLO ROW 1"));
                    assertEquals(1, updateCount);
                }
                // 5. query row 1 again
                {
                    final List<String> names = new ArrayList<>();
                    JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                            SqlStatements.selectByID.sql, Arrays.asList(1),
                            JdbcSqlF.ResultSets.allResultSets((rs) -> names.add(rs.getString("NAME")))
                    );
                    final String m = String.format("names %s", names);
                    assertAll(
                            () -> assertEquals(1, names.size(), m),
                            () -> assertTrue(names.contains("HELLO ROW 1"))
                    );
                }
            });

            // 6. drop table
            dropTable.accept(connection1);
        });
    }

    @Test
    public void test_insert_query_delete_names() throws SQLException {

        final ConsumerThrowingSQLException<Connection> insertValues = (connection) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(
                    SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connection,
                        sqlStatement.sql());
                assertEquals(1, updateCount);
            }
        };

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);

            JdbcSqlF.Connections.withTransaction(connection1, (Connection connectionInTransaction) -> {
                // 2. insert data
                insertValues.accept(connectionInTransaction);

                // 3. query rows
                {
                    final List<String> names = new ArrayList<>();
                    JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                            SqlStatements.selectAll.sql,
                            JdbcSqlF.ResultSets.allResultSets((rs) -> names.add(rs.getString("NAME")))
                    );
                    final String m = String.format("names %s", names);
                    assertAll(
                            () -> assertEquals(3, names.size(), m),
                            () -> assertTrue(names.contains("Hello")),
                            () -> assertTrue(names.contains("World")),
                            () -> assertTrue(names.contains("H2"))
                    );
                }
                {
                    // 4. delete row 2
                    int updateCount = JdbcSqlF.UpdateCommands.executeUpdate(connectionInTransaction,
                            SqlStatements.deleteID_2.sql());
                    assertEquals(1, updateCount);
                }
                // 5. query rows again
                {
                    final List<String> names = new ArrayList<>();
                    JdbcSqlF.ResultSetCommands.processResultSet(connectionInTransaction,
                            SqlStatements.selectAll.sql,
                            JdbcSqlF.ResultSets.allResultSets((rs) -> names.add(rs.getString("NAME")))
                    );
                    final String m = String.format("names %s", names);
                    assertAll(
                            () -> assertEquals(2, names.size(), m),
                            () -> assertTrue(names.contains("Hello")),
                            () -> assertTrue(names.contains("H2"))
                    );
                }
            });

            // 6. drop table
            dropTable.accept(connection1);
        });
    }

    @Test
    public void test_infos() throws SQLException {

        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            final Map<String, Object> connectionInfos = JdbcSqlF.Connections.getConnectionInfos().apply(connection1);
            System.out.printf("connection infos:%n%s%n", connectionInfos);
            assertEquals("PUBLIC", connectionInfos.get("schema"));
            assertEquals("TEST1", connectionInfos.get("catalog"));
        });

    }

}
