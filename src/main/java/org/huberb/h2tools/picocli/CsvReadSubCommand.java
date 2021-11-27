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

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.huberb.h2tools.support.OutputResultSet.OutputBy;
import org.huberb.h2tools.support.OutputResultSet.OutputMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Reading a CSV File from within a database.
 *
 * @author pi
 */
//Reading a CSV File from Within a Database
//
//A CSV file can be read using the function CSVREAD. Example:
//
//SELECT * FROM CSVREAD('test.csv');
//
//Please note for performance reason, CSVREAD should not be used inside a join. 
//Instead, import the data first (possibly into a temporary table), 
//create the required indexes if necessary, and then query this table.
//Importing Data from a CSV File
//
//A fast way to load or import data (sometimes called 'bulk load') 
//from a CSV file is to combine table creation with import. 
//Optionally, the column names and data types can be set when creating the table. 
//Another option is to use INSERT INTO ... SELECT.
//
//CREATE TABLE TEST AS SELECT * FROM CSVREAD('test.csv');
//CREATE TABLE TEST(ID INT PRIMARY KEY, NAME VARCHAR(255))
//    AS SELECT * FROM CSVREAD('test.csv');
//
@CommandLine.Command(name = "csvRead",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Reading a CSV File from within a database.")
public class CsvReadSubCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(CsvReadSubCommand.class);

    @CommandLine.ParentCommand
    private MainH2 mainH2; // picocli injects reference to parent command
    //--- to file
    @CommandLine.Option(names = {"--from"},
            defaultValue = "csvread.csv",
            paramLabel = "TO",
            required = true,
            description = "The source csv file name")
    private String from;
    @CommandLine.Option(names = {"--csv-columns"},
            paramLabel = "COLUMNS",
            required = false,
            description = "CSV columns")
    private String csvColumns;

    //---
    @CommandLine.Mixin
    private CsvReadWriteOptions csvReadWriteOptions;

    //---
    @CommandLine.Option(names = {"--create-table"},
            paramLabel = "CREATE-TABLE",
            required = false,
            description = "")
    private String createTable;
    @CommandLine.Option(names = {"--insert-table"},
            paramLabel = "INSERT-TABLE",
            required = false,
            description = "")
    private String insertTable;
    //---
    @CommandLine.Option(names = {"--output-format"},
            paramLabel = "OUTPUTFORMAT",
            defaultValue = "CSV",
            required = false,
            description = "Output format used")
    private String outputFormat;

    @Override
    public Integer call() throws Exception {
        Map<String, String> argsAsList = convertOptionsToArgs();
        logger.info("Args {}", argsAsList);
        //---
        process(argsAsList);
        return 0;
    }

    private Map<String, String> convertOptionsToArgs() {
        final Map<String, String> m = new HashMap<>();
        final String options = this.csvReadWriteOptions.createOptionsString();

        final String theFrom = normalizeFileOrUriName(this.from);
        final String theCsvColumns = this.csvColumns != null ? "'" + this.csvColumns + "'" : null;
        final String theOptions = options != null ? "'" + options + "'" : "null";
        if (this.createTable != null) {
            String sql = String.format("CREATE TABLE %s AS SELECT * FROM CSVREAD( '%s', %s, %s)",
                    this.createTable,
                    theFrom,
                    theCsvColumns,
                    theOptions
            );
            m.put("createTable", sql);
        } else if (this.insertTable != null) {
            String sql = String.format("INSERT INTO %s SELECT * FROM CSVREAD( '%s', %s, %s)",
                    this.insertTable,
                    theFrom,
                    theCsvColumns,
                    theOptions
            );
            m.put("insertTable", sql);
        } else {
            final String sql = String.format("SELECT * FROM CSVREAD( '%s', %s, %s)",
                    theFrom,
                    theCsvColumns,
                    theOptions
            );
            m.put("selectOnly", sql);
        }

        return m;
    }

    /**
     * Make name an absolute file-name.
     *
     * @param n specifies a file-name or an uri.
     *
     * @return absolute path of a file-name, except if name specifies an uri
     */
    String normalizeFileOrUriName(String n) {
        String result = null;
        try {
            URI uri = URI.create(n);
            if (uri.getScheme() != null) {
                result = uri.toString();
            }
        } catch (IllegalArgumentException illargex) {
            result = null;
        }
        if (result == null) {
            File f = new File(n);
            result = f.getAbsolutePath();
        }

        return result;
    }

    private void process(Map<String, String> args) throws SQLException, Exception {
        try (final Connection connection = this.mainH2.createConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            //---
            final Savepoint savepoint = connection.setSavepoint();
            try (final Statement statement = connection.createStatement()) {
                final String sql = buildSql(args);
                logger.info("Execute sql {}", sql);
                if (sql.startsWith("INSERT") || sql.startsWith("CREATE")) {
                    final boolean executedRc = statement.execute(sql);
                    handleExecuteStatementOutput(executedRc, statement);
                } else {
                    try (final ResultSet rs = statement.executeQuery(sql)) {
                        final Optional<OutputMode> outputModeOptional = OutputMode.findOutputMode(this.outputFormat);
                        if (outputModeOptional.isPresent()) {
                            final OutputBy outputBy = OutputMode.createOutputBy(outputModeOptional.get());
                            outputBy.output(rs, System.out);
                        }
                    }
                }
            }
            connection.commit();
        }
    }

    String buildSql(Map<String, String> args) {
        final String sql;
        if (args.containsKey("createTable")) {
            sql = args.get("createTable");
        } else if (args.containsKey("insertTable")) {
            sql = args.get("insertTable");
        } else if (args.containsKey("selectOnly")) {
            sql = args.get("selectOnly");
        } else {
            sql = null;
        }

        return sql;
    }

    void handleExecuteStatementOutput(boolean executedRc, Statement statement) throws SQLException, Exception {
        logger.info("Executed rc {}", executedRc);
        if (executedRc) {
            logger.info("Executed rc {}", executedRc);
            try (ResultSet rs = statement.getResultSet()) {
                final OutputMode outputMode = OutputMode.RAW;
                final OutputBy outputBy = OutputMode.createOutputBy(outputMode);
                outputBy.output(rs, System.out);
            }
        } else {
            final int updateCount = statement.getUpdateCount();
            System.out.printf("updateCount %d%n", updateCount);
        }
    }

}
