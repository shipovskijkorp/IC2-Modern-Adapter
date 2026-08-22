@echo off
setlocal
set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v9.2.1/gradle/wrapper/gradle-wrapper.jar"

if not exist "%WRAPPER_JAR%" call :bootstrap_wrapper
if errorlevel 1 exit /b %ERRORLEVEL%

if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java.exe"
)

"%JAVA_EXE%" %JAVA_OPTS% -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%

:bootstrap_wrapper
if not exist "%APP_HOME%gradle\wrapper" mkdir "%APP_HOME%gradle\wrapper"
echo Bootstrapping Gradle 9.2.1 wrapper...

where curl.exe >nul 2>nul
if not errorlevel 1 (
    curl.exe -fL --retry 3 --connect-timeout 20 "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
    if not errorlevel 1 goto :wrapper_ready
    if exist "%WRAPPER_JAR%" del /q "%WRAPPER_JAR%"
)

where pwsh.exe >nul 2>nul
if not errorlevel 1 (
    pwsh.exe -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    if not errorlevel 1 goto :wrapper_ready
    if exist "%WRAPPER_JAR%" del /q "%WRAPPER_JAR%"
)

if exist "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" (
    "%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe" -NoLogo -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'"
    if not errorlevel 1 goto :wrapper_ready
    if exist "%WRAPPER_JAR%" del /q "%WRAPPER_JAR%"
)

echo ERROR: Could not download the Gradle 9.2.1 wrapper JAR.
echo        Tried curl.exe, pwsh.exe, and Windows PowerShell.
echo        URL: %WRAPPER_URL%
exit /b 1

:wrapper_ready
if not exist "%WRAPPER_JAR%" (
    echo ERROR: Gradle wrapper bootstrap reported success but the JAR is missing.
    exit /b 1
)
exit /b 0
