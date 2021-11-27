#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME RunScript "$@"

# Runs a SQL script against a database.
# Usage: java org.h2.tools.RunScript <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]     Print the list of options
# [-url "<url>"]      The database URL (jdbc:...)
# [-user <user>]      The user name (default: sa)
# [-password <pwd>]   The password
# [-script <file>]    The script file to run (default: backup.sql)
# [-driver <class>]   The JDBC driver class to use (not required in most cases)
# [-showResults]      Show the statements and the results of queries
# [-checkResults]     Check if the query results match the expected results
# [-continueOnError]  Continue even if the script contains errors
# [-options ...]      RUNSCRIPT options (embedded H2; -*Results not supported)
# See also https://h2database.com/javadoc/org/h2/tools/RunScript.html

