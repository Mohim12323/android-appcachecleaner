# Android Cache Cleaner

## Description

Since Android 6 (Marshmallow) is not possible anymore to clear cache of all apps at same time and Google has moved this permission to system apps only. There is only one possible way left - open manually info about application and find specific "Storage" menu and then press "Clean cache" button.

**AndroidCacheCleaner** can request all installed user and system apps and it replaces all manual actions related to clean cache using Accessibility service.

**AndroidCacheCleaner** saves the last checked apps to show them next time only.

## screenshots

|Android 11 and early|Android 12 and later|
|-----------------|-------------------|
|accessibility button in navbar|floating accessibility button|
|![Android 11 and early](fastlane/metadata/android/en-US/images/phoneScreenshots/android_11.png?raw=true "Android 11 and early")|![Android 12 and later](fastlane/metadata/android/en-US/images/phoneScreenshots/android_12.png?raw=true "Android 12 and later")|

## How to use

1. Run app
2. Open **Accessibility Settings**
3. Enable Accessibility support for **AppCacheCleaner**
4. Return back to AppCacheCleaner app
5. Click **User Apps Cache**, **System Apps Cache** OR **All Apps Cache**
6. Check required apps (use floating button to check/uncheck all apps)
7. Press **Clean Cache** floating button
8. Press **Accessibility button** to interrupt process OR wait until process will be finished
9. Press **Close App** OR clean app from task manager to disable Accessibility

## Icon copyright

Icon made by [Smashicons](https://www.flaticon.com/authors/smashicons) from [Flaticon](https://www.flaticon.com/)
