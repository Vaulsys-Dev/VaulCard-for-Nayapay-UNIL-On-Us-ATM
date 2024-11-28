@echo off

rem set JAVA_HOME="/path/to/java/home"
echo Running Fanap Nano Switch

set MAINCLASS=vaulsys.application.VaulsysWCMS
set configPath= 

rem set liblist="dir lib /b /S"
rem dir lib /b /S
rem echo %lisb.list%

call set_java_env.bat 

set CLASSPATH=%LIB_JARS%;%FNP_JARS%

set PROPERTIES=-Xms256m -Xmx512m %LIB_ARGS%

rem "%JAVA_HOME%\bin\java" -cp %CLASSPATH% %PROPERTIES% %MAINCLASS%
"%JAVA_HOME%\bin\java" -cp %CLASSPATH% %PROPERTIES% %MAINCLASS%

rem for /F "Tokens=* Delims=" %%s in (lib.list) do (
rem   set %LIBRARIES%= %LIBRARIES%;%%s
rem )

rem echo %CLASSPATH%

rem set %FNP_SUIT_JARS% = 

rem set PROPERTIES=-Xms512m -Xmx2048m 
rem set CLASSPATH=%LIBRARIES%;%FNP_SUIT_JARS%

rem "%JAVA_HOME%\bin\java" -cp %CLASSPATH% %PROPERTIES% %MAINCLASS%
