/*
  Sample groovy script selecting h2 database
*/

// load h2
@Grab('com.h2database:h2:1.4.200')
@GrabConfig(systemClassLoader=true)

import groovy.sql.Sql
import org.h2.tools.Csv

/**
 *  print all rows once
 */
int sqlAllRows( Sql sql, String sqlSelect ) {
    println 'sql rows ***'
    sql.withTransaction {
        // use 'sql' instance ...
        List bugEntityList = sql.rows( sqlSelect )
        println "bugEntityList: ${bugEntityList}"
    }
    return 0
}

/**
 * print each row
 */
int sqlEachRow1( Sql sql, String sqlSelect ) {
    println 'sql eachRow ***'
    sql.withTransaction {
        int rowNum = 0
        sql.eachRow( sqlSelect ) { row ->
          println "row ${rowNum}: ${row}" 
          rowNum += 1
        }
    }
    return 0
}

/**
 * print for each row only selected columns
 */
int sqlEachRowSelectedColumns( Sql sql, String sqlSelect ) {
    println 'sql eachRow ***'
    sql.withTransaction {
        int rowNum = 0
        sql.eachRow( sqlSelect ) { row ->
          println "row ID ${row.ID}, CREATED_WHEN ${row[2]}, BUG_DESCRIPTION ${row.BUG_DESCRIPTION}"
          rowNum += 1
        }
    }
    return 0;
}

/**
 * print result set as CSV
 */
int sqlCsv( Sql sql, String sqlSelect ) {    
    println 'sql resultSet CSV ***'
    final Csv csv = new Csv()
    try (StringWriter sw = new StringWriter()) {
        sql.withTransaction {
            sql.query(sqlSelect) { resultSet ->
                csv.write( sw, resultSet )
            }
        }
        sw.flush()
        println sw.toString()
    }
    return 0;
}

// define h2 database access
final String driver = 'org.h2.Driver'

final String url = 'jdbc:h2:tcp://localhost:9093/test1'
final String user = 'sa1'
final String password = 'sa1'
final Map<String, String> jdbcConnection = [ 'url':url, 'user': user, 'password': password, 'driver': driver ]

//---
// define sql select
final String sqlSelect =  'select * from BUG_ENTITY';

// access h2 database
//Sql.withInstance( url, user, password, driver ) { sql ->
Sql.withInstance( jdbcConnection ) { sql ->
    //sqlAllRows sql, sqlSelect
    sqlEachRow1 sql, sqlSelect
    //sqlEachRowSelectedColumns sql, sqlSelect
    sqlCsv sql, sqlSelect
}

return 0
