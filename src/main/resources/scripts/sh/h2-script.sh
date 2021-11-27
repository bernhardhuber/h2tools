#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME Script "$@"

# Creates a SQL script file by extracting the schema and data of a database.
# Usage: java org.h2.tools.Script <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]    Print the list of options
# [-url "<url>"]     The database URL (jdbc:...)
# [-user <user>]     The user name (default: sa)
# [-password <pwd>]  The password
# [-script <file>]   The target script file name (default: backup.sql)
# [-options ...]     A list of options (only for embedded H2, see SCRIPT)
# [-quiet]           Do not print progress information
# See also http://h2database.com/javadoc/org/h2/tools/Script.html

