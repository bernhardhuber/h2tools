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
package org.huberb.h2tools.support;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.h2.tools.Csv;

/**
 *
 * @author pi
 */
public class CsvReadWriteWrappers {

    static class CsvWriter implements AutoCloseable {

        final Writer writer;
        final Map<String, String> options;
        final Csv csv;

        CsvWriter(Writer writer, Map<String, String> options) {
            this.writer = writer;
            this.options = options;
            this.csv = new Csv();
            final String csvOptions = "";
            this.csv.setOptions(csvOptions);
        }

        int writeTo(Connection connection, String querySql) throws CsvReaderWriteWrappersRuntimeException {

            try (final PreparedStatement preparedStatement = connection.prepareStatement(querySql)) {
                return writeTo(preparedStatement);
            } catch (SQLException sqlException) {
                throw new CsvReaderWriteWrappersRuntimeException("writeTo", sqlException);
            }
        }

        int writeTo(PreparedStatement preparedStatement) throws CsvReaderWriteWrappersRuntimeException {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return writeTo(resultSet);
            } catch (SQLException sqlException) {
                throw new CsvReaderWriteWrappersRuntimeException("writeTo", sqlException);
            }
        }

        int writeTo(ResultSet resultSet) throws CsvReaderWriteWrappersRuntimeException {
            try (resultSet) {
                final int rowsWritten = this.csv.write(writer, resultSet);
                return rowsWritten;
            } catch (SQLException sqlException) {
                throw new CsvReaderWriteWrappersRuntimeException("writeTo", sqlException);
            }
        }

        @Override
        public void close() throws IOException {
            if (this.writer != null) {
                this.writer.close();
            }
        }

        static class CsvWriterFactory {

            CsvWriter create(String outFilename, Map<String, String> options) throws CsvReaderWriteWrappersRuntimeException {
                try {
                    final String charsetAsString = options.getOrDefault("charset", null);
                    final Charset charset = charsetAsString != null ? Charset.forName(charsetAsString) : Charset.defaultCharset();
                    final Writer w = new FileWriter(outFilename, charset);
                    final CsvWriter csvWriter = new CsvWriter(w, options);
                    return csvWriter;
                } catch (IOException ioException) {
                    throw new CsvReaderWriteWrappersRuntimeException("create", ioException);
                }
            }
        }
    }

    static class CsvReader implements AutoCloseable {

        final Reader reader;
        final Map<String, String> options;
        final String[] colNames;

        CsvReader(Reader reader, Map<String, String> options, String[] colNames) {
            this.reader = reader;
            this.options = options;
            this.colNames = colNames;
        }

        void readFrom(Consumer<ResultSet> c) throws CsvReaderWriteWrappersRuntimeException {
            try {
                final Csv csv = new Csv();
                final String csvOptions = "";
                csv.setOptions(csvOptions);
                //---
                final ResultSet rs = csv.read(reader, colNames);
                c.accept(rs);
            } catch (IOException ex) {
                throw new CsvReaderWriteWrappersRuntimeException("readFrom", ex);
            }
        }

        @Override
        public void close() throws IOException {
            if (this.reader != null) {
                this.reader.close();
            }
        }

        static class DefaultCsvReaderConsumer implements Consumer<ResultSet> {

            final BiConsumer<String, String> bic;

            DefaultCsvReaderConsumer(BiConsumer<String, String> bic) {
                this.bic = bic;
            }

            @Override
            public void accept(ResultSet rs) {
                try (rs) {
                    final ResultSetMetaData meta = rs.getMetaData();

                    while (rs.next()) {
                        for (int i = 0; i < meta.getColumnCount(); i++) {
                            final String columnLabel = meta.getColumnLabel(i + 1);
                            final String columnValue = rs.getString(i + 1);
                            bic.accept(columnLabel, columnValue);
                        }
                    }
                } catch (SQLException sqlex) {
                    throw new CsvReaderWriteWrappersRuntimeException("Accepting resultSet", sqlex);
                }
            }

        }

        static class CsvReaderFactory {

            CsvReader create(String inFilename, Map<String, String> options, String[] colNames) throws IOException {
                final String charsetAsString = options.getOrDefault("charset", null);
                final Charset charset = charsetAsString != null ? Charset.forName(charsetAsString) : Charset.defaultCharset();
                final Reader r = new FileReader(inFilename, charset);
                final CsvReader csvReader = new CsvReader(r, options, colNames);
                return csvReader;
            }
        }

    }

    static class CsvReaderWriteWrappersRuntimeException extends RuntimeException {

        CsvReaderWriteWrappersRuntimeException(String message, SQLException sqlException) {
            super(message, sqlException);
        }

        CsvReaderWriteWrappersRuntimeException(String message, IOException ioException) {
            super(message, ioException);
        }

    }
}
