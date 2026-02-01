# Alaa IPTV - Android TV IPTV Player

A modern Android TV application for streaming IPTV content with a beautiful glassmorphism design.

## Features

- 📺 **Live TV Streaming** - Watch live TV channels with HLS/M3U8 support
- 🎬 **Movies & Series** - Access your VOD content library
- 💾 **Local Database** - Store channels and user data using Room
- 🔐 **User Authentication** - Secure login with server credentials
- 🎨 **Glassmorphism Design** - Modern UI optimized for Android TV
- 📊 **Playback History** - Track your viewing history
- ⭐ **Favorites** - Mark and organize your favorite channels
- 🎮 **D-pad Navigation** - Full TV remote control support

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern:

```
app/src/main/java/com/alaaaya/iptv/
├── MainActivity.kt
├── data/
│   ├── local/          # Room database (DAOs, Database)
│   ├── models/         # Data models (User, Channel, PlaybackHistory)
│   ├── remote/         # API models and Retrofit service
│   └── repository/     # Repository pattern implementations
├── domain/             # Business logic (if needed)
├── ui/
│   ├── login/          # Login screen (Fragment + ViewModel)
│   ├── main/           # Main screen with channel grid
│   └── player/         # Video player with ExoPlayer
└── utils/              # Utility classes and extensions
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Android SDK with Leanback for TV
- **Video Player**: Media3 ExoPlayer
- **Database**: Room Persistence Library
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Glide
- **Coroutines**: Kotlin Coroutines for async operations
- **Architecture**: MVVM with LiveData

## Setup Instructions

### Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK API 21+ (minimum)
- Android SDK API 34 (target)
- Gradle 8.2+

### Build the Project

1. Clone the repository:
```bash
git clone https://github.com/Alaaaya/Alaa-IPTV.git
cd Alaa-IPTV
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run the app on an Android TV emulator or device

### Configuration

The app uses standard IPTV API endpoints. To configure:

1. Launch the app
2. Enter your credentials on the login screen:
   - Username
   - Password
   - Server URL (e.g., `http://example.com:8080/`)

## API Integration

The app is compatible with standard IPTV panel APIs (Xtream Codes format):

- `GET player_api.php?username=X&password=Y` - Authentication
- `GET player_api.php?username=X&password=Y&action=get_live_streams` - Live channels
- `GET player_api.php?username=X&password=Y&action=get_vod_streams` - Movies
- `GET player_api.php?username=X&password=Y&action=get_series` - TV Series

## Screens

### Login Screen
- User authentication with server URL
- Glassmorphism design with blur effects
- Input validation

### Main Screen
- Grid view of channels/content
- Category filtering (Live TV, Movies, Series)
- Channel search functionality
- Favorite channels section

### Player Screen
- Full-screen video playback
- ExoPlayer with HLS support
- Playback controls overlay
- Channel information display

## Database Schema

### User Table
- ID, Username, Password, Server URL, Token, Expiry Date

### Channel Table
- ID, Name, Stream URL, Logo, Category, EPG Channel ID, Favorite status

### Playback History Table
- ID, Channel ID, Last Played timestamp, Playback position

## Dependencies

Key dependencies used in this project:

```kotlin
// AndroidX & UI
androidx.leanback:leanback:1.0.0
androidx.constraintlayout:constraintlayout:2.1.4

// Room Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Retrofit & Networking
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.okhttp3:okhttp:4.12.0

// ExoPlayer
androidx.media3:media3-exoplayer:1.2.0
androidx.media3:media3-ui:1.2.0

// Image Loading
com.github.bumptech.glide:glide:4.16.0
```

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and feature requests, please use the GitHub Issues page.