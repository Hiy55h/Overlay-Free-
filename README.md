# Overlay Free 0.1.0

Android Studio project for an original free floating sidebar prototype.

## Features
- Draggable floating handle.
- Sidebar listing launchable installed apps.
- Launch installed apps from the sidebar.
- A simple floating notes window.
- No subscription or paid feature gate.

## Android limitation
An ordinary third-party app cannot embed arbitrary installed apps inside its own overlay window. The app list launches apps normally. If the device OS supports freeform/multi-window mode, the system may allow those apps to appear in separate windows. The notes window is an overlay owned by this app.

## Build
Open this folder in Android Studio and let Gradle sync. Or push to GitHub: the included Actions workflow builds a debug APK and uploads it as an artifact.

- minSdk: 26
- targetSdk: 30
- compileSdk: 35
- No native libraries; compatible with armeabi-v7a devices.

## First launch
1. Open Overlay Free.
2. Grant "Display over other apps" permission.
3. Return and tap "サイドバーを開始".
4. Press the floating ☰ handle.
