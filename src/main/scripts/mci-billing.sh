#!/bin/sh

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
PRG_HOME=`cd "$PRGDIR" ; pwd`
# Only set JAVA_HOME if not already set
[ -z "$JAVA_HOME" ] && JAVA_HOME="/home/tools/jdk1.6.0_10"

MAIN_CLASS="vaulsys.othermains.MCIBilling"
PRG_PID="$PRG_HOME/mci-billing.pid"
PRG_NAME="MCIBilling"
INPUT_ARGS="$PRG_HOME/out"
CLASSPATH=

CUR_DIR=`pwd`
cd $PRG_HOME
for JAR_NAME in `find . -iname "*.jar" | awk '{print substr($0, 3)}'`
do
    CLASSPATH=$CLASSPATH:$PRG_HOME/$JAR_NAME
done
cd $CUR_DIR

start() {
	if [ ! -f $PRG_PID ]; then
		"$JAVA_HOME/bin/java" -cp $CLASSPATH $JAVA_OPTS $MAIN_CLASS $INPUT_ARGS &
		echo $! > $PRG_PID
		echo "$PRG_NAME started with pid=$!"
	else
		echo "$PRG_NAME has been started: file $PRG_PID existed"
	fi
}

stop() {
	if [ -f $PRG_PID ]; then
		kill -9 `cat $PRG_PID`
		rm -f $PRG_PID
		echo "$PRG_NAME stopped"
	fi
}

restart() {
	stop
	start
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		restart
		;;
	*)
		echo "Usage: $0 {start|stop|restart}"
		exit 2
esac
