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

import org.huberb.h2tools.CommandAndRemainingArgs.Value;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author berni3
 */
public class CommandAndRemainingArgsTest {

    @Test
    public void testBuild_command_noarg() {
        CommandAndRemainingArgs instance = new CommandAndRemainingArgs();
        Value value = instance.build(new String[]{"command"}).getValue();
        assertEquals("command", value.command);
        assertEquals(0, value.remainingArgs.length);
    }

    @Test
    public void testBuild_nocommand_noarg() {
        CommandAndRemainingArgs instance = new CommandAndRemainingArgs();
        Value value = instance.build(new String[]{}).getValue();
        assertEquals("", value.command);
        assertEquals(0, value.remainingArgs.length);
    }

    @Test
    public void testBuild_command_arg1() {
        CommandAndRemainingArgs instance = new CommandAndRemainingArgs();
        Value value = instance.build(new String[]{"command", "arg1"}).getValue();
        assertEquals("command", value.command);
        assertEquals(1, value.remainingArgs.length);
        assertEquals("arg1", value.remainingArgs[0]);
    }

}
