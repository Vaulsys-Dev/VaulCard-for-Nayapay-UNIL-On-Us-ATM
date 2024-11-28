#! /bin/sh

JAVA_HOME="/home/nanoswitch/java/jdk1.6.0_10"
echo Running Fanap Nano Switch

MAINCLASS=vaulsys.application.VaulsysWCMS
DIST_PATH="/home/nanoswitch/nano-switch"

#FILES_LIST=`find . -iname "*.jar" | awk '{print "/home/nanoswitch/dist" substr($0, 2) ":"}'`

#for JAR_NAME in `find . -iname "*.jar" | awk '{print "/home/nanoswitch/dist" substr($0, 2) ":"}'` 

for JAR_NAME in `find . -iname "*.jar" | awk '{print substr($0, 3)}'` 
do
#    echo $JAR_NAME
    CLASSPATH=$CLASSPATH:$DIST_PATH/$JAR_NAME
done

#source set_java_env.sh
#echo $LIB

#CLASSPATH=$LIB_JARS:$FNP_JARS
LIB_ARGS="-Dlog4j.configuration=config/log4j.properties"
#DEBUG_ARGS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,suspend=n,server=y";
PROPERTIES="-Xms1024m -Xmx1024m $LIB_ARGS $DEBUG_ARGS"

#echo $CLASSPATH
#echo $PROPERTIES

ERROR_CODE="22"
CUR_PATH=$PWD

if [ "$1" = "-b" ]
then
while [ $ERROR_CODE -ge "22" ]
do
kill `cat /var/run/nanoswitch.pid`
sleep 1
"$JAVA_HOME/bin/java" -cp $CLASSPATH $PROPERTIES $MAINCLASS &
echo $! > /var/run/nanoswitch.pid
ERROR_CODE=$?
cd $CUR_PATH
sleep 1
done
else
kill `cat /var/run/nanoswitch.pid`
sleep 1
"$JAVA_HOME/bin/java" -cp $CLASSPATH $PROPERTIES $MAINCLASS
echo $! > /var/run/nanoswitch.pid
ERROR_CODE=$?
cd $CUR_PATH
fi
