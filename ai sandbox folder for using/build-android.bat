@echo off
echo ================================================
echo   LocalLLM-Frontend Android v2 Build Script
echo ================================================
echo.

echo Step 1: Checking Android SDK...
if exist "%ANDROID_HOME%" (
    echo   Android SDK found at: %ANDROID_HOME%
) else (
    echo   WARNING: ANDROID_HOME not set. Please set it to your Android SDK path.
    echo   Example: setx ANDROID_HOME "C:\Users\{User}\AppData\Local\Android\Sdk"
)

echo.
echo Step 2: Gradle wrapper check...
if exist "gradlew.bat" (
    echo   Gradle wrapper found.
) else (
    echo   WARNING: gradlew.bat not found in current directory.
)

echo.
echo Step 3: Creating necessary directories...
if not exist "build" mkdir build
if not exist "build\intermediates" mkdir "build\intermediates"
if not exist "build\outputs" mkdir "build\outputs"

echo.
echo ================================================
echo   To build the app, run in PowerShell:
echo ================================================
echo   cd "C:\Users\Mickey\locally-uncensored-master\locally-uncensored-master\mobile-client\ai sandbox folder for using"
echo   gradlew.bat assembleDebug
echo.
echo   The APK will be located at: app\build\outputs\apk\debug\app-debug.apk
echo ================================================
echo.

pause
