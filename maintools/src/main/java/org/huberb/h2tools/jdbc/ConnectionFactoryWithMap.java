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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Create a jdbc connection from a {@link DriverManager}.
 *
 * @author berni3
 * @see Connection
 * @see DriverManager
 */
public class ConnectionFactoryWithMap implements IConnectionFactory {

    final Map<String, Object> m;

    public ConnectionFactoryWithMap(Map<String, Object> m) {
        this.m = m;
    }

    Optional<String> extractUrl() {
        return Optional.ofNullable((String) m.getOrDefault("url", null));
    }

    Optional<String[]> extractUserPassword() {
        final Optional<String[]> userPasswordOptional;
        final String user = (String) m.getOrDefault("user", null);
        final String password = (String) m.getOrDefault("password", null);
        if (user != null && password != null) {
            userPasswordOptional = Optional.of(new String[]{user, password});
        } else {
            userPasswordOptional = Optional.empty();
        }
        return userPasswordOptional;
    }

    Optional<Properties> extractProperties() {
        return Optional.ofNullable((Properties) m.getOrDefault("properties", null));
    }

    @Override
    public Connection createConnection() throws SQLException {
        final Connection connection;
        final Optional<String> urlOptional = extractUrl();
        final Optional<String[]> userPasswordOptional = extractUserPassword();
        final Optional<Properties> propertiesOptional = extractProperties();
        if (urlOptional.isPresent()) {
            if (propertiesOptional.isPresent()) {
                connection = DriverManager.getConnection(urlOptional.get(), propertiesOptional.get());
            } else if (userPasswordOptional.isPresent()) {
                connection = DriverManager.getConnection(urlOptional.get(), userPasswordOptional.get()[0], userPasswordOptional.get()[1]);
            } else {
                connection = DriverManager.getConnection(urlOptional.get());
            }
        } else {
            throw new SQLException("Cannot create connection from " + m);
        }
        return connection;
    }

}
