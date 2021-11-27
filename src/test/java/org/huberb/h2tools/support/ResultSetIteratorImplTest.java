/*
 * Copyright 2021 pi.
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
package org.huberb.h2tools.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.stream.Stream;
import org.h2.tools.SimpleResultSet;
import org.huberb.h2tools.support.ResultSetIterableIterators.ResultSetIteratorImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author pi
 */
public class ResultSetIteratorImplTest {

    @ParameterizedTest
    @MethodSource(value = "createEmptySimpleResultSet")
    public void testHasNextEmptyResultSet(final ResultSet resultSet) throws SQLException {
        final ResultSetIteratorImpl instance = new ResultSetIteratorImpl(resultSet);
        assertFalse(instance.hasNext());
        assertNotNull(instance.resultSet);
        assertFalse(instance.resultSet.next());
    }

    @ParameterizedTest
    @MethodSource(value = "createSimpleResultSet")
    public void testHasNextVaryResultSets(final ResultSet resultSet) throws SQLException {
        final ResultSetIteratorImpl instance = new ResultSetIteratorImpl(resultSet);
        assertTrue(instance.hasNext());
        final ResultSet firstRow = instance.next();
        assertSame(instance.resultSet, firstRow);
        assertEquals(0, firstRow.getInt(1));
        assertEquals("Hello", firstRow.getString(2));
    }

    static Stream<ResultSet> createEmptySimpleResultSet() {
        final SimpleResultSet simpleResultSet_ = new SimpleResultSet();
        simpleResultSet_.addColumn("ID", Types.INTEGER, 10, 0);
        simpleResultSet_.addColumn("NAME", Types.VARCHAR, 255, 0);
        return Arrays.asList(
                (ResultSet) simpleResultSet_)
                .stream();
    }

    static Stream<ResultSet> createSimpleResultSet() {
        final SimpleResultSet simpleResultSet_0 = new SimpleResultSet();
        simpleResultSet_0.addColumn("ID", Types.INTEGER, 10, 0);
        simpleResultSet_0.addColumn("NAME", Types.VARCHAR, 255, 0);
        simpleResultSet_0.addRow(0, "Hello");

        final SimpleResultSet simpleResultSet_0_1 = new SimpleResultSet();
        simpleResultSet_0_1.addColumn("ID", Types.INTEGER, 10, 0);
        simpleResultSet_0_1.addColumn("NAME", Types.VARCHAR, 255, 0);
        simpleResultSet_0_1.addRow(0, "Hello");
        simpleResultSet_0_1.addRow(1, "World");
        //---

        return Arrays.asList(
                (ResultSet) simpleResultSet_0,
                (ResultSet) simpleResultSet_0_1)
                .stream();
    }
}
