#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME Recover "$@"

# Helps recovering a corrupted database.
# Usage: java org.h2.tools.Recover <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]    Print the list of options
# [-dir <dir>]       The directory (default: .)
# [-db <database>]   The database name (all databases if not set)
# [-trace]           Print additional trace information
# [-transactionLog]  Print the transaction log
# Encrypted databases need to be decrypted first.
# See also http://h2database.com/javadoc/org/h2/tools/Recover.html

