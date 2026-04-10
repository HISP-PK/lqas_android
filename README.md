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

#### Activity Screen info-1
LQAS activities are usually conducted on a monthly basis, so you need to select the relevant period. After that, choose the district. A district-level user will only see their own district, while users at higher levels can view multiple districts.

Next, select the dataset from which you want to randomly pick data elements for validation. You also need to set the benchmark and define how many data elements you want to validate.

Once everything is set, tap on Load/Generate. The app will then display the facilities along with randomly generated data elements for validation.

### Activity Screen Picture-2
<img width="975" height="457" alt="image" src="https://github.com/user-attachments/assets/ada27f3a-6c54-4ee6-b0e3-8c5d1ed77e83" />

#### <br>2.2
<img width="975" height="457" alt="image" src="https://github.com/user-attachments/assets/46e80b2b-c22c-465e-b0a5-c818ff9e759d" />

#### <br>2.3
<img width="975" height="547" alt="image" src="https://github.com/user-attachments/assets/79f2153d-6398-4de7-8731-5dd4e0553a50" />

#### <br>2.4
<img width="975" height="548" alt="image" src="https://github.com/user-attachments/assets/650e6eb9-1baa-4969-89be-65ab4e0e3c2b" />

#### <br>2.5
<img width="975" height="546" alt="image" src="https://github.com/user-attachments/assets/df25399f-0a73-4dc4-b404-6884bc667e38" />

### Activity Screen info-2
After clicking Load/Generate, a table will appear showing the list of facilities along with their Union Council or parent information. There is a Denominator checkbox column, which you can use to include or exclude specific facilities. Although all facilities are listed, LQAS may only be conducted for a selected number of Facilities (not all).

The other columns show the selected data elements, where you can mark each entry using the following options:

Y for match,<br>
N for no match,<br>
S for skip,<br>
X for missing (this will be excluded from the total sample size),

Once you have completed the validation, click on the Calculate button to generate the results. After that, you can click Save if you want to store the results. (see pic 2.2, 2.3, 2.4, 2.5)
### Activity Screen Picture-3
<img width="975" height="546" alt="image" src="https://github.com/user-attachments/assets/5789db88-5d1f-49f6-b5f5-a55c3cd3f968" />

### Activity Screen info-3
Loading the Saved Records--> Drop Down menu to list Available saved records.

### Activity Screen Picture-4
<img width="975" height="542" alt="image" src="https://github.com/user-attachments/assets/4234df21-15f1-4ea4-9066-8ba707ff8aad" />
<img width="975" height="539" alt="image" src="https://github.com/user-attachments/assets/5bfa0c9b-9f04-41d2-8f5d-7666a390e7e2" />

### Activity Screen info-4
Click on Export to csv and save as pdf to share the results.
