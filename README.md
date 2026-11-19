# DRISHTI

DRISHTI is an Android accessibility assistant built by VIT Bhopal Group 36. It combines camera based scene descriptions, spoken questions, and text reading with speech output.

## Features

- **Live navigation:** uses the rear camera to describe the current scene and highlight visible hazards.
- **Ask DRISHTI:** turns a spoken question into a concise spoken answer.
- **Read text:** points the camera at a page, label, or sign and reads visible text aloud.
- **Accessible interface:** large touch targets, clear labels, screen reader descriptions, and speech output.

DRISHTI is an assistive companion. It does not replace a cane, guide dog, traffic signals, or other mobility aids.

## Requirements

- Android Studio with Android SDK platform 35 or newer and a Java 17 JDK.
- An Android device with a camera and microphone for camera and voice features.
- A Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey).

## Configure Gemini

Add the following line to the project's ignored `local.properties` file. Keep the key private and do not commit it.

```properties
GEMINI_API_KEY=your_api_key_here
```

The Gradle build also accepts `GEMINI_API_KEY` as an environment variable or Gradle property. If it is missing, the app builds but explains that Gemini needs configuration when an AI feature is opened.

The Gemini Developer API key is used by the Android app. For a public production release, route AI requests through a backend you control and apply suitable API restrictions and quotas.

## Build and run

1. Open this project in Android Studio and let Gradle sync.
2. Configure the Gemini key as shown above.
3. Connect an Android device or start an emulator.
4. Run the `app` configuration and grant camera or microphone access when prompted.

The app supports Android 11 (API 30) through Android 15 (API 35). Its release build deliberately excludes the Gemini key: a public release should call Gemini through a backend you control, so the key cannot be extracted from the APK. The debug build can use the local key for development testing.

The application uses Kotlin, Jetpack Compose, CameraX, OkHttp, and the Gemini `gemini-3.5-flash-lite` model.

## Repository

The refreshed project is maintained at [EPICS24-603/DRISHTI](https://github.com/EPICS24-603/DRISHTI). The original project is at [Sumit-5002/DRISHTI](https://github.com/Sumit-5002/DRISHTI).
