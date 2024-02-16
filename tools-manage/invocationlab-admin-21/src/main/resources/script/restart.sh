#!/bin/sh
SERVICE_NAME="invocationlab-admin.jar"
PID=$(ps -ef | grep -w ${SERVICE_NAME} | grep -v grep | awk '{ print $2 }')
if [[ "$PID" != "" ]]; then
kill -9 ${PID}
fi
nohup java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar invocationlab-admin.jar -Duser.timezone=Asia/Shanghai 1>/dev/null 2>&1 &