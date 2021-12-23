#!/bin/sh

#set -x

#--------------------------------------------------------------
BASEDIR=$(dirname "$0")
NOHUP_LOGFILE=$BASEDIR/h2.log
H2_VERSION=1.4.200
#H2_JAR=$BASEDIR/target/h2tools-1.0-SNAPSHOT-mainTools.jar
H2_JAR=$BASEDIR/target/dependency/h2-${H2_VERSION}.jar

if [ ! -f "${H2_JAR}" ]
then
  $M2_HOME/bin/mvn -Dartifact=com.h2database:h2:${H2_VERSION} dependency:copy
fi

if [ -f "${H2_JAR}" ]
then
  echo "${H2_JAR} exists!"
else
  echo "Missing jar file ${H2_JAR}!"
  return 1
fi

#--------------------------------------------------------------
H2_DATADIR=$BASEDIR/target/h2-data
#H2_TCPPORT=9092
H2_TCPPORT=9093
H2_TCPPASSWORD=test
#H2_WEBPORT=8082
H2_WEBPORT=8083
H2_WEBADMINPASSWORD=hugo

#--------------------------------------------------------------
if [ ! -f "$H2_JAR" ]; then
  echo "Missing h2 jar file $H2_JAR. Exiting with 1."
  exit 1
fi

#--------------------------------------------------------------
function usage {
    echo "Usage $0 [ start | stop | status ]"
}

#--------------------------------------------------------------
function startH2 {
    nohup java -cp $H2_JAR org.h2.tools.Server \
        -baseDir $H2_DATADIR \
        -ifNotExists \
        -web \
        -tcp \
        -tcpPort ${H2_TCPPORT} \
        -tcpPassword ${H2_TCPPASSWORD} \
        -webPort ${H2_WEBPORT} \
        -webAdminPassword ${H2_WEBADMINPASSWORD} \
        >> ${NOHUP_LOGFILE} &
}

#--------------------------------------------------------------
function stopH2 {
    nohup java -cp $H2_JAR org.h2.tools.Server \
        -tcpShutdown tcp://localhost:${H2_TCPPORT} \
        -tcpPassword ${H2_TCPPASSWORD} \
        >> ${NOHUP_LOGFILE}
}

#--------------------------------------------------------------
function statusH2 {
    echo "ABCDEFGH" | curl --no-progress-meter -v --output - telnet://localhost:${H2_TCPPORT} > /dev/null
    curl --no-progress-meter -v http://localhost:${H2_WEBPORT} > /dev/null
}

#--------------------------------------------------------------
echo "$(date) Running $0 $@" >> ${NOHUP_LOGFILE}

#--------------------------------------------------------------
case "$1" in
    start)
      echo "start h2"
      startH2
      ;;
    stop)
      echo "stop h2"
      stopH2
      ;;
    status)
      echo "status h2"
      statusH2
      ;;
    *)
      usage
      exit 2
      ;;
esac

#--------------------------------------------------------------
tail ${NOHUP_LOGFILE}

