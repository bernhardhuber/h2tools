#!/bin/sh
SCRIPT_DIR=`dirname $0`

JAR_NAME="target\${project.build.finalName}-mainTools.jar"

java -jar $JAR_NAME Backup "$@"

# Creates a backup of a database.
# This tool copies all database files. The database must be closed before using
#  this tool. To create a backup while the database is in use, run the BACKUP
#  SQL statement. In an emergency, for example if the application is not
#  responding, creating a backup using the Backup tool is possible by using the
#  quiet mode. However, if the database is changed while the backup is running
#  in quiet mode, the backup could be corrupt.
# Usage: java org.h2.tools.Backup <options>
# Options are case sensitive. Supported options are:
# [-help] or [-?]     Print the list of options
# [-file <filename>]  The target file name (default: backup.zip)
# [-dir <dir>]        The source directory (default: .)
# [-db <database>]    Source database; not required if there is only one
# [-quiet]            Do not print progress information
# See also http://h2database.com/javadoc/org/h2/tools/Backup.html

