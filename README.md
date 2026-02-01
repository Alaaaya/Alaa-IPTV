# Alaa IPTV - Android TV IPTV Player

A complete Android TV IPTV Player application built with Kotlin, featuring a modern glassmorphism UI design and support for Xtream Codes API.

## Features

### Core Functionality
- **Login System**: Secure authentication with Xtream Codes servers
- **Live TV**: Browse and watch live TV channels
- **Movies (VOD)**: Access on-demand movie content
- **Series**: Watch TV series with episode management
- **Favorites**: Mark and manage favorite content
- **Watch History**: Track recently watched content
- **Search**: Find channels, movies, and series quickly

### Technical Features
- **MVVM Architecture**: Clean separation of concerns with ViewModel pattern
- **Room Database**: Local caching of channels, movies, series, and user data
- **Xtream Codes API**: Full integration with Xtream Codes IPTV provider API
- **ExoPlayer**: Hardware-accelerated video playback
- **Focus Navigation**: Optimized for TV remote control (D-Pad navigation)
- **Glassmorphism UI**: Modern dark blue theme with glassmorphism effects
- **Coroutines**: Asynchronous operations for smooth UI experience

## Project Structure

```
Alaa-IPTV/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/alaaaya/iptv/
│   │   │   │   ├── ui/
│   │   │   │   │   ├── login/LoginActivity.kt
│   │   │   │   │   ├── main/
│   │   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   │   ├── MainViewModel.kt
│   │   │   │   │   │   ├── CategoryAdapter.kt
│   │   │   │   │   │   └── ChannelAdapter.kt
│   │   │   │   │   └── player/PlayerActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── XtreamCodesApi.kt
│   │   │   │   │   │   └── ApiModels.kt
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   └── [DAO classes]
│   │   │   │   │   ├── models/
│   │   │   │   │   │   ├── Channel.kt
│   │   │   │   │   │   ├── Movie.kt
│   │   │   │   │   │   ├── Series.kt
│   │   │   │   │   │   └── [Other models]
│   │   │   │   │   └── IptvRepository.kt
│   │   │   │   └── utils/
│   │   │   │       ├── Constants.kt
│   │   │   │       └── Extensions.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_login.xml
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_player.xml
│   │   │   │   │   ├── item_channel.xml
│   │   │   │   │   └── item_category.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── drawable/
│   │   │   │       └── [Background and selector resources]
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Technical Stack

- **Language**: Kotlin
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM + Clean Architecture

### Libraries Used

- **AndroidX Core & AppCompat**: Modern Android development
- **Leanback & TVProvider**: Android TV specific components
- **Lifecycle Components**: ViewModel and LiveData
- **Room**: Local database with SQLite
- **Retrofit**: HTTP client for API calls
- **Gson**: JSON serialization/deserialization
- **OkHttp**: HTTP client and logging interceptor
- **Coroutines**: Asynchronous programming
- **Media3 (ExoPlayer)**: Video playback
- **Glide**: Image loading and caching
- **Material Components**: UI components

## Building the Project

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 8 or newer
- Android SDK with API level 34
- Internet connection for downloading dependencies

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/Alaaaya/Alaa-IPTV.git
   cd Alaa-IPTV
   ```

2. Open the project in Android Studio

3. Let Gradle sync and download dependencies

4. Connect an Android TV device or use an emulator with TV profile

5. Run the app:
   ```bash
   ./gradlew installDebug
   ```

## Disclaimer

This is an IPTV player application. Users are responsible for the content they access through their IPTV service providers. This application does not provide any media content.