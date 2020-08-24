#!/usr/bin/python
# coding=utf-8

import os

import socket
import time
import zipfile
from urllib import urlopen

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

port = None
try:
    with zipfile.ZipFile(application_name + ".jar") as zipFile:
        file_content = zipFile.read("BOOT-INF/classes/application.yml").decode('utf-8')
        for line in file_content.splitlines():
            if line.startswith("  port:"):
                port = line.split(":")[1].strip()
                break
except IOError as e:
    print("error" + str(e))
print("Application port is:" + port)

ip_address = "localhost"
s = None
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(('8.8.8.8', 80))
    ip_address = s.getsockname()[0]
except IOError as e:
    print("error" + str(e))
finally:
    if s is not None:
        s.close()
print("Machine ip is:" + ip_address)

detect_url = "http://{}:{}/actuator/info".format(ip_address, port)
print("detect_url is:" + detect_url)
time.sleep(10)


def check_url(url):
    try:
        page = urlopen(url)
        print(page.read().decode("utf-8"))
        return True
    except Exception as e:
        print('timeout', str(e))
        return False


def print_last_lines_log(log_path, num):
    try:
        with open(log_path, "r") as out:
            lines = out.read().splitlines()
            end = len(lines) - 1
            start = end - num
            if start < 0:
                start = 0
            lines = lines[start:end]
            for line in lines:
                print(line)
    except IOError as e:
        print("error" + str(e))


try_times = 10
cur_time = 1
success = check_url(detect_url)
while not success:
    if cur_time > try_times:
        break
    print("try connect {} times...".format(cur_time))
    success = check_url(detect_url)
    cur_time = cur_time + 1
if success:
    print("Application {} start success.".format(application_name))
    print_last_lines_log("logs/{}.log".format(application_name), 300)
else:
    print("Application {} start failed.".format(application_name))
    print_last_lines_log("logs/{}.log".format(application_name), 1500)
