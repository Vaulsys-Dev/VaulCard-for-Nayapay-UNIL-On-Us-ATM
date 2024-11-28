#! /bin/sh

DIST_DIR="/home/nanoswitch/dist"

# *** Class path definition ***

LIB_JAR_PATH=$DIST_DIR/lib

LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/bouncycastle/bcprov-jdk16-138.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/bsh/bsh-1.3.0.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-beanutils.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-collections-3.2.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-configuration-1.4.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-digester-1.8.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-jxpath-1.2.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-lang-2.3.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/commons/commons-logging.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/dom4j/dom4j-1.6.1.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/antlr-2.7.6.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/asm.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/asm-attrs.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/c3p0-0.9.1.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/cglib-2.1.3.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/cglib-2.1_2jboss.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/ejb3-persistence.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/hibernate3.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/hibernate-annotations.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/hibernate-commons-annotations.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/jdbc2_0-stdext.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/hibernate/jta.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/jaxen/jaxen-1.1-beta-4.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/jbpm/jbpm-3.1.4.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/jbpm/jbpm-identity-3.1.4.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/log4j/log4j-1.2.14.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/mina/mina-core-1.1.2.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/mina/slf4j-api-1.4.3.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/mina/slf4j-log4j12-1.4.3.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/mysql/mysql-connector-java-5.0.4-bin.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-dbcp-1.2.1.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-logging.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-logging-api.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-modeler-1.1.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-pool-1.2.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/commons-validator-1.1.4.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/db2jcc.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/db2jcc_javax.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/db2jcc_license_cu.jar
LIB_JARS=$LIB_JARS:$LIB_JAR_PATH/quartz/quartz-1.5.2.jar

FNP_JARS=$FNP_JARS:$DIST_DIR/clearing-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/customer-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/eft-base-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/fanap-nanoswitch-base-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/fee-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/hsm-eracom-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/institution-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/iso8583v87-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/iso8583v87-pos-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/merchant-management-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/protocol-base-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/protocol-ifx-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/protocol-iso8583-base-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/routing-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/scheduler-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/security-base-1.0.1.jar
FNP_JARS=$FNP_JARS:$DIST_DIR/ssm-fanap-1.0.1.jar

LIB_ARGS=-Dlog4j.configuration=config/log4j.properties

echo $LIB_JARS
echo $FNP_JARS
echo $LIB_ARGS
