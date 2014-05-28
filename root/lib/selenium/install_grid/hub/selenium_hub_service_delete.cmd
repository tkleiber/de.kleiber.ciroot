setlocal
set SELENIUM_PATH=C:\SeleniumGridHub.2.42.0
sc stop SeleniumGridHub.2.42.0
sc delete SeleniumGridHub.2.42.0
rmdir /S /Q %SELENIUM_PATH%
endlocal
pause