#!/bin/sh

DATE=`date +%Y.%m.%d_%H-%M-%S`
LOG_NAME="/db/fanap-switch-db-dump_"$DATE".sql.gz"

mysqldump fanapswitch -pWeAreInLovingKishWare | gzip > $LOG_NAME

END_DATE=`date +%Y.%m.%d_%H-%M-%S`
echo "BACK UP process started at: "$DATE" and finished at: "$END_DATE
