# LQAS Android

Android application for DHIS2-based LQAS data validation.
LQAS Insight is a data validation app designed to verify whether the data entered into the system matches the records available in registers. It connects with DHIS2 using a URL, username, and password, and generates validation reports based on the selected criteria.

The app allows users to generate a random sample of data elements from a specific form or dataset for a defined reporting period and selected org units. At the district level, where multiple facilities may exist, validators can choose which facilities to include. Only the selected facilities are counted as the denominator, while others can be ignored. The system then calculates an overall average for the district based on the selected facilities.

During validation, users review randomly selected data entries (for example ANC1) and mark them accordingly:

Y for match,
N for no match,
S for skip,
X for missing (excluded from the total sample size).

Additionally, users can create a more focused dataset if they want a targeted or biased validation. This custom dataset can then be used within the app for validation purposes.

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

It does not include signing keys or release keystore files.

If you need a release APK or AAB:


1. Create your own keystore.
2. Add your own local signing configuration.
3. Build the release variant in your private environment.

## Notes

- The app connects to the DHIS2 server URL entered by the user.
- Use HTTPS for production deployments.

## Using the App

Available on PlayStore <a>https://play.google.com/store/apps/details?id=org.dhis2.dqapp&pcampaignid=web_share</a>

## Instructions to Use
### Login Screen Picture
<img width="975" height="547" alt="image" src="https://github.com/user-attachments/assets/29648012-bde6-44b5-897f-2d69b7227a10" />


### Login Screen info
After downloading and installing the app, the first screen asks for the DHIS2 login details. You need to enter the server URL (for example, https://province1.dhis2.pk), along with your username and password.

You also need to specify the organisational unit levels based on your DHIS2 setup. For example, you might enter level 3 for District and level 6 for Health Facility, depending on your specific DHIS2 implementation. If you are unsure about these levels, you should consult your DHIS2 implementation team for the correct configuration.

For LQAS create and assigne district-wise access users, so they can only see and validate data for their own district. This helps keep the work organized and avoids overlap.
### Activity Screen Picture-1
<img width="975" height="550" alt="image" src="https://github.com/user-attachments/assets/ea7f14b4-21f9-4150-915d-edb768f2ae1f" />
### Activity Screen info-1
LQAS activities are usually conducted on a monthly basis, so you need to select the relevant period. After that, choose the district. A district-level user will only see their own district, while users at higher levels can view multiple districts.

Next, select the dataset from which you want to randomly pick data elements for validation. You also need to set the benchmark and define how many data elements you want to validate.

Once everything is set, tap on Load/Generate. The app will then display the selected facilities along with randomly generated data elements for validation.

### Activity Screen Picture-2
### Activity Screen info-2

### Activity Screen Picture-3
### Activity Screen info-3

### Activity Screen Picture-4
### Activity Screen info-4

### Activity Screen Picture-5
### Activity Screen info-5
