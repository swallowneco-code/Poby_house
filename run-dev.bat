@echo off
rem Local dev server. Double-click this file, or run "run-dev" in a terminal.
rem Stop with Ctrl+C.
rem NOTE: keep this file ASCII-only. cmd.exe reads .bat in the OEM codepage,
rem       so Korean text here corrupts command parsing.

set "JAVA_HOME=%USERPROFILE%\.jdks\ms-17.0.20"
cd /d "%~dp0"

echo [1/2] starting MySQL container...
call docker compose up -d
if errorlevel 1 goto :dockerfail

echo [2/2] starting Spring Boot on http://localhost:8080
call "%~dp0gradlew.bat" bootRun --console=plain
goto :eof

:dockerfail
echo.
echo Docker is not running. Start Docker Desktop first, then run this again.
pause
