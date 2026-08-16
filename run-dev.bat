@echo off
rem Local dev server. Double-click this file, or run "run-dev" in a terminal.
rem Stop with Ctrl+C.
rem NOTE: keep this file ASCII-only. cmd.exe reads .bat in the OEM codepage,
rem       so Korean text here corrupts command parsing.

rem Do not hardcode one JDK folder name. A pinned name breaks the moment the
rem JDK is updated or reinstalled under a different vendor prefix.
rem Use JAVA_HOME only if it already points somewhere real; otherwise fall back
rem to the first Java 17 found under .jdks, and finally to whatever is on PATH.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :javaready
set "JAVA_HOME="
for /d %%D in ("%USERPROFILE%\.jdks\*17*") do if exist "%%D\bin\java.exe" set "JAVA_HOME=%%D"
:javaready

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
