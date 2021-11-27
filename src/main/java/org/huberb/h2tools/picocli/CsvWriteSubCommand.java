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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.huberb.h2tools.support.OutputResultSet.OutputBy;
import org.huberb.h2tools.support.OutputResultSet.OutputMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * Writes a CSV (comma separated values).
 *
 * @author pi
 */
// The built-in function CSVWRITE can be used to create a CSV file from a query.
//
//Example:
//
//CREATE TABLE TEST(ID INT, NAME VARCHAR);
//INSERT INTO TEST VALUES(1, 'Hello'), (2, 'World');
//CALL CSVWRITE('test.csv', 'SELECT * FROM TEST');
@CommandLine.Command(name = "csvWrite",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Writes a CSV (comma separated values).")
public class CsvWriteSubCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(CsvWriteSubCommand.class);

    @CommandLine.ParentCommand
    private MainH2 mainH2; // picocli injects reference to parent command
    //--- to file
    @CommandLine.Option(names = {"--to"},
            defaultValue = "csvwrite.csv",
            paramLabel = "TO",
            required = true,
            description = "The target csv file name")
    private File toFile;
    @CommandLine.Option(names = {"--query"},
            paramLabel = "QUERY",
            required = true,
            description = "The query string to extract data")
    private String query;
    
    //---
    @CommandLine.Mixin
    private CsvReadWriteOptions csvReadWriteOptions;

    @Override
    public Integer call() throws Exception {
        final List<String> argsAsList = convertOptionsToArgs();
        logger.info("Args {}", argsAsList);
        //---
        process(argsAsList);
        return 0;
    }

    private List<String> convertOptionsToArgs() {
        final List<String> argsAsList = new ArrayList<>();
        if (this.toFile != null) {
            argsAsList.add(String.format("%s", this.toFile.getAbsolutePath()));
        }
        if (this.query != null) {
            argsAsList.add(String.format("%s", this.query));
        }
        final String options = this.csvReadWriteOptions.createOptionsString();
        argsAsList.add(options);
        return argsAsList;
    }

    private void process(List<String> args) throws SQLException, Exception {
        try (final Connection connection = this.mainH2.createConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            //---
            final Savepoint savepoint = connection.setSavepoint();
            try (final Statement statement = connection.createStatement()) {
                final String sql = buildSql(args);
                logger.info("Execute sql {}", sql);
                final boolean executedRc = statement.execute(sql);
                handleExcuteStatementOutput(executedRc, statement);
            }
            connection.rollback(savepoint);
        }
    }

    String buildSql(List<String> args) {
        final String theToFile = args.get(0);
        final String theQuery = args.get(1);
        final String theCsvOptions = args.get(2);
        final String sql = String.format("CALL CSVWRITE( '%s', '%s', '%s' )", theToFile, theQuery, theCsvOptions);
        return sql;
    }

    void handleExcuteStatementOutput(boolean executedRc, Statement statement) throws SQLException, Exception {
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
