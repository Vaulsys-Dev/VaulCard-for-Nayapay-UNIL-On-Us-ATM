#!/bin/sh

DATE=`date +%Y.%m.%d_%H-%M-%S`
LOG_NAME="/db/daily/fanap-switch-daily-db-dump_"$DATE".sql.bz2"

mysqldump fanapswitch -pWeAreInLovingKishWare | bzip2 > $LOG_NAME

END_DATE=`date +%Y.%m.%d_%H-%M-%S`
echo "BACK UP process is started at: "$DATE" and finished at: "$END_DATE
