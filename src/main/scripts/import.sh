#!/bin/sh

JAVA_HOME="/home/nanoswitch/java/jdk1.6.0_10"

echo Importing Data For Fanap Nano Switch

MAINCLASS=vaulsys.config.importer.Import
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
PROPERTIES="-Xms256m -Xmx512m $LIB_ARGS"

#echo $CLASSPATH
#echo $PROPERTIES

if [ "$#" = "1" ] ;
then
    IMPORT_CONF_FILE=$1
else
    IMPORT_CONF_FILE="config/importData/formats/files/importConfig.xml"
fi

FNP_SWITCH_ARGS="-config "$IMPORT_CONF_FILE

"$JAVA_HOME/bin/java" -cp $CLASSPATH $PROPERTIES $MAINCLASS $FNP_SWITCH_ARGS