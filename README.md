# Office Productivity App

An Android productivity app for meeting notes, tasks and reminders, and daily/weekly reports.

## Project skeleton

- Kotlin + Jetpack Compose UI
- Material 3 starter dashboard
- Android app module with Java 17, `minSdk 26`, and `compileSdk 35`
- Feature placeholders for tasks/reminders, meeting notes, and reports

## Open and run

1. Install Android Studio with Android SDK 35 and a JDK 17 runtime.
2. Open this repository as a project in Android Studio and let Gradle sync.
3. Run the `app` configuration on an emulator or Android device.

If you want a command-line Gradle wrapper, generate it from the project root using Gradle 8.9: `gradle wrapper --gradle-version 8.9`.

## Next steps

1. Add ViewModels and navigation for the three feature areas.
2. Add Room entities/DAOs for tasks, meetings, and reports.
3. Schedule task reminders with WorkManager and request notification permission where required.
4. Add unit and UI tests before introducing sync or account features.
