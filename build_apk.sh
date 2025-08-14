#!/bin/bash
# Script to easily compile the BalatroMod project into a release APK.

echo "Navigating to project directory..."
# The script assumes it is run from the repository root.
cd BalatroMod

if [ ! -f "./gradlew" ]; then
    echo "Error: gradlew not found. Make sure you are in the repository root."
    exit 1
fi

echo "Ensuring gradlew is executable..."
chmod +x ./gradlew

echo "Starting Gradle build..."
# The assembleRelease task builds the release version of the app.
./gradlew assembleRelease

echo ""
echo "Build finished!"
echo "You can find the unsigned release APK at: BalatroMod/app/build/outputs/apk/release/app-release-unsigned.apk"
echo "Note: This APK is unsigned. You will need to sign it with your own key before it can be installed on a device."
