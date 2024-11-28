#! /bin/sh

JAVA_HOME="/home/nanoswitch/java/jdk1.6.0_10"
echo Running Fanap Nano Switch

#MAINCLASS=vaulsys.othermains.CalculateFess
#MAINCLASS=vaulsys.othermains.DabaseMerger
MAINCLASS=vaulsys.othermains.Reconcile
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
PROPERTIES="-Xms1024m -Xmx1024m $LIB_ARGS"

CUR_PATH=$PWD

"$JAVA_HOME/bin/java" -cp $CLASSPATH $PROPERTIES $MAINCLASS $1

