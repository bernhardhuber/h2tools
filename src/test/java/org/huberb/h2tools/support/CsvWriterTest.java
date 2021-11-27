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

import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.h2.tools.SimpleResultSet;
import org.huberb.h2tools.support.CsvReadWriteWrappers.CsvWriter;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author pi
 */
public class CsvWriterTest {

    public CsvWriterTest() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @ParameterizedTest
    @MethodSource(value = "createSimpleResultSet")
    public void testWriteTo_ResultSet(final ResultSet resultSet) throws IOException {
        try (StringWriter sw = new StringWriter()) {
            final Map<String, String> options = Collections.emptyMap();
            try (final CsvWriter csvWriter = new CsvWriter(sw, options)) {
                final int writtenCount = csvWriter.writeTo(resultSet);
                assertEquals(2, writtenCount);
            }
            final String m = "" + sw.toString();
            String csvString = sw.toString();
            assertTrue(csvString.contains("\"ID\",\"NAME\""), m);
            assertTrue(csvString.contains("\"0\",\"Hello\""), m);
            assertTrue(csvString.contains("\"1\",\"World\""), m);
        }
    }

    static Stream<ResultSet> createSimpleResultSet() {

        final SimpleResultSet simpleResultSet_0_1 = new SimpleResultSet();
        simpleResultSet_0_1.addColumn("ID", Types.INTEGER, 10, 0);
        simpleResultSet_0_1.addColumn("NAME", Types.VARCHAR, 255, 0);
        simpleResultSet_0_1.addRow(0, "Hello");
        simpleResultSet_0_1.addRow(1, "World");
        //---

        return Arrays.asList(
                (ResultSet) simpleResultSet_0_1)
                .stream();
    }
}
