# Balatro Mod Application

This is an Android application for loading Lua mods into the game Balatro without needing root access.

## How it Works

This application uses the `VirtualApp` framework to create a sandboxed environment where the Balatro game can be run. By using native hooks to intercept file system calls, the application can redirect the game's request for its main `boot.lua` script to a custom, dynamically generated `loader.lua` script.

This loader script first executes the original game code, and then loads any user-enabled mods, allowing for a flexible and non-destructive modding experience.

## Building from Source

To compile this application yourself, you will need to have the Gradle build tool installed on your system.

### 1. Install Gradle

**On Linux (Debian/Ubuntu):**
```bash
sudo apt update
sudo apt install gradle
```

**On Windows (using Chocolatey package manager):**
```powershell
choco install gradle
```

For other operating systems or for manual installation instructions, please refer to the official Gradle installation guide: [https://gradle.org/install/](https://gradle.org/install/)

### 2. Compile the APK

Once Gradle is installed, navigate to this `BalatroMod` directory in your terminal and run the following command:

```bash
gradle assembleRelease
```

The unsigned release APK will be located in `app/build/outputs/apk/release/`. You will need to sign this APK with your own key before it can be installed on a device.
