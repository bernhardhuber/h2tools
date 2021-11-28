# MainTools

## SYNOPSIS
```
MainTools [-hV] [@<filename>...] [<toolName>]
```

## DESCRIPTION

```
Usage: MainTools [-hV] [@<filename>...] [<toolName>]
Run H2 Tools
Database URLs (https://h2database.com/html/features.html#database_url)

Embedded (https://h2database.com/html/features.html#connection_modes)
jdbc:h2:~/test 'test' in the user home directory
jdbc:h2:/data/test 'test' in the directory /data
jdbc:h2:./test in the current(!) working directory

In-Memory (https://h2database.com/html/features.html#in_memory_databases)
jdbc:h2:mem:test multiple connections in one process
jdbc:h2:mem: unnamed private; one connection

Server Mode (https://h2database.com/html/tutorial.html#using_server)
jdbc:h2:tcp://localhost/~/test user home dir
jdbc:h2:tcp://localhost//data/test absolute dir
Server start:java -cp *.jar org.h2.tools.Server

Settings (https://h2database.com/html/features.html#database_url)
jdbc:h2:..;MODE=MySQL compatibility (or HSQLDB,...)
jdbc:h2:..;TRACE_LEVEL_FILE=3 log to *.trace.db

      [@<filename>...]   One or more argument files containing options.
      [<toolName>]       Launch H2 tool, like Shell, Script, RunScript, etc.
  -h, --help             Show this help message and exit.
  -V, --version          Print version information and exit.
```

## Available Commands

Following toolName are available.

### Backup

* toolname *Backup*
* class org.h2.tools.Backup

Creates a backup of a database. This tool copies all database files. 
The database must be closed before using this tool. 
To create a backup while the database is in use, run the BACKUP SQL statement.
In an emergency, for example if the application is not responding, 
creating a backup using the Backup tool is possible by using the quiet mode. 
However, if the database is changed while the backup is running in quiet mode, 
the backup could be corrupt.

```
Creates a backup of a database.
This tool copies all database files. The database must be closed before using
 this tool. To create a backup while the database is in use, run the BACKUP
 SQL statement. In an emergency, for example if the application is not
 responding, creating a backup using the Backup tool is possible by using the
 quiet mode. However, if the database is changed while the backup is running
 in quiet mode, the backup could be corrupt.
Usage: java org.h2.tools.Backup <options>
Options are case sensitive. Supported options are:
[-help] or [-?]     Print the list of options
[-file <filename>]  The target file name (default: backup.zip)
[-dir <dir>]        The source directory (default: .)
[-db <database>]    Source database; not required if there is only one
[-quiet]            Do not print progress information
See also https://h2database.com/javadoc/org/h2/tools/Backup.html
```

### ChangeFileEncryption

* toolname *ChangeFileEncryption*
* class org.h2.tools.ChangeFileEncryption

Allows changing the database file encryption password or algorithm. 
This tool can not be used to change a password of a user. 
The database must be closed before using this tool.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar ChangeFileEncryption -?
Allows changing the database file encryption password or algorithm.
This tool can not be used to change a password of a user.
 The database must be closed before using this tool.
Usage: java org.h2.tools.ChangeFileEncryption <options>
Options are case sensitive. Supported options are:
[-help] or [-?]   Print the list of options
[-cipher type]    The encryption type (AES)
[-dir <dir>]      The database directory (default: .)
[-db <database>]  Database name (all databases if not set)
[-decrypt <pwd>]  The decryption password (if not set: not yet encrypted)
[-encrypt <pwd>]  The encryption password (if not set: do not encrypt)
[-quiet]          Do not print progress information
See also https://h2database.com/javadoc/org/h2/tools/ChangeFileEncryption.html
```

### Console

* toolname *Console*
* class org.h2.tools.Console

Starts the H2 Console (web-) server, as well as the TCP and PG server.

```
```

### ConvertTraceFile

* toolname *ConvertTraceFile*
* class org.h2.tools.ConvertTraceFile

Converts a .trace.db file to a SQL script and Java source code. 
SQL statement statistics are listed as well.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar ConvertTraceFile -?
Converts a .trace.db file to a SQL script and Java source code.
SQL statement statistics are listed as well.
Usage: java org.h2.tools.ConvertTraceFile <options>
Options are case sensitive. Supported options are:
[-help] or [-?]      Print the list of options
[-traceFile <file>]  The trace file name (default: test.trace.db)
[-script <file>]     The script file name (default: test.sql)
[-javaClass <file>]  The Java directory and class file name (default: Test)
See also https://h2database.com/javadoc/org/h2/tools/ConvertTraceFile.html
```

