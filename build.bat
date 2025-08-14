@echo off
echo Navigating to the Android project directory...
cd BalatroMod

echo Starting the Gradle build...
call gradlew.bat assembleRelease

echo.
echo Build finished!
echo You can find the unsigned release APK at: app\build\outputs\apk\release\
pause
