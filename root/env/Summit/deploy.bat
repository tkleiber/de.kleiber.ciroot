@echo off
pushd %~dp0
setlocal
cls
call setenv.bat
pushd ..\..
set path=%ANT_HOME%\bin;%path%
ant
popd
popd
endlocal
pause
