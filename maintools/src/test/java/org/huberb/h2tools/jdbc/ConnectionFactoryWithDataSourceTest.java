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
import java.sql.SQLException;
import org.h2.jdbcx.JdbcConnectionPool;
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
    public void given_a_h2_connection_then_connect_to_this_h2_database() throws SQLException {
        final JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:mem:test1", "sa1", "sa1");
        try {
            final ConnectionFactoryWithDataSource connectionFactoryWithDataSource = new ConnectionFactoryWithDataSource(cp);

            try (Connection connection = connectionFactoryWithDataSource.createConnection()) {
                assertNotNull(connection);
                assertFalse(connection.isClosed());
                assertFalse(connection.isReadOnly());
                assertTrue(connection.isValid(5));

                System.out.printf("ClientInfo %s%n", connection.getClientInfo());
                System.out.printf("MetaData %s%n", connection.getMetaData());
            }

        } finally {
            assertEquals(0, cp.getActiveConnections());
            cp.dispose();
        }

    }

}
