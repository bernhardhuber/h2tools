@echo off
setlocal

set JAR_NAME=target\${project.build.finalName}-mainTools.jar

"%JAVA_HOME%\bin\java" -jar %JAR_NAME% RunScript %*

@rem 
@rem Runs a SQL script against a database.
@rem Usage: java org.h2.tools.RunScript <options>
@rem Options are case sensitive. Supported options are:
@rem [-help] or [-?]     Print the list of options
@rem [-url "<url>"]      The database URL (jdbc:...)
@rem [-user <user>]      The user name (default: sa)
@rem [-password <pwd>]   The password
@rem [-script <file>]    The script file to run (default: backup.sql)
@rem [-driver <class>]   The JDBC driver class to use (not required in most cases)
@rem [-showResults]      Show the statements and the results of queries
@rem [-checkResults]     Check if the query results match the expected results
@rem [-continueOnError]  Continue even if the script contains errors
@rem [-options ...]      RUNSCRIPT options (embedded H2; -*Results not supported)
@rem See also https://h2database.com/javadoc/org/h2/tools/RunScript.html
@rem 
