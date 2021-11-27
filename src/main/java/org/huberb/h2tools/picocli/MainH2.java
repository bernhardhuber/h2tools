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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import org.h2.util.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 *
 * @author pi
 */
@CommandLine.Command(name = "MainH2",
        subcommands = {
            ScriptSubCommand.class,
            CsvReadSubCommand.class,
            CsvWriteSubCommand.class,
            ShowSubCommand.class,},
        mixinStandardHelpOptions = true,
        showAtFileInUsageHelp = true,
        showDefaultValues = true,
        version = "MainH2 0.1-SNAPSHOT",
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
public class MainH2 implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(MainH2.class);

    @CommandLine.Option(names = {"--user"}, defaultValue = "sa",
            paramLabel = "USER",
            required = false,
            description = "h2 user")
    private String userName;
    @CommandLine.Option(names = {"--password"}, defaultValue = "",
            paramLabel = "PASSWORD",
            required = false,
            description = "h2 password")
    private String password;
    @CommandLine.Option(names = {"--url"}, defaultValue = "jdbc:h2:mem:/test1",
            paramLabel = "URL",
            required = false,
            description = "h2 jdbc URL")
    private String jdbcURL;
    @CommandLine.Option(names = {"--driver"}, defaultValue = "org.h2.Driver",
            paramLabel = "DRIVER",
            required = false,
            description = "jdbc Driver")
    private String jdbcDriver;

    /**
     * application main entry point.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        final int exitCode = new CommandLine(new MainH2()).execute(args);
        System.exit(exitCode);
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getJdbcURL() {
        return jdbcURL;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    Connection createConnection() throws SQLException {
        logger.info(String.format("Create connection url %s, user %s, password %s", jdbcDriver, jdbcURL, userName, password));
        final Connection connection = JdbcUtils.getConnection(jdbcDriver, jdbcURL, userName, password);
        return connection;
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }

}
