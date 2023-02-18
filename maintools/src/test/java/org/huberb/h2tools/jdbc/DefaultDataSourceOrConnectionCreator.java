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

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

/**
 *
 * @author berni3
 */
class DefaultDataSourceOrConnectionCreator {

    /**
     * create a {@link DataSource} backed by {@link JdbcConnectionPool }.
     */
    JdbcConnectionPool createJdbcConnectionPool() {
        final String url = "jdbc:h2:mem:test1";
        final String username = "sa1";
        final String password = "sa1";
        return JdbcConnectionPool.create(url, username, password);
    }

    /**
     * create a {@link DataSource} backed by a {@link JdbcDataSource}.
     */
    JdbcDataSource createJdbcDataSource() {
        final String url = "jdbc:h2:mem:test1";
        final String username = "sa1";
        final String password = "sa1";
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(url);
        ds.setUser(username);
        ds.setPassword(password);
        return ds;
    }

    /**
     * Create {@link ConnectionFactoryWithDataSource} from
     * {@link JdbcDataSource}.
     *
     * @return {@link ConnectionFactoryWithDataSource}
     */
    ConnectionFactoryWithDataSource createConnectionFactoryWithDataSource(DataSource dataSource) {
        return new ConnectionFactoryWithDataSource(dataSource);
    }

    //----
    ConnectionFactoryWithMap createConnectionFactoryWithMap() {
        final Map<String, Object> m = new HashMap<>();
        m.put("url", "jdbc:h2:mem:test1");
        m.put("user", "sa1");
        m.put("password", "sa1");
        return new ConnectionFactoryWithMap(m);
    }

}
