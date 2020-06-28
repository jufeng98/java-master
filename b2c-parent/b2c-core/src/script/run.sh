`#!/bin/bash
# 找到并导出jar包名称
export APPLICATION_NAME=`ls | grep .jar | sed s/".jar"/""/`

source stop.sh
source start.sh