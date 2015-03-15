setlocal
set SELENIUM_PATH=C:\SeleniumGridNode.2.45.0
sc stop SeleniumGridNode.2.45.0
sc delete SeleniumGridNode.2.45.0
rmdir /S /Q %SELENIUM_PATH%
endlocal
pause