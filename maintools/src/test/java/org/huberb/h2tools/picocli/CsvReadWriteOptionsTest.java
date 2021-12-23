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
package org.huberb.h2tools.picocli;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

/**
 *
 * @author pi
 */
public class CsvReadWriteOptionsTest {

    public CsvReadWriteOptionsTest() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of createOptionsMap method, of class CsvReadWriteOptions.
     */
    @Test
    public void testCreateOptionsMap() {
    }

    /**
     * Test of createOptionsString method, of class CsvReadWriteOptions.
     */
    @Test
    public void testCreateOptionsString() {
        final String[] args = new String[0];
        final CsvReadWriteOptions instance = new CsvReadWriteOptions();
        final ParseResult parseResult = new CommandLine(instance).parseArgs(args);
        assertTrue(parseResult.errors().isEmpty());

        final String s = instance.createOptionsString();
        //System.out.println("optionsString #1 " + s);
        // optionsString #1 charset=UTF-8 fieldSeparator=, null= preserveWhitespace=false fieldDelimiter=" lineComment= lineSeparator=
        // escape=" writeColumnHeader=true caseSensitiveColumnNames=false 
        assertTrue(s.contains("charset=UTF-8"), s);
        assertTrue(s.contains("fieldSeparator=,"), s);
        assertTrue(s.contains("preserveWhitespace=false"), s);
        assertTrue(s.contains("fieldDelimiter=\""), s);
        assertTrue(s.contains("writeColumnHeader=true"), s);
        assertTrue(s.contains("caseSensitiveColumnNames=false"), s);
    }

    /**
     * Test of createOptionsString method, of class CsvReadWriteOptions.
     */
    @Test
    public void testCreateOptionsString_no_writeColumnHeader() {
        final String[] args = new String[]{"--no-write-column-header"};
        final CsvReadWriteOptions instance = new CsvReadWriteOptions();
        final ParseResult parseResult = new CommandLine(instance).parseArgs(args);
        assertTrue(parseResult.errors().isEmpty());

        final String s = instance.createOptionsString();
        assertTrue(s.contains("charset=UTF-8"), s);
        assertTrue(s.contains("fieldSeparator=,"), s);
        assertTrue(s.contains("preserveWhitespace=false"), s);
        assertTrue(s.contains("fieldDelimiter=\""), s);
        assertTrue(s.contains("writeColumnHeader=false"), s);
        assertTrue(s.contains("caseSensitiveColumnNames=false"), s);
    }

    /**
     * Test of createOptionsString method, of class CsvReadWriteOptions.
     *
     * @param fieldSeparatorValue
     */
    @ParameterizedTest
    @ValueSource(strings = {",", ";", "|", "\t"})
    public void testCreateOptionsString_vary_fieldSeparator(String fieldSeparatorValue) {
        final String[] args = new String[]{"--field-separator=" + fieldSeparatorValue};
        final CsvReadWriteOptions instance = new CsvReadWriteOptions();
        final ParseResult parseResult = new CommandLine(instance).parseArgs(args);
        assertTrue(parseResult.errors().isEmpty());

        final String s = instance.createOptionsString();
        assertTrue(s.contains("charset=UTF-8"), s);
        assertTrue(s.contains("fieldSeparator=" + fieldSeparatorValue), s);
        assertTrue(s.contains("preserveWhitespace=false"), s);
        assertTrue(s.contains("fieldDelimiter=\""), s);
        assertTrue(s.contains("writeColumnHeader=true"), s);
        assertTrue(s.contains("caseSensitiveColumnNames=false"), s);
    }

}