### CreateCluster

* toolname *CreateCluster*
* class org.h2.tools.CreateCluster

Creates a cluster from a stand-alone database. 
Copies a database to another location if required.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar CreateCluster -?
Creates a cluster from a stand-alone database.
Copies a database to another location if required.
Usage: java org.h2.tools.CreateCluster <options>
Options are case sensitive. Supported options are:
[-help] or [-?]       Print the list of options
[-urlSource "<url>"]  The database URL of the source database (jdbc:h2:...)
[-urlTarget "<url>"]  The database URL of the target database (jdbc:h2:...)
[-user <user>]        The user name (default: sa)
[-password <pwd>]     The password
[-serverList <list>]  The comma separated list of host names or IP addresses
See also https://h2database.com/javadoc/org/h2/tools/CreateCluster.html
```

### DeleteDbFiles

* toolname *DeleteDbFiles*
* class org.h2.tools.DeleteDbFiles

Deletes all files belonging to a database. 
The database must be closed before calling this tool.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar DeleteDbFiles -?
Deletes all files belonging to a database.
The database must be closed before calling this tool.
Usage: java org.h2.tools.DeleteDbFiles <options>
Options are case sensitive. Supported options are:
[-help] or [-?]   Print the list of options
[-dir <dir>]      The directory (default: .)
[-db <database>]  The database name
[-quiet]          Do not print progress information
See also https://h2database.com/javadoc/org/h2/tools/DeleteDbFiles.html
```

### Recover

* toolname *Recover*
* class org.h2.tools.Recover

Helps recovering a corrupted database.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar Recover -?
Helps recovering a corrupted database.
Usage: java org.h2.tools.Recover <options>
Options are case sensitive. Supported options are:
[-help] or [-?]    Print the list of options
[-dir <dir>]       The directory (default: .)
[-db <database>]   The database name (all databases if not set)
[-trace]           Print additional trace information
[-transactionLog]  Print the transaction log
Encrypted databases need to be decrypted first.
See also https://h2database.com/javadoc/org/h2/tools/Recover.html
```

### Restore

* toolname *Restore*
* class org.h2.tools.Restore

Restores a H2 database by extracting the database files from a .zip file.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar Restore -?
Restores a H2 database by extracting the database files from a .zip file.
Usage: java org.h2.tools.Restore <options>
Options are case sensitive. Supported options are:
[-help] or [-?]     Print the list of options
[-file <filename>]  The source file name (default: backup.zip)
[-dir <dir>]        The target directory (default: .)
[-db <database>]    The target database name (as stored if not set)
[-quiet]            Do not print progress information
See also https://h2database.com/javadoc/org/h2/tools/Restore.html
```

### RunScript

* toolname *RunScript*
* class org.h2.tools.RunScript

Runs a SQL script against a database.

```
Runs a SQL script against a database.
Usage: java org.h2.tools.RunScript <options>
Options are case sensitive. Supported options are:
[-help] or [-?]     Print the list of options
[-url "<url>"]      The database URL (jdbc:...)
[-user <user>]      The user name (default: sa)
[-password <pwd>]   The password
[-script <file>]    The script file to run (default: backup.sql)
[-driver <class>]   The JDBC driver class to use (not required in most cases)
[-showResults]      Show the statements and the results of queries
[-checkResults]     Check if the query results match the expected results
[-continueOnError]  Continue even if the script contains errors
[-options ...]      RUNSCRIPT options (embedded H2; -*Results not supported)
See also https://h2database.com/javadoc/org/h2/tools/RunScript.html
```

