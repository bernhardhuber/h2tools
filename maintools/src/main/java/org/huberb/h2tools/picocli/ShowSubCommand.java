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
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.huberb.h2tools.support.OutputResultSet.OutputBy;
import org.huberb.h2tools.support.OutputResultSet.OutputMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;

/**
 * Lists the schemas, tables, or the columns of a table.
 *
 * @author pi
 */
@CommandLine.Command(name = "show",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Lists the schemas, tables, or the columns of a table.")
public class ShowSubCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ShowSubCommand.class);

    // picocli injects reference to parent command
    @CommandLine.ParentCommand
    private MainH2 mainH2;
    //---
    @ArgGroup(exclusive = true, multiplicity = "1")
    Exclusive exclusive;

    static class Exclusive {

        @CommandLine.Option(names = {"--schemas"},
                required = true,
                description = "Show schemas")
        private boolean schemas;
        @CommandLine.Option(names = {"--tables"},
                required = true,
                description = "Show tables")
        private boolean tables;
        @CommandLine.Option(names = {"--columns"},
                required = true,
                description = "Show columns")
        private boolean columns;
    }
    //---
    @CommandLine.Option(names = {"--from-schema"},
            paramLabel = "SCHEMA",
            required = false,
            description = "Show from a schema")
    private String fromASchema;
    @CommandLine.Option(names = {"--from-table"},
            paramLabel = "TABLE",
            required = false,
            description = "Show from a table")
    private String fromATable;
    @CommandLine.Option(names = {"--output-format"},
            paramLabel = "OUTPUTFORMAT",
            defaultValue = "CSV",
            required = false,
            description = "Output format used."
            + "Valid values: ${COMPLETION-CANDIDATES}")
    private OutputMode outputFormat;

    @Override
    public Integer call() throws Exception {
        List<String> argsAsList = convertOptionsToArgs();
        logger.info("Args {}", argsAsList);
        //---
        process(argsAsList);
        return 0;
    }

    private List<String> convertOptionsToArgs() {
        final List<String> argsAsList = new ArrayList<>();
        if (this.exclusive.schemas) {
            argsAsList.add("SCHEMAS");
        } else if (this.exclusive.tables) {
            argsAsList.add("TABLES");
        } else if (this.exclusive.columns) {
            argsAsList.add("COLUMNS");
        } else {
            argsAsList.add("SCHEMAS");
        }

        if (this.exclusive.tables) {
            if (this.fromASchema != null) {
                argsAsList.add("FROM");
                argsAsList.add(this.fromASchema);
            }
        } else if (this.exclusive.columns) {
            if (this.fromATable != null) {
                argsAsList.add("FROM");
                argsAsList.add(this.fromATable);
            }
            if (this.fromASchema != null) {
                argsAsList.add("FROM");
                argsAsList.add(this.fromASchema);
            }
        }
        return argsAsList;
    }

    private void process(List<String> args) throws Exception {
        try (final Connection connection = this.mainH2.createConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            //---
            final Savepoint savepoint = connection.setSavepoint();
            try (final Statement stat = connection.createStatement()) {
                final String sql = buildSql(args);
                logger.info("Execute sql {}", sql);
                try (final ResultSet rs = stat.executeQuery(sql)) {
                    final OutputBy outputBy = OutputMode.createOutputBy(this.outputFormat);
                    outputBy.output(rs, System.out);
                }
            }
            connection.rollback(savepoint);
        }
    }

    private String buildSql(List<String> args) {
        final String argsAsString = args.stream().collect(Collectors.joining(" "));
        final String sql = String.format("SHOW %s", argsAsString);
        return sql;
    }

}
