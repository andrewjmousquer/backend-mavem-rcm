#!/bin/bash

PROCESSOR_NAME="$SERVER_APP_NAME.jar"
PID=`ps -aux | grep -v grep | grep "$PROCESSOR_NAME" | awk '{print $2}'`

if [ ! -z "$PID" ]
then
        echo "Parando a execução do WebPortal Backend...(" $PID ")"
        kill -9 $PID
else
        echo "O processo WebPortal Backend."
fi