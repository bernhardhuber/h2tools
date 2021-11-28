/*
 * Copyright 2021 berni3.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

/**
 *
 * @author berni3
 */
public class MainH2Test {

    MainH2 app;
    CommandLine cmd;
    StringWriter swOut;
    StringWriter swErr;

    @BeforeEach
    public void setUp() {
        app = new MainH2();
        cmd = new CommandLine(app);

        swOut = new StringWriter();
        cmd.setOut(new PrintWriter(swOut));

        swErr = new StringWriter();
        cmd.setErr(new PrintWriter(swErr));
        //---
    }

    @ParameterizedTest
    @ValueSource(strings = {"--help", "-h"})
    public void testCommandLine_help(String helpOption) {
        //---
        final int exitCode = cmd.execute(helpOption);
        assertEquals(0, exitCode);
        assertEquals("", swErr.toString(), "stderr");
        final String swErrAsString = swOut.toString();
        final String m = String.format("stdout helpOption %s, stderr: %s", helpOption, swErrAsString);
        assertNotEquals(0, swErrAsString, m);
        assertTrue(swErrAsString.contains("Usage:"), m);
        assertTrue(swErrAsString.contains("script"), m);
        assertTrue(swErrAsString.contains("csvRead"), m);
        assertTrue(swErrAsString.contains("csvWrite"), m);
        assertTrue(swErrAsString.contains("show"), m);
        assertTrue(swErrAsString.contains("-h"), m);
        assertTrue(swErrAsString.contains("--help"), m);
        assertTrue(swErrAsString.contains("-V"), m);
        assertTrue(swErrAsString.contains("--version"), m);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--version", "-V"})
    public void testCommandLine_version(String versionOption) {
        //---
        final int exitCode = cmd.execute(versionOption);
        assertEquals(0, exitCode);
        assertEquals("", swErr.toString(), "stderr");
        final String swErrAsString = swOut.toString();
        final String m = String.format("stdout versionOption %s, stderr: %s", versionOption, swErrAsString);
        assertNotEquals(0, swErrAsString, m);
        assertTrue(swErrAsString.contains("MainH2"), m);
    }
}
