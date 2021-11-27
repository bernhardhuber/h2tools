# h2tools
Commandline tools using h2 database

# Build Status

[![Java CI with Maven](https://github.com/bernhardhuber/h2tools/actions/workflows/maven.yml/badge.svg)](https://github.com/bernhardhuber/h2tools/actions/workflows/maven.yml)

# MainH2

```
Usage: MainH2 [-hV] [--driver=DRIVER] [--password=PASSWORD] [--url=URL]
              [--user=USER] [@<filename>...] [COMMAND]
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

      [@<filename>...]      One or more argument files containing options.
      --driver=DRIVER       jdbc Driver
                              Default: org.h2.Driver
  -h, --help                Show this help message and exit.
      --password=PASSWORD   h2 password
                              Default:
      --url=URL             h2 jdbc URL
                              Default: jdbc:h2:mem:/test1
      --user=USER           h2 user
                              Default: sa
  -V, --version             Print version information and exit.
Commands:
  script    Creates a SQL script file by extracting the schema and data of a
              database.
  csvRead   Reading a CSV File from within a database.
  csvWrite  Writes a CSV (comma separated values).
  show      Lists the schemas, tables, or the columns of a table.
```

# Available Commands

Command Name, Class, Description

*Backup*, class org.h2.tools.Backup,
Creates a backup of a database. This tool copies all database files. 
The database must be closed before using this tool. 
To create a backup while the database is in use, run the BACKUP SQL statement.
In an emergency, for example if the application is not responding, 
creating a backup using the Backup tool is possible by using the quiet mode. 
However, if the database is changed while the backup is running in quiet mode, 
the backup could be corrupt.

*ChangeFileEncryption*, class org.h2.tools.ChangeFileEncryption,
Allows changing the database file encryption password or algorithm. 
This tool can not be used to change a password of a user. 
The database must be closed before using this tool.

*Console*, class org.h2.tools.Console,
Starts the H2 Console (web-) server, as well as the TCP and PG server.

*ConvertTraceFile*, class org.h2.tools.ConvertTraceFile,
Converts a .trace.db file to a SQL script and Java source code. 
SQL statement statistics are listed as well.

*CreateCluster*, class org.h2.tools.CreateCluster,
Creates a cluster from a stand-alone database. 
Copies a database to another location if required.

*DeleteDbFiles*, class org.h2.tools.DeleteDbFiles,
Deletes all files belonging to a database. 
The database must be closed before calling this tool.

*Recover*, class org.h2.tools.Recover,
Helps recovering a corrupted database.

*Restore*, class org.h2.tools.Restore,
Restores a H2 database by extracting the database files from a .zip file.

*RunScript*, class org.h2.tools.RunScript,
Runs a SQL script against a database.

*Script*, class org.h2.tools.Script,
Creates a SQL script file by extracting the schema and data of a database.

*Server*, class org.h2.tools.Server,
Starts the H2 Console (web-) server, TCP, and PG server.

*Shell*, class org.h2.tools.Shell,
Interactive command line tool to access a database using JDBC.


# MainTools

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

# References

H2 http://h2database.com/
