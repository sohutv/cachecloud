#!/bin/bash

###########################################################################
# @author: leifu 
# @desc: start cachecloud
# @time: 2016-03-31 
###########################################################################

SERVER_NAME=cachecloud
DEPLOY_DIR=/opt/cachecloud-web
STDOUT_FILE=${DEPLOY_DIR}/logs/cachecloud-web.log
WAR_FILE=${DEPLOY_DIR}/cachecloud-open-web-1.0-SNAPSHOT.war

PIDS=`ps -f | grep java | grep "${DEPLOY_DIR}" |awk '{print $2}'`
if [ -n "$PIDS" ]; then
    echo "ERROR: The ${SERVER_NAME} already started!"
    echo "PID: $PIDS"
    exit 1
fi

JAVA_OPTS="-server -Xmx4g -Xms4g -Xss256k -XX:MaxDirectMemorySize=1G -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1ReservePercent=25 -XX:InitiatingHeapOccupancyPercent=40 -XX:+PrintGCDateStamps -Xloggc:/opt/cachecloud-web/logs/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=100M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/cachecloud-web/logs/java.hprof -XX:+DisableExplicitGC -XX:-OmitStackTraceInFastThrow -XX:+PrintCommandLineFlags -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.util.Arrays.useLegacyMergeSort=true -Dfile.encoding=UTF-8"
echo -e "Starting the ${SERVER_NAME} ...\c"
nohup java $JAVA_OPTS -jar ${WAR_FILE} > $STDOUT_FILE 2>&1 &
COUNT=0
while [ $COUNT -lt 1 ]; do    
    echo -e ".\c"
    sleep 1 
    COUNT=`ps -f | grep java | grep "${DEPLOY_DIR}" | awk '{print $2}' | wc -l`
    if [ $COUNT -gt 0 ]; then
        break
    fi
done

echo "OK!"
PIDS=`ps -f | grep java | grep "${DEPLOY_DIR}" | awk '{print $2}'`
echo "PID: $PIDS"
echo "STDOUT: $STDOUT_FILE"

