@echo off
setlocal

set JAR_NAME=target\${project.build.finalName}-mainTools.jar
set SERVER_OPTS=-web -tcp -ifNotExists

"%JAVA_HOME%\bin\java" -jar %JAR_NAME% Server %SERVER_OPTS%

@rem 
@rem Starts the H2 Console (web-) server, TCP, and PG server.
@rem Usage: java org.h2.tools.Server <options>
@rem When running without options, -tcp, -web, -browser and -pg are started.
@rem Options are case sensitive. Supported options are:
@rem [-help] or [-?]         Print the list of options
@rem [-web]                  Start the web server with the H2 Console
@rem [-webAllowOthers]       Allow other computers to connect - see below
@rem [-webDaemon]            Use a daemon thread
@rem [-webPort <port>]       The port (default: 8082)
@rem [-webSSL]               Use encrypted (HTTPS) connections
@rem [-webAdminPassword]     Password of DB Console administrator
@rem [-browser]              Start a browser connecting to the web server
@rem [-tcp]                  Start the TCP server
@rem [-tcpAllowOthers]       Allow other computers to connect - see below
@rem [-tcpDaemon]            Use a daemon thread
@rem [-tcpPort <port>]       The port (default: 9092)
@rem [-tcpSSL]               Use encrypted (SSL) connections
@rem [-tcpPassword <pwd>]    The password for shutting down a TCP server
@rem [-tcpShutdown "<url>"]  Stop the TCP server; example: tcp://localhost
@rem [-tcpShutdownForce]     Do not wait until all connections are closed
@rem [-pg]                   Start the PG server
@rem [-pgAllowOthers]        Allow other computers to connect - see below
@rem [-pgDaemon]             Use a daemon thread
@rem [-pgPort <port>]        The port (default: 5435)
@rem [-properties "<dir>"]   Server properties (default: ~, disable: null)
@rem [-baseDir <dir>]        The base directory for H2 databases (all servers)
@rem [-ifExists]             Only existing databases may be opened (all servers)
@rem [-ifNotExists]          Databases are created when accessed
@rem [-trace]                Print additional trace information (all servers)
@rem [-key <from> <to>]      Allows to map a database name to another (all servers)
@rem The options -xAllowOthers are potentially risky.
@rem For details, see Advanced Topics / Protection against Remote Access.
@rem See also https://h2database.com/javadoc/org/h2/tools/Server.html
@rem 