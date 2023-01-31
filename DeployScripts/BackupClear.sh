#!/bin/bash
if [ ! -z "$SERVER_ENV" ] && [ ! -z "$SERVER_APP_NAME" ];
        then
                mkdir /opt/$SERVER_APP_NAME/backup
                cd /opt/$SERVER_APP_NAME/backend/portal
                zip -r /opt/$SERVER_APP_NAME/backup/backend *
                rm -rf /opt/$SERVER_APP_NAME/backend/portal/*

                aws s3 cp /opt/$SERVER_APP_NAME/backup/backend*.zip s3://sbm-backup-applications/$SERVER_APP_NAME/$SERVER_ENV/

                rm -rf /opt/$SERVER_APP_NAME/backup/backend*.zip
        else
                ls -l /VariavelNaoExiste
fi
