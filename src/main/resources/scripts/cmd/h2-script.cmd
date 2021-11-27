@echo off
setlocal

set JAR_NAME=target\${project.build.finalName}-mainTools.jar

"%JAVA_HOME%\bin\java" -jar %JAR_NAME% Script %*

@rem 
@rem Creates a SQL script file by extracting the schema and data of a database.
@rem Usage: java org.h2.tools.Script <options>
@rem Options are case sensitive. Supported options are:
@rem [-help] or [-?]    Print the list of options
@rem [-url "<url>"]     The database URL (jdbc:...)
@rem [-user <user>]     The user name (default: sa)
@rem [-password <pwd>]  The password
@rem [-script <file>]   The target script file name (default: backup.sql)
@rem [-options ...]     A list of options (only for embedded H2, see SCRIPT)
@rem [-quiet]           Do not print progress information
@rem See also https://h2database.com/javadoc/org/h2/tools/Script.html
@rem 
