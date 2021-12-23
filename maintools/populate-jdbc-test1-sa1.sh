#!/bin/sh


BASEDIR=$(dirname "$0")

#----------------------------------------------------------------------
TARGET_DIR=${BASEDIR}/target
JAR_FILE=${TARGET_DIR}/h2tools-0.2.0-SNAPSHOT-mainTools.jar 

function populateBugEntity {
#target/classes/sqls/bug_entity/bug_entity.sql
#target/classes/sqls/bug_entity/bug_entity_data.sql
#----------------------------------------------------------------------
$JAVA_HOME/bin/java -jar ${JAR_FILE} \
	RunScript \
	@./h2tools-jdbc-test1-sa1.txt \
	-script ${TARGET_DIR}/classes/sqls/bug_entity/bug_entity.sql

#----------------------------------------------------------------------
$JAVA_HOME/bin/java -jar ${JAR_FILE} \
	RunScript \
	@./h2tools-jdbc-test1-sa1.txt \
	-script ${TARGET_DIR}/classes/sqls/bug_entity/bug_entity_data.sql \
	-showResults
}

function populateEventEntiy {
for i in ${TARGET_DIR}/classes/sqls/event_entity/event_entity.sql ${TARGET_DIR}/classes/sqls/event_entity/event_entity_data.sql
do
$JAVA_HOME/bin/java -jar ${JAR_FILE} \
	RunScript \
	@./h2tools-jdbc-test1-sa1.txt \
	-script $i \
	-showResults
done
}

function populateTest {
#target/classes/sqls/test/test.showResults.sql
#target/classes/sqls/test/test.sql
#target/classes/sqls/test_identity/test_identity.showResults.sql
#target/classes/sqls/test_identity/test_identity.sql
for i in ${TARGET_DIR}/classes/sqls/test/test.sql
do
$JAVA_HOME/bin/java -jar ${JAR_FILE} \
	RunScript \
	@./h2tools-jdbc-test1-sa1.txt \
	-script $i \
	-showResults
done
}

populateBugEntity 
populateEventEntiy 
populateTest 

