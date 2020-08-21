#!/usr/bin/python
# coding=utf-8

import os

import signal
import time

application_name = None
for file_name in os.listdir("./"):
    if file_name.endswith(".jar"):
        application_name = file_name.split(".")[0]
        break

pid = os.popen("ps -ef | grep " + application_name + " | grep -v grep | awk '{ print $2 }'").read().strip()
print("{} the pid is:{}.".format(application_name, pid))

if pid == "":
    print("Application {} is already stopped.".format(application_name))
else:
    print("try to stop {}, pid is:{}，please wait...".format(application_name, pid))
    os.kill(int(pid), signal.SIGTERM)
    try_times = 0
    while (try_times < 8):
        time.sleep(1)
        pid = os.popen("ps -ef | grep " + application_name + " | grep -v grep | awk '{ print $2 }'").read().strip()
        print("repeat find pid is:" + pid)
        if pid == "":
            break
        try_times = try_times + 1
    if try_times == 8:
        os.kill(int(pid), signal.SIGKILL)
        print("Application pid:{} {} stopped success with kill -9.".format(pid, application_name))
    else:
        print("Application pid:{} {} stopped success with kill -15.".format(pid, application_name))
