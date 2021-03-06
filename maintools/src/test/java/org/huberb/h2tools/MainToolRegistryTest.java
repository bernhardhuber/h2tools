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
package org.huberb.h2tools;

import java.util.Optional;
import org.h2.tools.Server;
import org.h2.util.Tool;
import org.huberb.h2tools.MainToolRegistry.ToolEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author berni3
 */
public class MainToolRegistryTest {

    @ParameterizedTest
    @ValueSource(strings = {"SERVER", "server", "Server"})
    public void testFindGetOrDefault_server(String command) {
        final MainToolRegistry instance = new MainToolRegistry();

        final String m = "" + command;
        final Optional<Class<? extends Tool>> cOptional = instance.findGetOrDefault(command);
        assertNotNull(cOptional, m);
        assertTrue(cOptional.isPresent(), m);
        assertEquals(Server.class.getName(), cOptional.get().getName(), m);
    }

    @Test
    public void testRetrieveIterableOfToolClassesWithMain() {
        final MainToolRegistry instance = new MainToolRegistry();
        final Iterable<ToolEntry> it = instance.retrieveIterableOfToolClassesWithMain();
        assertNotNull(it);

        int i = 0;
        for (ToolEntry te : it) {
            i += 1;
        }
        assertEquals(12, i);
    }
}
