setlocal
pushd %~dp0
set SELENIUM_PATH=C:\SeleniumGridNode.2.42.0
FOR /F "tokens=2 delims==,-" %%a In ('wmic os get Caption /value') DO SET OS_NAME=%%a
FOR /F "tokens=2 delims==,-" %%a In ('wmic os get CSDVersion /value') DO SET OS_SERVICE_PACK=%%a
FOR /F "tokens=2 delims==,-" %%a In ('wmic os get Version /value') DO SET OS_VERSION=%%a
FOR /F "tokens=2*" %%i IN ('reg query "HKLM\SOFTWARE\Microsoft\Internet Explorer" /v Version ^| FIND "Version"') DO SET INTERNET_EXPLORER_VERSION=%%j
FOR /F "tokens=2*" %%i IN ('reg query "HKLM\SOFTWARE\Wow6432Node\Mozilla\Mozilla Firefox" /v CurrentVersion ^| FIND "CurrentVersion"') DO SET FIREFOX_VERSION=%%j
if "%FIREFOX_VERSION%#" NEQ "#" (
  SET FIREFOX_VERSION=%FIREFOX_VERSION:(=%
  SET FIREFOX_VERSION=%FIREFOX_VERSION:)=%
)

set INTERNET_EXPLORER_TEST_PROFILE=%OS_NAME%%OS_SERVICE_PACK%_BUILD_%OS_VERSION%_IE_%INTERNET_EXPLORER_VERSION%
set INTERNET_EXPLORER_TEST_PROFILE=%INTERNET_EXPLORER_TEST_PROFILE: =_%
echo %INTERNET_EXPLORER_TEST_PROFILE%

set FIREFOX_TEST_PROFILE=%OS_NAME%%OS_SERVICE_PACK%_BUILD_%OS_VERSION%_FF_%FIREFOX_VERSION%
set FIREFOX_TEST_PROFILE=%FIREFOX_TEST_PROFILE: =_%
echo %FIREFOX_TEST_PROFILE%

mkdir %SELENIUM_PATH%
copy ..\..\lib\selenium-server-standalone-2.42.0.jar %SELENIUM_PATH%
copy ..\..\lib\IEDriverServer.exe %SELENIUM_PATH%
copy ..\nssm.exe %SELENIUM_PATH%

copy NodeConfig.json %SELENIUM_PATH%
if "%INTERNET_EXPLORER_VERSION%#" NEQ "#" (
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###INTERNET_EXPLORER_TEST_PROFILE### %INTERNET_EXPLORER_TEST_PROFILE%
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###INTERNET_EXPLORER_MAX_INSTANCES### 5
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###MAX_SESSIONS### 5
) else (
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###INTERNET_EXPLORER_MAX_INSTANCES### 0
)

if "%FIREFOX_VERSION%#" NEQ "#" (
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###FIREFOX_TEST_PROFILE### %FIREFOX_TEST_PROFILE%
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###FIREFOX_MAX_INSTANCES### 5
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###MAX_SESSIONS### 5
) else (
  replace.vbs %SELENIUM_PATH%\NodeConfig.json ###FIREFOX_MAX_INSTANCES### 0
)

replace.vbs %SELENIUM_PATH%\NodeConfig.json ###MAX_SESSIONS### 0

set /P sc_user=Bitte Service-User eingeben    :
set /P sc_pw=Bitte Service-Passwort eingeben:
sc create SeleniumGridNode.2.42.0 binpath= "C:\SeleniumGridNode.2.42.0\nssm.exe" start= auto DisplayName= "Selenium Grid Node 2.42.0" obj= "%sc_user%" password= "%sc_pw%" type= own
rem Für Screenshots im Internet Explorer muss der Service als Accout "Lokales System" im Modus "Datenaustausch zwischen Dienst und Deskttop zulassen" ausgeführt werden
rem sc create SeleniumGridNode.2.42.0 binpath= "C:\SeleniumGridNode.2.42.0\nssm.exe" start= auto DisplayName= "Selenium Grid Node 2.42.0" type= own type= interact

regedit -s SeleniumGridNode.parameter.reg
regedit -s IE11.reg
sc start SeleniumGridNode.2.42.0
popd
endlocal
pause