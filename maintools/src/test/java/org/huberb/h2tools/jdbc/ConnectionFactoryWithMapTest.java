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

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author berni3
 */
public class ConnectionFactoryWithMapTest {

    @Test
    public void given_mem_url_user_password_then_connect_to_h2_database() throws SQLException {
        final Map<String, Object> m = new HashMap<>();
        m.put("url", "jdbc:h2:mem:test1");
        m.put("user", "sa1");
        m.put("password", "sa1");
        final ConnectionFactoryWithMap connectionFactoryWithMap = new ConnectionFactoryWithMap(m);
        try (Connection connection = connectionFactoryWithMap.createConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertFalse(connection.isReadOnly());
            assertTrue(connection.isValid(5));

            System.out.printf("ClientInfo %s%n", connection.getClientInfo());
            System.out.printf("MetaData %s%n", connection.getMetaData());
        }
    }

    @Test
    public void given_mem_then_connect_to_h2_database() throws SQLException {
        final Map<String, Object> m = new HashMap<>();
        m.put("url", "jdbc:h2:mem:test1");
        final ConnectionFactoryWithMap connectionFactoryWithMap = new ConnectionFactoryWithMap(m);
        try (Connection connection = connectionFactoryWithMap.createConnection()) {
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertFalse(connection.isReadOnly());
            assertTrue(connection.isValid(5));

            System.out.printf("ClientInfo %s%n", connection.getClientInfo());
            System.out.printf("MetaData %s%n", connection.getMetaData());
        }
    }
}
