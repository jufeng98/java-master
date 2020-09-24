#!/bin/bash
echo "Application ${APPLICATION_NAME} starting, please wait..."
JAVA_OPTS="-Xms256m -Xmx512m"
nohup /usr/java/jdk1.8.0_92/bin/java -jar "${APPLICATION_NAME}".jar "${JAVA_OPTS}" 1>/dev/null 2>&1 &

# 检测应用是否启动成功
detectApplication() {
  PORT_FILE="tmp/port.properties"
  EXCEPTION_FILE="tmp/exception.log"
  SUCCESS_FLAG=true
  while :
  do
    if [[ -f "$PORT_FILE" ]]; then
      SUCCESS_FLAG=true
      break;
    fi
    if [[ -f "$EXCEPTION_FILE" ]]; then
      SUCCESS_FLAG=false
      break;
    fi
    sleep 3
  done
  if [[ $SUCCESS_FLAG == "true" ]] ; then
    echo "Application ${APPLICATION_NAME} start success."
    tail -n 100 logs/"${APPLICATION_NAME}".log
  else
    echo "Application ${APPLICATION_NAME} start failed."
    cat $EXCEPTION_FILE
  fi
  cp -a $PORT_FILE "$PORT_FILE.log" 1>/dev/null 2>&1
  cp -a $EXCEPTION_FILE "$EXCEPTION_FILE.log" 1>/dev/null 2>&1
  rm -rf $PORT_FILE
  rm -rf $EXCEPTION_FILE
}
detectApplication