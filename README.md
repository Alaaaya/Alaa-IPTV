# Alaa IPTV - Android TV Application

A fully functional IPTV application for Android TV with a modern dark glassmorphism design, blue accent theme, and iBO Player-like experience.

## Features

### Core Features
- **Live TV Streaming**: Stream live channels using Xtream Codes API or M3U playlists
- **Movies (VOD)**: Browse and watch movies from your IPTV provider
- **Series**: Access TV series with episodes organized by season
- **Favorites**: Mark and quickly access your favorite content with database persistence
- **Recents**: Track and revisit recently viewed content
- **Full Player**: ExoPlayer (Media3) based video player with playback controls
- **Channel Reordering**: Long-press on OK button to reorder channels

### User Interface
- **Dark Glassmorphism Design**: Modern translucent UI with depth effects
- **Blue Accent Theme**: Elegant blue color scheme throughout the app (`#2196F3`)
- **D-Pad Navigation**: Fully optimized for Android TV remote control (no touch/mouse)
- **Focus Handling**: Smooth custom focus management with scale animations
- **Preview Panel**: Large preview (2/3 screen) with content metadata
- **Category Navigation**: Horizontal scrolling categories for easy filtering
- **5 Tab Layout**: Live TV, Movies, Series, Favorites, and Recents

### Technical Features
- **Xtream Codes API**: Full integration with Xtream Codes providers
- **M3U Support**: Parse and play M3U playlists
- **ExoPlayer (Media3)**: Industry-standard video player with adaptive streaming
- **Room Database**: Local caching with favorites and recents persistence
- **Clean Architecture**: Separated data/domain/ui layers
- **RecyclerView**: Efficient list rendering with custom TV-optimized adapters
- **ViewBinding**: Type-safe view access
- **Coroutines**: Asynchronous operations for smooth UI
- **Retrofit**: RESTful API communication
- **Glide**: Efficient image loading and caching

## Android TV UI/UX

This app features a **complete Android TV interface** with iBO Player-like experience:

### Screen Layout
```
┌──────────────────────────────────────────────────────────────┐
│ [Live TV] [Movies] [Series] [Favorites] [Recents]            │
├────────────────────┬─────────────────────────────────────────┤
│ [All][News][Sports]│           PREVIEW PANEL                 │
│                    │                                          │
│ ► BBC News   ❤    │        [Large Poster/Icon]               │
│   CNN             │                                          │
│   Fox News        │    BBC News - Channel 102                │
│   Sky News        │                                          │
│   MSNBC           │    [Play]  [❤]                          │
└────────────────────┴─────────────────────────────────────────┘
```

### Key Features
- **1/3 + 2/3 Split Layout**: Content list on left, large preview on right
- **Smooth Focus Animations**: 1.05x scale with 200ms transitions
- **Long-Press Reordering**: Hold OK for 1 second to enable reorder mode
- **Database-Backed State**: Favorites and recents persist across sessions
- **Auto-Preview Updates**: Preview changes automatically with focus
- **Custom Focus Management**: No Leanback library, fully custom implementation

📖 **Full Documentation**: See [ANDROID_TV_UI.md](ANDROID_TV_UI.md) for complete Android TV UI/UX details.

## Architecture

### Project Structure
```
app/src/main/java/com/alaa/iptv/
├── data/
│   ├── api/              # API services and clients
│   ├── models/           # Data models
│   ├── preferences/      # SharedPreferences wrapper
│   └── repository/       # Data repositories
├── ui/
│   ├── login/           # Login screen
│   ├── main/            # Main activity with channel list
│   └── player/          # Video player activity
└── utils/               # Utility classes (M3U parser, etc.)
```

### Key Components

#### Login Activity
- Server URL, username, and password authentication
- Xtream Codes API authentication
- Credentials persistence
- Error handling and user feedback

#### Main Activity
- Tab-based navigation (Live TV, Movies, Series, Favorites)
- Category filtering with horizontal RecyclerView
- Channel/content list with vertical RecyclerView
- Live preview panel with metadata display
- Favorite management
- Focus-based navigation optimized for remote control

#### Player Activity
- ExoPlayer integration
- Custom player controls
- D-Pad navigation support
- Buffering indicators
- Error handling
- Play/pause, seek controls

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Android TV device or emulator for testing

### Installation

1. Clone the repository:
```bash
git clone https://github.com/Alaaaya/Alaa-IPTV.git
cd Alaa-IPTV
```

2. Open the project in Android Studio

3. Wait for Gradle sync to complete

4. Build and run on Android TV device or emulator

### Configuration

The app requires an Xtream Codes compatible IPTV provider with:
- Server URL
- Username
- Password

Alternatively, you can use M3U playlist URLs.

## Usage

### First Launch
1. Enter your IPTV provider details (server URL, username, password)
2. Click "Login" to authenticate
3. Wait for content to load

### Navigation
- **D-Pad Up/Down**: Navigate through channels/content
- **D-Pad Left/Right**: Switch between tabs and categories
- **OK/Select**: Select channel/content to view preview or play
- **Long Press OK**: Enable reordering mode (future enhancement)
- **Back**: Return to previous screen

### Playing Content
1. Navigate to desired channel or content
2. Press OK to view preview
3. Press "Play" button or OK again to start playback
4. Use D-Pad in player to control playback:
   - **OK/Center**: Play/Pause
   - **Left/Right**: Seek backward/forward
   - **Back**: Exit player

## Features Details

### Glassmorphism Design
The app features a modern glassmorphism design with:
- Translucent backgrounds with blur effect
- Border highlights for depth
- Smooth focus animations
- Blue accent colors
- Dark theme optimized for TV viewing

### Focus Management
- Custom focus handling for TV remote navigation
- Visual feedback on focus (scale animation, color changes)
- Automatic preview updates on focus change
- Seamless tab and category navigation

### Favorites System
- Toggle favorites with heart button
- Persistent storage using SharedPreferences
- Quick access via Favorites tab
- Visual indicators on favorited items

## Dependencies

- **Kotlin**: 1.9.20
- **AndroidX Core**: 1.12.0
- **ExoPlayer (Media3)**: 1.2.0
- **Retrofit**: 2.9.0
- **OkHttp**: 4.12.0
- **Glide**: 4.16.0
- **Coroutines**: 1.7.3
- **Gson**: 2.10.1

## Build Configuration

- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34
- **Java Version**: 17

## License

This project is available for personal and educational use.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## Troubleshooting

### Login Issues
- Verify server URL is correct (include http:// or https://)
- Check username and password
- Ensure stable internet connection
- Verify Xtream Codes API compatibility

### Playback Issues
- Check stream URL validity
- Ensure sufficient network bandwidth
- Try different stream qualities if available
- Verify device codec support

### Navigation Issues
- Ensure using TV remote or D-Pad
- Check focus is visible on current item
- Restart app if navigation becomes unresponsive

## Future Enhancements

- EPG (Electronic Program Guide) integration
- Channel reordering with drag-and-drop
- Parental controls
- Multi-profile support
- Picture-in-Picture mode
- Recommendations based on viewing history
- Search functionality
- Settings screen for customization