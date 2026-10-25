@echo off
setlocal
title Send Files to REV Control Hub

REM ===== Configuration =====
set "HUB_IP=192.168.43.1:5555"
set "SOURCE=C:\Rajkumar\FTC-2026\TeamCode\src\main\java\org\firstinspires\ftc\teamcode"
set "DEST=/sdcard/FIRST/java/src/org/firstinspires/ftc/teamcode"

REM ===== Find ADB =====
set "ADB=adb.exe"

where adb.exe >nul 2>&1
if errorlevel 1 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    ) else if exist "C:\Android\platform-tools\adb.exe" (
        set "ADB=C:\Android\platform-tools\adb.exe"
    ) else (
        echo ERROR: adb.exe not found.
        echo Install Android SDK Platform-Tools or edit this BAT file.
        pause
        exit /b 1
    )
)

REM ===== Validate source folder =====
if not exist "%SOURCE%\" (
    echo ERROR: Source folder does not exist:
    echo %SOURCE%
    pause
    exit /b 1
)

echo.
echo ========================================
echo     Sending files to REV Control Hub
echo ========================================
echo.

REM ===== Connect =====
"%ADB%" connect %HUB_IP%
if errorlevel 1 (
    echo ERROR: Could not connect to the Hub.
    pause
    exit /b 1
)

REM ===== Transfer =====
echo.
echo Copying files...
"%ADB%" push "%SOURCE%\." "%DEST%"

if errorlevel 1 (
    echo.
    echo ERROR: File transfer failed.
    "%ADB%" disconnect %HUB_IP%
    pause
    exit /b 1
)

REM ===== Disconnect =====
echo.
"%ADB%" disconnect %HUB_IP%

echo.
echo SUCCESS: File transfer completed.
pause

