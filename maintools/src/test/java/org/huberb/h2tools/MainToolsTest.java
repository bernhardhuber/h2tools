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
package org.huberb.h2tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

/**
 *
 * @author berni3
 */
public class MainToolsTest {

    MainTools app;
    CommandLine cmd;
    StringWriter swErr;
    StringWriter swOut;

    @BeforeEach
    public void setUp() {
        app = new MainTools();
        cmd = new CommandLine(app);

        swOut = new StringWriter();
        cmd.setOut(new PrintWriter(swOut));

        swErr = new StringWriter();
        cmd.setErr(new PrintWriter(swErr));
        //---
    }

    @AfterEach
    public void teardDown() throws IOException {
        swErr.close();
        swOut.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"--help", "-h"})
    public void testCommandLine_help(String helpOption) {
        //---
        final int exitCode = cmd.execute(helpOption);
        assertEquals(0, exitCode);
        {
            assertEquals("", swErr.toString(), "stderr");
        }
        {
            final String swOutAsString = swOut.toString();
            final String m = String.format("stdout helpOption %s, stdout: %s", helpOption, swOutAsString);
            assertNotEquals("", swOutAsString, m);
            assertAll(
                    () -> assertTrue(swOutAsString.contains("Usage:"), m),
                    () -> assertTrue(swOutAsString.contains("toolName"), m),
                    () -> assertTrue(swOutAsString.contains("-h"), m),
                    () -> assertTrue(swOutAsString.contains("--help"), m),
                    () -> assertTrue(swOutAsString.contains("-V"), m),
                    () -> assertTrue(swOutAsString.contains("--version"), m));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"--version", "-V"})
    public void testCommandLine_version(String versionOption) {
        //---
        final int exitCode = cmd.execute(versionOption);
        assertEquals(0, exitCode);
        {
            assertEquals("", swErr.toString(), "stderr");
        }
        {
            final String swOutAsString = swOut.toString();
            final String m = String.format("stdout versionOption %s, stdout: %s", versionOption, swOutAsString);
            assertNotEquals("", swOutAsString, m);
            assertTrue(swOutAsString.contains("MainTools"), m);
        }
    }

    @Test
    public void testCommandLine_tools() {
        //---
        cmd.setStopAtPositional(true);
        cmd.setStopAtUnmatched(true);
        app.registerCommandLine(cmd);

        //---
        String emptyOption = "";
        final int exitCode = cmd.execute(emptyOption);
        assertEquals(-1, exitCode);
        {
            assertEquals("", swErr.toString(), "stderr");
        }
        {
            final String swOutAsString = swOut.toString();
            final String m = String.format("stdout option %s, stdout: %s", emptyOption, swOutAsString);
            assertNotEquals("", swOutAsString, m);

            Arrays.asList(
                    "Backup",
                    "ChangeFileEncryption",
                    "Console",
                    "ConvertTraceFile",
                    "CreateCluster",
                    "DeleteDbFiles",
                    "Recover",
                    "Restore",
                    "RunScript",
                    "Script",
                    "Server",
                    "Shell").forEach((final String s) -> {
                        final String m2 = String.format("Expecting: %s in stdout output: %s", s, m);
                        assertTrue(swOutAsString.contains(s), m2);
                    });
        }
    }
}
