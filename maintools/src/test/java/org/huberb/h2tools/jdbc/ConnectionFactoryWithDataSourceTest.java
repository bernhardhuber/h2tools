/*
 * Copyright 2020 berni3.
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
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.huberb.h2tools.jdbc.JdbcSql.ConnectionFactoryWithDataSource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class ConnectionFactoryWithDataSourceTest {

    @Test
    public void given_a_h2_jdbc_connection_pool_then_connect_to_this_h2_database() throws SQLException {
        final JdbcConnectionPool cp = createJdbcConnectionPool();
        try {
            final ConnectionFactoryWithDataSource connectionFactoryWithDataSource = new ConnectionFactoryWithDataSource(cp);

            try (Connection connection = connectionFactoryWithDataSource.createConnection()) {
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));

                System.out.printf("%n---%nClientInfo %s%n"
                        + "MetaData %s%n"
                        + "DatabaseMetaDataInfo %s%n",
                        connection.getClientInfo(),
                        connection.getMetaData(),
                        databaseMetaDataInfo(connection.getMetaData())
                );
            }

        } finally {
            assertEquals(0, cp.getActiveConnections());
            cp.dispose();
        }
    }

    @Test
    public void given_a_h2_data_source_connection_then_connect_to_this_h2_database() throws SQLException {
        final DataSource cp = createDataSource();
        final ConnectionFactoryWithDataSource connectionFactoryWithDataSource = new ConnectionFactoryWithDataSource(cp);

        try (Connection connection = connectionFactoryWithDataSource.createConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertFalse(connection.isReadOnly());
            assertTrue(connection.isValid(5));

            System.out.printf("%n---%nClientInfo %s%n"
                    + "MetaData %s%n"
                    + "DatabaseMetaDataInfo %s%n",
                    connection.getClientInfo(),
                    connection.getMetaData(),
                    databaseMetaDataInfo(connection.getMetaData())
            );
        }
    }

    JdbcConnectionPool createJdbcConnectionPool() {
        final String url = "jdbc:h2:mem:test1";
        final String username = "sa1";
        final String password = "sa1";
        final JdbcConnectionPool cp = JdbcConnectionPool.create(url, username, password);
        return cp;
    }

    DataSource createDataSource() {
        final String url = "jdbc:h2:mem:test1";
        final String username = "sa1";
        final String password = "sa1";
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(username);
        ds.setPassword(password);
        return ds;
    }

    String databaseMetaDataInfo(DatabaseMetaData dmd) throws SQLException {
        return String.format(""
                + "DatabaseMajorVersion %d, "
                + "DatabaseMinorVersion %d%n"
                + "DatabaseProductName %s, "
                + "DatabaseProductVersion %s%n"
                + "DriverName %s, "
                + "DriverVersion %s"
                + "",
                dmd.getDatabaseMajorVersion(),
                dmd.getDatabaseMinorVersion(),
                dmd.getDatabaseProductName(),
                dmd.getDatabaseProductVersion(),
                dmd.getDriverName(),
                dmd.getDriverVersion(),
                ""
        );
    }
}
