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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.h2.tools.SimpleResultSet;
import org.huberb.h2tools.support.OutputResultSet.OutputByArrayOfArraysJson;
import org.huberb.h2tools.support.OutputResultSet.OutputByCsv;
import org.huberb.h2tools.support.OutputResultSet.OutputByJson;
import org.huberb.h2tools.support.OutputResultSet.OutputByRaw;
import org.huberb.h2tools.support.OutputResultSet.OutputByTabular;
import org.huberb.h2tools.support.OutputResultSet.OutputByYaml;
import org.huberb.h2tools.support.OutputResultSet.OutputMode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author pi
 */
public class OutputResultSetTest {

    /**
     * Test of FindOutputMode method, of class OutputMode.
     */
    @Test
    public void test_OutputMode_FindOutputMode() {
        assertAll(
                () -> assertEquals(OutputMode.CSV, OutputMode.findOutputMode(OutputMode.CSV.name()).get()),
                () -> assertEquals(OutputMode.JSON, OutputMode.findOutputMode(OutputMode.JSON.name()).get()),
                () -> assertEquals(OutputMode.JSON_ARRAYS, OutputMode.findOutputMode(OutputMode.JSON_ARRAYS.name()).get()),
                () -> assertEquals(OutputMode.JSON_MAPS, OutputMode.findOutputMode(OutputMode.JSON_MAPS.name()).get()),
                () -> assertEquals(OutputMode.RAW, OutputMode.findOutputMode(OutputMode.RAW.name()).get()),
                () -> assertEquals(OutputMode.TABULAR, OutputMode.findOutputMode(OutputMode.TABULAR.name()).get()),
                () -> assertEquals(OutputMode.YAML, OutputMode.findOutputMode(OutputMode.YAML.name()).get()),
                () -> assertEquals(false, OutputMode.findOutputMode("XXX").isPresent())
        );
    }

    /**
     * Test of FindOutputMode method, of class OutputMode.
     */
    @Test
    public void test_OutputMode_CreateOuputBy() {
        assertAll(
                () -> assertEquals(OutputResultSet.OutputByCsv.class, OutputMode.createOutputBy(OutputMode.CSV).getClass()),
                () -> assertEquals(OutputResultSet.OutputByJson.class, OutputMode.createOutputBy(OutputMode.JSON).getClass()),
                () -> assertEquals(OutputResultSet.OutputByArrayOfArraysJson.class, OutputMode.createOutputBy(OutputMode.JSON_ARRAYS).getClass()),
                () -> assertEquals(OutputResultSet.OutputByJson.class, OutputMode.createOutputBy(OutputMode.JSON_MAPS).getClass()),
                () -> assertEquals(OutputResultSet.OutputByRaw.class, OutputMode.createOutputBy(OutputMode.RAW).getClass()),
                () -> assertEquals(OutputResultSet.OutputByTabular.class, OutputMode.createOutputBy(OutputMode.TABULAR).getClass()),
                () -> assertEquals(OutputResultSet.OutputByYaml.class, OutputMode.createOutputBy(OutputMode.YAML).getClass()),
                () -> assertEquals(OutputResultSet.OutputByCsv.class, OutputMode.createOutputBy(OutputMode.CSV).getClass()),
                () -> assertEquals(OutputResultSet.OutputByCsv.class, OutputMode.createOutputBy(OutputMode.CSV).getClass()),
                () -> assertEquals(OutputResultSet.OutputByCsv.class, OutputMode.createOutputBy(OutputMode.CSV).getClass())
        );
    }

    @Test
    public void test_OutputByRaw_output() throws SQLException, IOException {
        final OutputByRaw instance = new OutputByRaw();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }
            {
                final String resultFinal = result;
                Arrays.asList(
                        "ID: 0",
                        "NAME: Hello",
                        "ID: 1",
                        "NAME: World")
                        .forEach((String s) -> {
                            assertTrue(resultFinal.contains(s), resultFinal);
                        });
            }
        }
    }

    @Test
    public void test_OutputByCsv_output() throws SQLException, IOException {
        final OutputByCsv instance = new OutputByCsv();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }
            {
                final String resultFinal = result;
                assertAll(
                        () -> assertTrue(resultFinal.contains("\"ID\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"NAME\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"0\",\"Hello\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"1\",\"World\""), resultFinal)
                );
            }
        }
    }

    @Test
    public void test_OutputByJson_output() throws SQLException, IOException {
        final OutputByJson instance = new OutputByJson();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }
            {
                final String resultFinal = result;
                assertAll(
                        () -> assertTrue(resultFinal.contains("\"ID\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"NAME\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"0\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"Hello\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"1\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"World\""), resultFinal)
                );
            }
        }
    }

    @Test
    public void test_OutputByArrayOfArrayJson_output() throws SQLException, IOException {
        final OutputByArrayOfArraysJson instance = new OutputByArrayOfArraysJson();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }
            {
                final String resultFinal = result;
                assertAll(
                        () -> assertTrue(resultFinal.contains("\"ID\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"NAME\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"0\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"Hello\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"1\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"World\""), resultFinal)
                );
            }
        }
    }

    @Test
    public void test_OutputByYaml_output() throws SQLException, IOException {
        final OutputByYaml instance = new OutputByYaml();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }
            {
                final String resultFinal = result;
                assertAll(
                        () -> assertTrue(resultFinal.contains(""
                                + "## YAML\n"
                                + "---\n"
                                + "[\n" + ""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"ID\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"NAME\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"0\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"Hello\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"1\""), resultFinal),
                        () -> assertTrue(resultFinal.contains("\"World\""), resultFinal)
                );
            }
        }
    }

    @Test
    public void test_OutputByTabular_output() throws SQLException, IOException {
        final OutputByTabular instance = new OutputByTabular();
        try (final ResultSet rs = createSimpleResultSet()) {
            String result = "";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos)) {

                instance.output(rs, ps);
                ps.flush();

                result = baos.toString(StandardCharsets.UTF_8);
            }

            {
                final String resultFinal = result;
                assertAll(
                        () -> assertTrue(resultFinal.contains("ID, NAME"), resultFinal),
                        () -> assertTrue(resultFinal.contains("0, Hello"), resultFinal),
                        () -> assertTrue(resultFinal.contains("1, World"), resultFinal));
            }
        }
    }

    //---
    ResultSet createSimpleResultSet() {
        SimpleResultSet rs = new SimpleResultSet();
        rs.addColumn("ID", Types.INTEGER, 10, 0);
        rs.addColumn("NAME", Types.VARCHAR, 255, 0);
        rs.addRow(0, "Hello");
        rs.addRow(1, "World");
        return rs;
    }
}
