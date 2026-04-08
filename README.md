# DQ App Android

Public-safe source snapshot for the Android DQ app.

## Included

- Android app source under `app/src`
- Gradle build files
- Gradle wrapper files needed to build the project

## Excluded From This Public Copy

- Signing materials such as keystores and `keystore.properties`
- Local SDK and machine-specific configuration such as `local.properties`
- IDE metadata and Gradle caches
- Build outputs such as APKs, AABs, and generated folders
- Backups, memory dumps, and Play Console assets

## Build Notes

1. Open the project in Android Studio.
2. Let Android Studio recreate `local.properties`.
3. Build the debug variant first.
4. Add your own signing configuration if you need a release build.

## Publishing Notes

Before pushing changes, review:

- No secrets in source or config files
- No generated output under `build/` or `app/build/`
- No signing keys or passwords