### Script

* toolname *Script*
* class org.h2.tools.Script

Creates a SQL script file by extracting the schema and data of a database.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar Script -?
Creates a SQL script file by extracting the schema and data of a database.
Usage: java org.h2.tools.Script <options>
Options are case sensitive. Supported options are:
[-help] or [-?]    Print the list of options
[-url "<url>"]     The database URL (jdbc:...)
[-user <user>]     The user name (default: sa)
[-password <pwd>]  The password
[-script <file>]   The target script file name (default: backup.sql)
[-options ...]     A list of options (only for embedded H2, see SCRIPT)
[-quiet]           Do not print progress information
See also https://h2database.com/javadoc/org/h2/tools/Script.html
```

### Server

* toolname *Server*
* class org.h2.tools.Server

Starts the H2 Console (web-) server, TCP, and PG server.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar Server -?
Starts the H2 Console (web-) server, TCP, and PG server.
Usage: java org.h2.tools.Server <options>
When running without options, -tcp, -web, -browser and -pg are started.
Options are case sensitive. Supported options are:
[-help] or [-?]         Print the list of options
[-web]                  Start the web server with the H2 Console
[-webAllowOthers]       Allow other computers to connect - see below
[-webDaemon]            Use a daemon thread
[-webPort <port>]       The port (default: 8082)
[-webSSL]               Use encrypted (HTTPS) connections
[-webAdminPassword]     Password of DB Console administrator
[-browser]              Start a browser connecting to the web server
[-tcp]                  Start the TCP server
[-tcpAllowOthers]       Allow other computers to connect - see below
[-tcpDaemon]            Use a daemon thread
[-tcpPort <port>]       The port (default: 9092)
[-tcpSSL]               Use encrypted (SSL) connections
[-tcpPassword <pwd>]    The password for shutting down a TCP server
[-tcpShutdown "<url>"]  Stop the TCP server; example: tcp://localhost
[-tcpShutdownForce]     Do not wait until all connections are closed
[-pg]                   Start the PG server
[-pgAllowOthers]        Allow other computers to connect - see below
[-pgDaemon]             Use a daemon thread
[-pgPort <port>]        The port (default: 5435)
[-properties "<dir>"]   Server properties (default: ~, disable: null)
[-baseDir <dir>]        The base directory for H2 databases (all servers)
[-ifExists]             Only existing databases may be opened (all servers)
[-ifNotExists]          Databases are created when accessed
[-trace]                Print additional trace information (all servers)
[-key <from> <to>]      Allows to map a database name to another (all servers)
The options -xAllowOthers are potentially risky.
For details, see Advanced Topics / Protection against Remote Access.
See also https://h2database.com/javadoc/org/h2/tools/Server.html
```

### Shell

* toolname *Shell*
* class org.h2.tools.Shell

Interactive command line tool to access a database using JDBC.

```
$ $JAVA_HOME/bin/java -jar target/h2tools-1.0-SNAPSHOT-mainTools.jar Shell -?
Interactive command line tool to access a database using JDBC.
Usage: java org.h2.tools.Shell <options>
Options are case sensitive. Supported options are:
[-help] or [-?]        Print the list of options
[-url "<url>"]         The database URL (jdbc:h2:...)
[-user <user>]         The user name
[-password <pwd>]      The password
[-driver <class>]      The JDBC driver class to use (not required in most cases)
[-sql "<statements>"]  Execute the SQL statements and exit
[-properties "<dir>"]  Load the server properties from this directory
If special characters don't work as expected, you may need to use
 -Dfile.encoding=UTF-8 (Mac OS X) or CP850 (Windows).
See also https://h2database.com/javadoc/org/h2/tools/Shell.html
```

## Examples


