@echo off
setlocal EnableDelayedExpansion

::  colors
set "red=[91m"
set "green=[92m"
set "yellow=[93m"
set "cyan=[96m"
set "reset=[0m"

set "JAR_PATH=%~dp0APKEditor.jar"
if not exist "%JAR_PATH%" (
    echo %red%[ERROR] APKEditor.jar not found in %~dp0%reset%
    exit /b 1
)

if "%~1"=="" (
    echo %yellow%APKEditor%reset% 
	
    echo %cyan%Usage:%reset% apkeditor [command] [options]
    echo %cyan%Commands:%reset% use %green% apkeditor -h %reset%- to see the available commands
    echo %cyan%Example:%reset% apkeditor d -i app.apk
    exit /b 0
)

echo %yellow%[INFO] Running command: java -jar APKEditor.jar %* %reset%
java -jar "%JAR_PATH%" %*

exit /b %ERRORLEVEL%
