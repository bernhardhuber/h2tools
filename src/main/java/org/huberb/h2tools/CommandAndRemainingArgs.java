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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utility to manipulate {@code String[] args}, by removing first argument.
 *
 * @author berni3
 */
class CommandAndRemainingArgs {

    static class Value {

        String command;
        String[] remainingArgs;

        public Value(String command, String[] remainingArgs) {
            this.command = command;
            this.remainingArgs = remainingArgs;
        }

    }
    private Value value;

    CommandAndRemainingArgs build(String[] args) {
        final List<String> argsAsList = Arrays.asList(args);
        final String commandLowerCase;
        final List<String> argsRemainingAsList;
        if (argsAsList.size() > 1) {
            commandLowerCase = argsAsList.get(0).toLowerCase();
            argsRemainingAsList = argsAsList.subList(1, argsAsList.size());
        } else if (argsAsList.size() == 1) {
            commandLowerCase = argsAsList.get(0).toLowerCase();
            argsRemainingAsList = Collections.emptyList();
        } else {
            commandLowerCase = "";
            argsRemainingAsList = Collections.emptyList();
        }

        final String[] argsRemaining = argsRemainingAsList.toArray(new String[argsRemainingAsList.size()]);
        this.value = new Value(commandLowerCase, argsRemaining);
        return this;
    }

    Value getValue() {
        return this.value;
    }

}
