@echo off
setlocal

rem Copyright (c) 1999, 2006 Tanuki Software Inc.
rem
rem Java Service Wrapper command based script
rem

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem
rem Find the application home.
rem
rem %~dp0 is location of current script under NT
set _REALPATH=%~dp0

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%-windows-x86-32.exe
if exist "%_WRAPPER_EXE%" goto validate
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%-windows-x86-64.exe
if exist "%_WRAPPER_EXE%" goto validate
set _WRAPPER_EXE=%_REALPATH%%_WRAPPER_BASE%.exe
if exist "%_WRAPPER_EXE%" goto validate
echo Unable to locate a Wrapper executable using any of the following names:
echo %_REALPATH%%_WRAPPER_BASE%-windows-x86-32.exe
echo %_REALPATH%%_WRAPPER_BASE%-windows-x86-64.exe
echo %_REALPATH%%_WRAPPER_BASE%.exe
pause
goto :eof

:validate
rem Find the requested command.
for /F %%v in ('echo %1^|findstr "^console$ ^start$ ^pause$ ^resume$ ^stop$ ^restart$ ^install$ ^remove"') do call :exec set COMMAND=%%v

if "%COMMAND%" == "" (
    echo Usage: %0 { console : start : pause : resume : stop : restart : install : remove }
    pause
    goto :eof
) else (
    shift
)

rem
rem Find the wrapper.conf
rem
:conf
set _WRAPPER_CONF="%_REALPATH%..\conf\wrapper.conf"

rem
rem Run the application.
rem At runtime, the current directory will be that of wrapper.exe
rem
call :%COMMAND%
if errorlevel 1 pause
goto :eof

:console
"%_WRAPPER_EXE%" -c %_WRAPPER_CONF%
goto :eof

:start
"%_WRAPPER_EXE%" -t %_WRAPPER_CONF%
goto :eof

:pause
"%_WRAPPER_EXE%" -a %_WRAPPER_CONF%
goto :eof

:resume
"%_WRAPPER_EXE%" -e %_WRAPPER_CONF%
goto :eof

:stop
"%_WRAPPER_EXE%" -p %_WRAPPER_CONF%
goto :eof

:install
"%_WRAPPER_EXE%" -i %_WRAPPER_CONF%
goto :eof

:remove
"%_WRAPPER_EXE%" -r %_WRAPPER_CONF%
goto :eof

:restart
call :stop
call :start
goto :eof

:exec
%*
goto :eof

