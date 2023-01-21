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
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class JdbcSqlFTest {

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
    DataSource jdbcConnectionPool;

    @BeforeEach
    public void setUp() {
        final DefaultDataSourceOrConnectionCreator defaultDataSourceOrConnectionCreator = new DefaultDataSourceOrConnectionCreator();
        jdbcConnectionPool = defaultDataSourceOrConnectionCreator.createJdbcConnectionPool();
        assertNotNull(jdbcConnectionPool);
    }

    @Test
    public void hello() throws SQLException {
        JdbcSqlF jdbcSqlF = new JdbcSqlF();

        final ConsumerThrowingSQLException<Connection> createTable = (connection2) -> {
            final int updateCount = jdbcSqlF.executeUpdate(connection2,
                    SqlStatements.createTable.sql(),
                    JdbcSqlF.PreparedStatements.noop());
            assertEquals(0, updateCount);

        };
        final ConsumerThrowingSQLException<Connection> insertValues = (connection2) -> {
            //---
            for (SqlStatements sqlStatement : EnumSet.of(SqlStatements.insertID_1,
                    SqlStatements.insertID_2,
                    SqlStatements.insertID_3)) {
                final int updateCount = jdbcSqlF.executeUpdate(connection2,
                        sqlStatement.sql(),
                        JdbcSqlF.PreparedStatements.noop());
                assertEquals(1, updateCount);
            }
        };
        final ConsumerThrowingSQLException<Connection> dropTable = (connection2) -> {
            final int updateCount = jdbcSqlF.executeUpdate(connection2,
                    SqlStatements.dropTable.sql(),
                    JdbcSqlF.PreparedStatements.noop());
            assertEquals(0, updateCount);
        };


        JdbcSqlF.Connections.withDataSource(jdbcConnectionPool, (Connection connection1) -> {
            // 1. create table
            createTable.accept(connection1);
            JdbcSqlF.Connections.withTransaction(connection1,
                    (Connection connection2) -> {
                        // 2. insert data
                        insertValues.accept(connection2);

                        // 3. query rows
                        final AtomicInteger ai = new AtomicInteger(0);
                        jdbcSqlF.processResultSet(connection2,
                                SqlStatements.selectAll.sql,
                                JdbcSqlF.ResultSetConsumers.f1((rs) -> ai.incrementAndGet()));
                        assertEquals(3, ai.intValue());
                    });
            // 4. drop table
            dropTable.accept(connection1);
        }
        );
    }
}
