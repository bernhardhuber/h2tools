#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME Shell "$@"

# Interactive command line tool to access a database using JDBC.
# Usage: java org.h2.tools.Shell <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]        Print the list of options
# [-url "<url>"]         The database URL (jdbc:h2:...)
# [-user <user>]         The user name
# [-password <pwd>]      The password
# [-driver <class>]      The JDBC driver class to use (not required in most cases)
# [-sql "<statements>"]  Execute the SQL statements and exit
# [-properties "<dir>"]  Load the server properties from this directory
# If special characters don't work as expected, you may need to use
#  -Dfile.encoding=UTF-8 (Mac OS X) or CP850 (Windows).
# See also http://h2database.com/javadoc/org/h2/tools/Shell.html

