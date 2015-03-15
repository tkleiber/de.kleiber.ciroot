setlocal
set SELENIUM_PATH=C:\SeleniumGridHub.2.45.0
sc stop SeleniumGridHub.2.45.0
sc delete SeleniumGridHub.2.45.0
rmdir /S /Q %SELENIUM_PATH%
endlocal
pause