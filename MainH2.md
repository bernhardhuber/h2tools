# MainH2

## SYNOPSIS
```
Usage: MainH2 [-hV] [--driver=DRIVER] [--password=PASSWORD] [--url=URL]
              [--user=USER] [@<filename>...] [COMMAND]
```

## DESCRIPTION

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

