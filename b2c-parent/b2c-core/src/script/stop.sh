#!/bin/bash
# 得到微服务进程id
PID=$(ps -ef | grep ${APPLICATION_NAME} | grep -v grep | awk '{ print $2 }')
echo "${APPLICATION_NAME} the pid is:${PID}."
if [[ -z "${PID}" ]]; then
    echo "Application ${APPLICATION_NAME} is already stopped."
else
	echo "try to stop ${APPLICATION_NAME}, pid is:${PID}，please wait..."
	# 发送关闭信号,使得虚拟机能完成关闭前的收尾工作并安全退出
    kill -15 ${PID}
    TRY_TIMES=0
	while [[ "$TRY_TIMES" -lt 8 ]];
    do
		sleep 1
		# 重新寻找微服务的进程id
		PID=$(ps -ef | grep ${APPLICATION_NAME} | grep -v grep | awk '{ print $2 }')
		echo "repeat find pid is:${PID}"
		if [[ -z "$PID" ]]; then
		    # 虚拟机进程已成功自行退出
		    break;
		fi
		TRY_TIMES=$[${TRY_TIMES}+1]
	done
	if [[ ${TRY_TIMES} == 8 ]]; then
	    # 检测了8次(即等待了8s),虚拟机进程均未能自行退出,则强制结束进程
	    kill -9 ${PID}
	    echo "Application pid:${PID} ${APPLICATION_NAME} stopped success with kill -9."
	else
	    echo "Application pid:${PID} ${APPLICATION_NAME} stopped success with kill -15."
	fi
fi