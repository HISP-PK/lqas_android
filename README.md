# LQAS Android

Android application for DHIS2-based LQAS data validation.

## Download

### Option 1: Clone with Git

```bash
git clone https://github.com/HISP-PK/lqas_android.git
cd lqas_android
```

### Option 2: Download ZIP

1. Open the repository on GitHub.
2. Click `Code`.
3. Click `Download ZIP`.
4. Extract the ZIP to a folder on your computer.

## Requirements

- Android Studio Hedgehog or newer
- Android SDK installed through Android Studio
- JDK 17
- Internet connection to connect to your DHIS2 server
- Android device or emulator running Android 5.0 (API 21) or above

## Open the Project

1. Open Android Studio.
2. Click `Open`.
3. Select the `lqas_android` folder.
4. Wait for Gradle sync to finish.

Android Studio should generate `local.properties` automatically.

## Build the App

### Debug build from Android Studio

1. Open the project.
2. Wait for Gradle sync.
3. Click `Run` to install the debug build on a connected device or emulator.


### Install directly from Android Studio

1. Enable Developer Options on the phone.
2. Enable USB debugging.
3. Connect the phone to the computer.
4. Click `Run` in Android Studio.

## Release Build

This public repository does not include signing keys or release keystore files.

If you need a release APK or AAB:

1. Create your own keystore.
2. Add your own local signing configuration.
3. Build the release variant in your private environment.

## Notes

- The app connects to the DHIS2 server URL entered by the user.
- Use HTTPS for production deployments.
