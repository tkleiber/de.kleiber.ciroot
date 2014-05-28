SET HOME=C:\Oracle\JDev121200
ECHO Fuer ANT benoetigte Umgebungsvariablen
REM --------- PRODUKTION --------------
SET JAVA_HOME=%HOME%\oracle_common\jdk
SET ANT_HOME=%HOME%\oracle_common\modules\org.apache.ant_1.8.4
set ANT_OPTS=-Xms128M -Xmx850M -XX:PermSize=128M -XX:MaxPermSize=512M
REM Parameter können bei OutOfMemory Exceptions angepasst werden, 
REM die Einstellungen oben haben sich als Minimaleinstellungen bewährt
REM Bei weiteren Problemen die Datei %HOME%\jdeveloper\ide\bin\ide.conf
REM anpassen und den Speicher dort reduzieren: AddVMOption  -Xmx512M AddVMOption  -Xms128M
REM -Xms1250M -Xmx1250M -XX:PermSize=1024M -XX:MaxPermSize=2048M
set WL_DEPLOY_SERVER=soa_server1
set WL_USERNAME=weblogic
set WL_HOME=%HOME%
set SOA_ANT=%WL_HOME%\jdeveloper\bin

set TEST_DRIVER=internet explorer
rem set TEST_DRIVER=firefox

set TEST_DRIVER_PLATFORM=WINDOWS
rem set TEST_DRIVER_PLATFORM=LINUX
rem set TEST_DRIVER_PLATFORM=ANY

rem wenn der Fehler auf IE 9 reproduzierbar ist, dann aufgrund der Testdauer zunächst dort testen
set TEST_DRIVER_VERSION=Microsoft_Windows_7_Enterprise_Service_Pack_1_BUILD_6.1.7601_IE_9.0.8112.16421
rem set TEST_DRIVER_VERSION=Microsoft_Windows_7_Enterprise_Service_Pack_1_BUILD_6.1.7601_IE_9.10.9200.16521
