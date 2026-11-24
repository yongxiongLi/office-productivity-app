# Office Productivity App

An Android productivity app for meeting notes, tasks and reminders, and daily/weekly reports.

## Project skeleton

- Kotlin + Jetpack Compose and Material 3
- Task screen with local Room persistence
- WorkManager-based task deadline notifications
- Android app module with Java 17, `minSdk 26`, and `compileSdk 35`

## Open and run

1. Install Android Studio with Android SDK 35 and a JDK 17 runtime.
2. Open this repository as a project in Android Studio and let Gradle sync.
3. Run the `app` configuration on an emulator or Android device.

If you want a command-line Gradle wrapper, generate it from the project root using Gradle 8.9: `gradle wrapper --gradle-version 8.9`.

## Tasks and reminders

Tasks are stored locally in Room. Create a task and optionally choose a date and time to schedule a notification. You can mark a task complete or delete it; either action cancels its pending reminder. Android 13 and later require notification permission. WorkManager schedules reminders as deferrable background work, so delivery is not guaranteed at the exact minute.

## Next steps

1. Add edit-task support and tests for persistence and scheduling.
2. Add meeting notes and reports, then link meeting action items to tasks.
3. Consider backup or account sync after the local-first workflow is stable.
