#!/usr/bin/python
# coding=utf-8

import os

import time

application_name = None
for file_name in os.listdir("./"):
    if file_name.endswith(".jar"):
        application_name = file_name.split(".")[0]
        break
print("Application {} starting, please wait...".format(application_name))

java_opts = "-Duser.timezone=GMT+8 -Xms512m -Xmx512m"
if not os.path.exists("logs"):
    os.mkdir("logs")
os.system("""
    nohup /usr/java/jdk1.8.0_92/bin/java -jar {}.jar {} 1>/dev/null 2>&1  &
    """.format(application_name, java_opts, application_name))


def print_last_lines_log(log_path, num):
    try:
        with open(log_path, "r") as out:
            lines = out.read().splitlines()[-num:]
            for line in lines:
                print(line)
    except IOError as e:
        print("error" + str(e))


port_file = "tmp/port.properties"
exception_file = "tmp/exception.log"
success_flag = True
while True:
    if os.path.exists(port_file):
        success_flag = True
        break
    if os.path.exists(exception_file):
        success_flag = False
        break
    time.sleep(3)
if success_flag:
    print("Application {} start success.".format(application_name))
    print_last_lines_log("logs/{}.log".format(application_name), 100)
else:
    print("Application {} start failed.".format(application_name))
    print_last_lines_log(exception_file, 1000)
