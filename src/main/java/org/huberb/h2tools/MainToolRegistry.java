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
import java.util.Optional;
import org.h2.util.Tool;

/**
 * A registry of command to main tool-classes. This registry is used to map
 * command-line argument to an tool-class for invoking its main-method.
 *
 * @author berni3
 */
class MainToolRegistry {

    static class ToolEntry {

        String name;
        Class<? extends Tool> clazz;
        String description;

        public ToolEntry(String name, Class<? extends Tool> clazz, String description) {
            this.name = name;
            this.clazz = clazz;
            this.description = description;
        }

        static List<ToolEntry> l() {
            final List<ToolEntry> l = Arrays.asList(
                    new ToolEntry(
                            org.h2.tools.Backup.class.getSimpleName(),
                            org.h2.tools.Backup.class,
                            "Creates a backup of a database. This tool copies all database files. "
                            + "The database must be closed before using this tool. "
                            + "To create a backup while the database is in use, run the BACKUP SQL statement. "
                            + "In an emergency, for example if the application is not responding, creating a backup using the Backup tool is possible by using the quiet mode. "
                            + "However, if the database is changed while the backup is running in quiet mode, the backup could be corrupt."),
                    new ToolEntry(
                            org.h2.tools.ChangeFileEncryption.class.getSimpleName(),
                            org.h2.tools.ChangeFileEncryption.class,
                            "Allows changing the database file encryption password or algorithm. "
                            + "This tool can not be used to change a password of a user. "
                            + "The database must be closed before using this tool."),
                    new ToolEntry(
                            org.h2.tools.Console.class.getSimpleName(),
                            org.h2.tools.Console.class,
                            "Starts the H2 Console (web-) server, as well as the TCP and PG server."),
                    new ToolEntry(
                            org.h2.tools.ConvertTraceFile.class.getSimpleName(),
                            org.h2.tools.ConvertTraceFile.class,
                            "Converts a .trace.db file to a SQL script and Java source code. SQL statement statistics are listed as well."),
                    new ToolEntry(
                            org.h2.tools.CreateCluster.class.getSimpleName(),
                            org.h2.tools.CreateCluster.class,
                            "Creates a cluster from a stand-alone database. Copies a database to another location if required."),
                    new ToolEntry(
                            org.h2.tools.DeleteDbFiles.class.getSimpleName(),
                            org.h2.tools.DeleteDbFiles.class,
                            "Deletes all files belonging to a database. The database must be closed before calling this tool."),
                    new ToolEntry(
                            org.h2.tools.Recover.class.getSimpleName(),
                            org.h2.tools.Recover.class,
                            "Helps recovering a corrupted database."),
                    new ToolEntry(
                            org.h2.tools.Restore.class.getSimpleName(),
                            org.h2.tools.Restore.class,
                            "Restores a H2 database by extracting the database files from a .zip file."),
                    new ToolEntry(
                            org.h2.tools.RunScript.class.getSimpleName(),
                            org.h2.tools.RunScript.class,
                            "Runs a SQL script against a database."),
                    new ToolEntry(
                            org.h2.tools.Script.class.getSimpleName(),
                            org.h2.tools.Script.class,
                            "Creates a SQL script file by extracting the schema and data of a database."),
                    new ToolEntry(
                            org.h2.tools.Server.class.getSimpleName(),
                            org.h2.tools.Server.class,
                            "Starts the H2 Console (web-) server, TCP, and PG server."),
                    new ToolEntry(
                            org.h2.tools.Shell.class.getSimpleName(),
                            org.h2.tools.Shell.class,
                            "Interactive command line tool to access a database using JDBC.")
            );
            return l;

        }
    }
    final List<ToolEntry> l;

    MainToolRegistry() {

        l = ToolEntry.l();
    }

    Optional<Class<? extends Tool>> findGetOrDefault(String commandLowerCase) {
        Class<? extends Tool> value = null;
        final String key = commandLowerCase.toLowerCase();
        for (ToolEntry te : this.l) {
            if (key.equalsIgnoreCase(te.name)) {
                value = te.clazz;
                break;
            }
        }
        return Optional.ofNullable(value);
    }

    Iterable<ToolEntry> retrieveIterableOfToolClassesWithMain() {
        return Collections.unmodifiableList(this.l);
    }

}
