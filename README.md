# ViciVirtude - Balance Your Life

ViciVirtude is a local-first Android application designed to help you manage your habits by distinguishing between **Vices** (bad habits to avoid) and **Virtues** (good habits to cultivate).

![ViciVirtude App Icon](app/src/main/res/drawable/ic_launcher_foreground.png)

## Core Concepts
- **Vices**: Habits you want to quit. The streak represents the number of days since your last "failure." (Theming: **Red**)
- **Virtues**: Habits you want to build. The streak represents the consecutive days you have successfully "triumphed." (Theming: **Blue**)

## Key Features
- **Dashboard Hub**: Seamlessly switch between Vices and Virtues with a fluid, high-performance interface.
- **Dynamic Theming**: The app's background and accent colors transition smoothly as you swipe between your progress tabs.
- **Smart Reminders**: Receive contextual notifications ("Fight" vs "Practice") to stay on track.
- **Home Screen Widget**: Track your primary focus directly from your launcher.
- **Rich Commentary**: Log descriptions and reasons for each event to build a meaningful history of your journey.
- **Advanced History**: Consolidated view with grouping, filtering, and detailed statistics.

## Performance Optimized
Built with a focus on buttery-smooth interactions:
- **Deferred State Reading**: UI state is read only during draw/layout phases for 60FPS animations.
- **Background Processing**: Heavy data transformations are offloaded to background threads.
- **Local State Buffering**: Zero input lag during text entry.

## Technical Stack
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **Database**: Room (Local SQLite)
- **Widget Framework**: Jetpack Glance
- **Dependency Injection**: Hilt

## Getting Started
1. Open the project in Android Studio.
2. Sync Gradle files.
3. Run on an emulator or device (Min SDK 26).

## Localization
Supports **English** and **Portuguese (Brasil)**.

---
*Created with focus on fluidity, balance, and mindful habit tracking.*
