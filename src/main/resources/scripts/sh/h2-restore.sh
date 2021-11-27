#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME Restore "$@"

# Restores a H2 database by extracting the database files from a .zip file.
# Usage: java org.h2.tools.Restore <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]     Print the list of options
# [-file <filename>]  The source file name (default: backup.zip)
# [-dir <dir>]        The target directory (default: .)
# [-db <database>]    The target database name (as stored if not set)
# [-quiet]            Do not print progress information
# See also http://h2database.com/javadoc/org/h2/tools/Restore.html

