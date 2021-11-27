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

import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine;

/**
 *
 * @author pi
 */
public class CsvReadWriteOptions {
    //---

    @CommandLine.Option(names = {"--case-sensitive-columnnames"},
            negatable = true,
            required = false,
            description = "use case sensitive column names")
    private boolean caseSensitiveColumnNames = false; // (true or false; disabled by default),
    @CommandLine.Option(names = {"--charset"},
            defaultValue = "UTF-8",
            paramLabel = "CHARSET",
            required = false,
            description = "csv charset, eg UTF-8, ISO-8859-1")
    private String charset; // (for example 'UTF-8'),
    @CommandLine.Option(names = {"--escape"},
            defaultValue = "\"",
            paramLabel = "ESCAPE",
            required = false,
            description = "the character that escapes the field delimiter")
    private String escape; // (the character that escapes the field delimiter),
    @CommandLine.Option(names = {"--field-delimiter"},
            defaultValue = "\"",
            paramLabel = "FIELDDELIMITER",
            required = false,
            description = "")
    private String fieldDelimiter; // (a double quote by default),
    @CommandLine.Option(names = {"--field-separator"},
            defaultValue = ",",
            paramLabel = "FIELDSEPARATOR",
            required = false,
            description = "")
    private String fieldSeparator; // (a comma by default),
    @CommandLine.Option(names = {"--line-comment"},
            defaultValue = "",
            required = false,
            description = "")
    private String lineComment; // (disabled by default),
    @CommandLine.Option(names = {"--line-separator"},
            paramLabel = "LINESEPARATOR",
            required = false,
            description = "the line separator used for writing; ignored for reading")
    private String lineSeparator = System.lineSeparator(); // (the line separator used for writing; ignored for reading),
    @CommandLine.Option(names = {"--null"},
            defaultValue = "",
            paramLabel = "NULL",
            required = false,
            description = "Support reading existing CSV files that contain explicit null delimiters. Note that an empty, unquoted values are also treated as null.")
    private String null_; //, Support reading existing CSV files that contain explicit null delimiters. Note that an empty, unquoted values are also treated as null.
    @CommandLine.Option(names = {"--preserve-whitespace"},
            negatable = true,
            required = false,
            description = "Preserve whitespace in values")
    private boolean preserveWhitespace = false; // (true or false; disabled by default),
    @CommandLine.Option(names = {"--no-write-column-header"},
            negatable = true,
            required = false,
            description = "write csv column header")
    private boolean writeColumnHeader = true; // (true or false; enabled by default).

    Map<String, String> createOptionsMap() {
        final Map<String, String> m = new HashMap<>();
        m.put("caseSensitiveColumnNames", String.valueOf(this.caseSensitiveColumnNames));
        if (this.charset != null) {
            m.put("charset", this.charset);
        }
        if (this.escape != null) {
            m.put("escape", String.valueOf(this.escape));
        }
        if (this.fieldDelimiter != null) {
            m.put("fieldDelimiter", this.fieldDelimiter);
        }
        if (this.fieldSeparator != null) {
            m.put("fieldSeparator", this.fieldSeparator);
        }
        if (this.lineComment != null) {
            m.put("lineComment", String.valueOf(this.lineComment));
        }
        if (this.lineSeparator != null) {
            m.put("lineSeparator", String.valueOf(this.lineSeparator));
        }
        if (this.null_ != null) {
            m.put("null", String.valueOf(this.null_));
        }
        m.put("preserveWhitespace", String.valueOf(this.preserveWhitespace));
        m.put("writeColumnHeader", String.valueOf(this.writeColumnHeader));
        return m;
    }

    String createOptionsString() {
        final Map<String, String> m = createOptionsMap();
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> me : m.entrySet()) {
            sb.append(me.getKey()).append("=").append(me.getValue());
            sb.append(" ");
        }
        return sb.toString();
    }
}
