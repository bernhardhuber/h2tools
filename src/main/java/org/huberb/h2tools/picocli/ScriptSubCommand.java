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
 *
 * @author pi
 */
@CommandLine.Command(name = "script",
        mixinStandardHelpOptions = true,
        showDefaultValues = true,
        description = "Creates a SQL script file by extracting the schema and data of a database.")
public class ScriptSubCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ScriptSubCommand.class);

    @CommandLine.ParentCommand
    private MainH2 mainH2; // picocli injects reference to parent command
    //---
    @CommandLine.Option(names = {"--nodata"},
            required = false,
            description = "NODATA will not emit INSERT statements. ")
    private boolean nodata;
    //---
    @CommandLine.Option(names = {"--simple"},
            required = false,
            description = "SIMPLE does not use multi-row insert statements. ")
    private boolean simple;
    @CommandLine.Option(names = {"--columns"},
            paramLabel = "COLUMNS",
            required = false,
            description = "COLUMNS includes column name lists into insert statements.")
    private String columns;
    //---
    @CommandLine.Option(names = {"--nopasswords"},
            required = false,
            description = "NOPASSWORDS")
    private boolean nopasswords;
    //---
    @CommandLine.Option(names
            = {"--nosettings"},
            required = false,
            description = "NOSETTINGS turns off dumping the database settings (the SET XXX commands)")
    private boolean nosettings;
    //---
    @CommandLine.Option(names = {"--drop"},
            required = false,
            description = "If the DROP option is specified, drop statements are created for tables, views, and sequences.")
    private boolean drop;
    @CommandLine.Option(names = {"--blocksize"},
            defaultValue = "8192",
            required = false,
            description = "If the block size is set, CLOB and BLOB values larger than this size are split into separate blocks. "
            + "BLOCKSIZE is used when writing out LOB data, and specifies the point at the values transition from being inserted as inline values, "
            + "to be inserted using out-of-line commands. ")
    private Integer blocksize;
    //--- to file
    @CommandLine.Option(names = {"--to"},
            defaultValue = "backup.sql",
            paramLabel = "TO",
            required = false,
            description = "The target script file name (default: backup.sql)")
    private File toFile;
    @CommandLine.Option(names = {"--compression"},
            paramLabel = "COMPRESSION",
            required = false,
            description = "The compression (DEFALTE, LZF, ZIP, GZIP) and encryption algorithm to use for script files. "
            + "When using encryption, only DEFLATE and LZF are supported. LZF is faster but uses more space.")
    private String compression;
    @CommandLine.Option(names = {"--cipher-algorithm"},
            paramLabel = "CIPHERALGORITHM",
            required = false,
            description = "Only the algorithm AES (AES-128) is supported currently.")
    private String cipherAlgorithm;
    @CommandLine.Option(names = {"--cipher-password"},
            paramLabel = "CIPHERPASSWORD",
            required = false,
            description = "Ciphyer password")
    private String cipherPassword;
    // TODO add compression options
    @CommandLine.Option(names = {"--charset"},
            paramLabel = "CHARSET",
            required = false,
            description = "script charset, eg UTF-8, ISO-8859-1")
    private String charset;
    @CommandLine.Option(names = {"--table"},
            paramLabel = "TABLE",
            required = false,
            description = "When using the TABLE, only the selected table(s) are included.")
    private String table;
    @CommandLine.Option(names = {"--schema"},
            paramLabel = "SCHEMA",
            required = false,
            description = "When using the SCHEMA option, only the selected schema(s) are included.")
    private String schema;

    @Override
    public Integer call() throws Exception {
        List<String> argsAsList = convertOptionsToArgs();
        logger.info("Args {}", argsAsList);
        //---
        process(argsAsList);
        return 0;
    }

    private List<String> convertOptionsToArgs() {
        List<String> argsAsList = new ArrayList<>();
        //---
        if (nodata) {
            argsAsList.add("NODATA");
        }
        if (simple) {
            argsAsList.add("SIMPLE");
        }
        if (columns != null) {
            argsAsList.add(columns);
        }
        if (nopasswords) {
            argsAsList.add("NOPASSWORDS");
        }
        if (nosettings) {
            argsAsList.add("NOSETTINGS");
        }
        if (drop) {
            argsAsList.add("DROP");
        }
        if (blocksize != null) {
            argsAsList.add("BLOCKSIZE");
            argsAsList.add(String.valueOf(blocksize));
        }
        //---
        if (toFile != null) {
            argsAsList.add("TO");
            argsAsList.add("'" + toFile.getAbsolutePath() + "'");
            if (charset != null) {
                argsAsList.add("CHARSET");
                argsAsList.add(charset);
            }
            if (compression != null) {
                argsAsList.add("COMPRESSION");
                argsAsList.add(compression);
            }
            if (cipherAlgorithm != null && cipherPassword != null) {
                argsAsList.add("CIPHER");
                argsAsList.add(cipherAlgorithm);
                argsAsList.add("PASSWORD");
                argsAsList.add(cipherPassword);
            }
        }
        if (table != null) {
            argsAsList.add("TABLE");
            argsAsList.add(table);
        }
        if (schema != null) {
            argsAsList.add("SCHEMA");
            argsAsList.add(schema);
        }
        return argsAsList;
    }

    private void process(List<String> args) throws SQLException, Exception {
        try (final Connection connection = this.mainH2.createConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (final Statement statement = connection.createStatement()) {
                final String sql = buildSql(args);
                logger.info("Execute sql {}", sql);
                final boolean executedRc = statement.execute(sql);
                handleExcuteStatementOutput(executedRc, statement);
            }
        }
    }

    private String buildSql(List<String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append("SCRIPT ");
        for (int i = 0; i < args.size(); i++) {
            if (sb.charAt(sb.length() - 1) != ' ') {
                sb.append(" ");
            }
            sb.append(args.get(i));
        }
        return sb.toString();
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
