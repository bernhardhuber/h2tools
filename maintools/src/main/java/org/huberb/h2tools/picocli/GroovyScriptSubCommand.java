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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 *
 * @author berni3
 */
@CommandLine.Command(name = "groovy",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Run groovy script")
public class GroovyScriptSubCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptSubCommand.class);

    // picocli injects reference to parent command
    @CommandLine.ParentCommand
    private MainH2 mainH2;

    //--- to file
    @CommandLine.Option(names = {"--groovy-script"},
            defaultValue = "process.groovysh",
            required = true,
            description = "Specify groovy script file name to run")
    private File groovyScriptFile;

    @Override
    public Integer call() throws Exception {
        logger.info("Args {}", this.groovyScriptFile);
        //---
        process();
        return 0;
    }

    void process() throws CompilationFailedException, IOException {
        final Binding binding = new Binding();
        final GroovyShell groovyShell = new GroovyShell(binding, CompilerConfiguration.DEFAULT);
        groovyShell.evaluate(this.groovyScriptFile);
    }
}
