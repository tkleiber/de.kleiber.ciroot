setlocal
pushd %~dp0
set SELENIUM_PATH=C:\SeleniumGridHub.2.45.0
mkdir %SELENIUM_PATH%
copy ..\..\lib\selenium-server-standalone-2.45.0.jar %SELENIUM_PATH%
copy ..\nssm.exe %SELENIUM_PATH%
set /P sc_user=Bitte Service-User eingeben    :
set /P sc_pw=Bitte Service-Passwort eingeben:
sc create SeleniumGridHub.2.45.0 binpath= "C:\SeleniumGridHub.2.45.0\nssm.exe" start= auto DisplayName= "Selenium Grid Hub 2.45.0" obj= "%sc_user%" password= "%sc_pw%" type= own
rem sc create SeleniumGridHub.2.45.0 binpath= "C:\SeleniumGridHub.2.45.0\nssm.exe" start= auto DisplayName= "Selenium Grid Hub 2.45.0" obj= "laptop\Admin" password= "IbdlA" type= own
regedit -s SeleniumGridHub.parameter.reg
sc start SeleniumGridHub.2.45.0
popd
endlocal
pause