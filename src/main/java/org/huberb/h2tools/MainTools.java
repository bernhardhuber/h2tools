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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.h2.util.Tool;
import org.huberb.h2tools.MainToolRegistry.ToolEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Parameters;

/**
 * Simple ueber-jar main-class. Invokes main-classes of org.h2.tools.*.
 *
 * @author berni3
 */
@CommandLine.Command(name = "MainTools",
        mixinStandardHelpOptions = true,
        showAtFileInUsageHelp = true,
        showDefaultValues = true,
        version = "MainTools 0.1-SNAPSHOT",
        description = "Run H2 Tools%n"
        + "Database URLs (https://h2database.com/html/features.html#database_url) %n"
        + "%n"
        + "Embedded (https://h2database.com/html/features.html#connection_modes)%n"
        + "jdbc:h2:~/test 'test' in the user home directory%n"
        + "jdbc:h2:/data/test 'test' in the directory /data%n"
        + "jdbc:h2:./test in the current(!) working directory%n"
        + "%n"
        + "In-Memory (https://h2database.com/html/features.html#in_memory_databases)%n"
        + "jdbc:h2:mem:test multiple connections in one process%n"
        + "jdbc:h2:mem: unnamed private; one connection%n"
        + "%n"
        + "Server Mode (https://h2database.com/html/tutorial.html#using_server)%n"
        + "jdbc:h2:tcp://localhost/~/test user home dir%n"
        + "jdbc:h2:tcp://localhost//data/test absolute dir%n"
        + "Server start:java -cp *.jar org.h2.tools.Server%n"
        + "%n"
        + "Settings (https://h2database.com/html/features.html#database_url)%n"
        + "jdbc:h2:..;MODE=MySQL compatibility (or HSQLDB,...)%n"
        + "jdbc:h2:..;TRACE_LEVEL_FILE=3 log to *.trace.db%n"
        + ""
)
public class MainTools implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MainTools.class);

    /**
     * main entry point.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        final MainTools mainTools = new MainTools();
        final CommandLine commandLine = new CommandLine(mainTools);
        commandLine.setStopAtPositional(true);
        commandLine.setStopAtUnmatched(true);
        mainTools.registerCommandLine(commandLine);
        //--
        final int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Parameters(arity = "0..1",
            index = "0",
            description = "Launch H2 tool, like Shell, Script, RunScript, etc.")
    private String toolName;

    //---
    private CommandLine commandLine;
    private final MainToolRegistry mainToolRegistry;

    public MainTools() {
        this.mainToolRegistry = new MainToolRegistry();
    }

    void registerCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * Picocli entry point.
     * 
     * @return application exist status.
     * @throws Exception 
     */
    @Override
    public Integer call() throws Exception {
        final List<String> toolOptions = commandLine.getUnmatchedArguments();
        final List<String> argsAsList = toolOptions == null
                ? Collections.emptyList()
                : toolOptions;
        final String[] args = argsAsList.toArray(new String[argsAsList.size()]);
        //---
        if (toolName == null) {
            loggerInfo("Missing command ");
            printAvailableClasses();
            return -1;
        }
        final String command = toolName;
        final String[] argsRemaining = args;
        final Optional<Class<? extends Tool>> mClassOptional = mainToolRegistry.findGetOrDefault(command);
        if (mClassOptional.isPresent()) {
            final Class<? extends Tool> mClass = mClassOptional.get();
            final Method method = mClass.getMethod("main", String[].class);
            method.invoke(null, (Object) argsRemaining);
        } else {
            loggerInfo("Undefined command `%s'", command);
            printAvailableClasses();
            return -1;
        }
        return 0;
    }

    void printAvailableClasses() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available commands\n\nCommand Name, Class, Description\n");
        for (ToolEntry te : mainToolRegistry.retrieveIterableOfToolClassesWithMain()) {
            final String simpleName = te.name;
            final Class clazz = te.clazz;
            final String description = te.description;
            sb.append(String.format("@|bold %s|@, %s,\n%s\n\n", simpleName, clazz, description));
        }
        final String str = Ansi.AUTO.string(sb.toString());
        loggerInfo(str);
    }

    void loggerInfo(String fmt, Object... args) {
        final String m = String.format(fmt, args);
        logger.info(m);
    }

}
