# ViceVirtue - Habit & Vice Tracker

ViceVirtue is a local-first Android application designed to help users manage their habits by distinguishing between Vices (bad habits to avoid) and Virtues (good habits to cultivate).

## Core Concepts
- **Vices**: Habits you want to quit. The streak represents the number of days since your last "failure."
- **Virtues**: Habits you want to build. The streak represents the consecutive days you have successfully "triumphed."

## Key Features
- **Dashboard**: Overview of all tracked items with current streaks and quick logging.
- **Commentary System**: Optional detailed logging. Add descriptions or reasons for each entry to contextualize your progress. Can be toggled on/off in settings.
- **Detail View**: Comprehensive history for specific habits, including editing/deleting entries and updating descriptions.
- **Global History**: Consolidated view of all activities with date grouping and filtering.
- **Theme Customization**: Support for Light, Dark, and System themes, configurable directly in the settings.
- **Home Screen Widget**: Track your favorite habit directly from your home screen with quick-log buttons.
- **Smart Reminders**: Schedule custom notifications to help you stay on track. Vices remind you to fight ("Fight against..."), while Virtues encourage practice ("Practice...").
- **Multi-language Support**: Full support for English and Portuguese (Brasil).
- **Responsive UI**: Built with Jetpack Compose using a custom "Vice/Virtue" theme (Red for Vices, Blue for Virtues).

## Technical Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (Local SQLite)
- **Dependency Injection**: Hilt
- **Widget Framework**: Jetpack Glance
- **State Management**: DataStore for widget bindings

## Getting Started
1. Open the project in Android Studio.
2. Sync the project with Gradle files.
3. Run the app on an emulator or physical device (Min SDK 26).

## Localization
All strings are externalized in `res/values/strings.xml` and `res/values-pt/strings.xml`.

## License
Private/Internal Project
