@echo off
setlocal
cd /d "%~dp0"
for /d %%J in ("%~dp0.tools\jdk17\jdk-*") do set "JAVA_HOME=%%~fJ"
set "MAVEN_USER_HOME=%~dp0.m2"
call "%~dp0mvnw.cmd" "-Dmaven.repo.local=%~dp0.m2\repository" spring-boot:run
endlocal
