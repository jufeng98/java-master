#!/bin/bash
echo "Application ${APPLICATION_NAME} starting, please wait..."
JAVA_OPTS="-Xms256m -Xmx512m"
nohup /usr/java/jdk1.8.0_92/bin/java -jar "${APPLICATION_NAME}".jar "${JAVA_OPTS}" 1>/dev/null 2>&1 &

# 检测应用是否启动成功
detectApplication() {
  PORT_FILE="tmp/port.properties"
  EXCEPTION_FILE="tmp/exception.log"
  rm -rf $PORT_FILE
  rm -rf $EXCEPTION_FILE
  SUCCESS_FLAG=true
  TRY_TIMES=100
  CUR_TIMES=0
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
    CUR_TIMES=$[${CUR_TIMES}+1]
    if [[ ${CUR_TIMES} -ge ${TRY_TIMES} ]]; then
      SUCCESS_FLAG=false
      break;
    fi
    sleep 2
    echo "detect ${CUR_TIMES} times..."
  done
  if [[ $SUCCESS_FLAG == "true" ]] ; then
    echo "Application ${APPLICATION_NAME} start success."
    tail -n 100 logs/"${APPLICATION_NAME}".log
    cp -a $PORT_FILE "$PORT_FILE.log" 1>/dev/null 2>&1
    rm -rf $PORT_FILE
    return 0
  else
    echo "Application ${APPLICATION_NAME} start failed."
    cat $EXCEPTION_FILE 2>/dev/null
    cp -a $EXCEPTION_FILE "$EXCEPTION_FILE.log" 1>/dev/null 2>&1
    rm -rf $EXCEPTION_FILE
    return 1
  fi
}
detectApplication