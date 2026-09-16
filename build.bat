@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_DIR=%~dp0"
set "DIST_DIR=%PROJECT_DIR%dist"
set "SOURCE_JAR=%PROJECT_DIR%target\TreeHarvester-26.2_1.1.jar"
set "MAVEN_CMD=mvn"
set "JDK_25_HOME="
set "MAVEN_REPOSITORY=%PROJECT_DIR%.tools\m2"

for /f "tokens=2,*" %%A in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\25" /v JavaHome 2^>nul ^| find "JavaHome"') do set "JDK_25_HOME=%%B"
if not defined JDK_25_HOME (
    echo [ERROR] JDK 25 was not found. Install JDK 25 and try again.
    exit /b 1
)
if not exist "%JDK_25_HOME%\bin\java.exe" (
    echo [ERROR] JDK 25 is not usable: %JDK_25_HOME%
    exit /b 1
)
set "JAVA_HOME=%JDK_25_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

where mvn >nul 2>&1
if errorlevel 1 (
    set "MAVEN_CMD=%PROJECT_DIR%.tools\apache-maven-3.9.11\bin\mvn.cmd"
    if not exist "!MAVEN_CMD!" (
        echo [ERROR] Maven was not found in PATH or .tools. Install Maven and try again.
        exit /b 1
    )
)

if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"

rem Clear generated output while preserving the dist directory itself.
del /a /f /q "%DIST_DIR%\*" >nul 2>&1
for /d %%D in ("%DIST_DIR%\*") do rd /s /q "%%~fD"

pushd "%PROJECT_DIR%"
for %%L in (en_US zh_TW zh_CN ja_JP) do (
    echo [INFO] Building %%L...
    call "%MAVEN_CMD%" "-Dmaven.repo.local=%MAVEN_REPOSITORY%" "-Ddefault.language=%%L" clean package
    if errorlevel 1 (
        popd
        echo [ERROR] Build failed for %%L.
        exit /b 1
    )

    if not exist "%SOURCE_JAR%" (
        popd
        echo [ERROR] Built JAR was not found: %SOURCE_JAR%
        exit /b 1
    )

    copy /y "%SOURCE_JAR%" "%DIST_DIR%\TreeHarvester_26.2_1.1-%%L.jar" >nul
    if errorlevel 1 (
        popd
        echo [ERROR] Could not copy the %%L JAR to dist.
        exit /b 1
    )
)
popd

echo [OK] Build complete: %DIST_DIR%
endlocal
