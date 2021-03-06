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
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * Create a jdbc connection from a {@link DataSource}.
 *
 * @author berni3
 * @see Connection
 * @see DataSource
 */
public class ConnectionFactoryWithDataSource implements IConnectionFactory {

    final DataSource dataSource;

    public ConnectionFactoryWithDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection createConnection() throws SQLException {
        final Connection connection = this.dataSource.getConnection();
        return connection;
    }

}
