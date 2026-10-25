@echo off
title EduExam Pro Desktop
cd /d "%~dp0"
if exist "%JAVA_HOME%\bin\javaw.exe" (
  start "" "%JAVA_HOME%\bin\javaw.exe" -jar "online-exam-system-8.0.jar"
) else (
  start "" javaw -jar "online-exam-system-8.0.jar"
)
