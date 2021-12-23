@echo off
setlocal

set JAR_NAME=target\${project.build.finalName}-mainTools.jar

"%JAVA_HOME%\bin\java" -jar %JAR_NAME% Shell %*

@rem 
@rem Interactive command line tool to access a database using JDBC.
@rem Usage: java org.h2.tools.Shell <options>
@rem Options are case sensitive. Supported options are:
@rem [-help] or [-?]        Print the list of options
@rem [-url "<url>"]         The database URL (jdbc:h2:...)
@rem [-user <user>]         The user name
@rem [-password <pwd>]      The password
@rem [-driver <class>]      The JDBC driver class to use (not required in most cases)
@rem [-sql "<statements>"]  Execute the SQL statements and exit
@rem [-properties "<dir>"]  Load the server properties from this directory
@rem If special characters don't work as expected, you may need to use
@rem -Dfile.encoding=UTF-8 (Mac OS X) or CP850 (Windows).
@rem See also https://h2database.com/javadoc/org/h2/tools/Shell.html
@rem 
