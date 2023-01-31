#!/bin/bash

PROCESSOR_NAME="$SERVER_APP_NAME.jar"
cd /opt/$SERVER_APP_NAME/backend
unzip *.zip
rm -rf *.zip
echo "Iniciando a execução da AST!"
java -jar $PROCESSOR_NAME &> /dev/null &