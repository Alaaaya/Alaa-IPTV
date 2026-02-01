# Alaa IPTV Architecture

## Application Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      User Launch App                         │
└───────────────────┬─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│                    LoginActivity                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  • Server URL Input                                  │   │
│  │  • Username Input                                    │   │
│  │  • Password Input                                    │   │
│  │  • Login Button                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                          │                                   │
│                          ▼                                   │
│              XtreamApiService.authenticate()                 │
│                          │                                   │
│         ┌────────────────┴─────────────────┐               │
│         ▼                                   ▼                │
│    [Success]                          [Failure]              │
│  Save to Prefs                    Show Error Message         │
└────┬────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────┐
│                    MainActivity                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  Tabs: [Live TV] [Movies] [Series] [Favorites]      │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌──────────────────┐  ┌──────────────────────────────┐   │
│  │ Left Panel       │  │ Right Panel (Preview)         │   │
│  │ ┌──────────────┐ │  │ ┌──────────────────────────┐ │   │
│  │ │ Categories   │ │  │ │ Preview Image            │ │   │
│  │ │ (Horizontal) │ │  │ │                          │ │   │
│  │ └──────────────┘ │  │ └──────────────────────────┘ │   │
│  │                  │  │ • Title                       │   │
│  │ ┌──────────────┐ │  │ • Info/Description           │   │
│  │ │ Channels/    │ │  │ • [Play] [Favorite]          │   │
│  │ │ Content List │ │  │                              │   │
│  │ │ (Vertical)   │ │  └──────────────────────────────┘   │
│  │ │              │ │                                      │
│  │ └──────────────┘ │                                      │
│  └──────────────────┘                                      │
│                 │                                            │
│                 ▼ (On Play Click)                           │
└─────────────────────────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────────────────────┐
│                   PlayerActivity                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           ExoPlayer Video View                       │   │
│  │                                                       │   │
│  │  ┌──────────────────┐                               │   │
│  │  │ Channel Name     │                               │   │
│  │  └──────────────────┘                               │   │
│  │                                                       │   │
│  │              [Video Playing]                         │   │
│  │                                                       │   │
│  │  • D-Pad Controls: Play/Pause, Seek                 │   │
│  │  • Back: Exit to Main                               │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow Architecture

```
┌─────────────┐
│     UI      │
│  (Activity) │
└──────┬──────┘
       │
       │ User Actions
       │ (Click, Navigate)
       ▼
┌─────────────────┐
│   Repository    │
│  MediaRepository│
└──────┬──────────┘
       │
       │ API Calls
       ▼
┌─────────────────┐          ┌──────────────┐
│   API Client    │◄────────►│   Network    │
│ XtreamApiService│          │              │
└─────────────────┘          └──────────────┘
       │
       │ Parse Response
       ▼
┌─────────────────┐
│  Data Models    │
│  • Channel      │
│  • Movie        │
│  • Series       │
│  • Category     │
└─────────────────┘
       │
       │ Display Data
       ▼
┌─────────────────┐
│   Adapters      │
│  • Channel      │
│  • Category     │
└─────────────────┘
       │
       │ RecyclerView
       ▼
┌─────────────────┐
│   UI Items      │
│  • Focus Handle │
│  • Click Handle │
└─────────────────┘
```

## Component Interactions

```
┌────────────────────────────────────────────────────────┐
│                     AppPreferences                      │
│  • serverUrl, username, password                        │
│  • isLoggedIn                                           │
│  • favorites (Set<String>)                              │
└────────────────┬───────────────────────────────────────┘
                 │
    ┌────────────┼────────────┐
    │            │             │
    ▼            ▼             ▼
┌────────┐  ┌────────┐   ┌──────────┐
│ Login  │  │  Main  │   │  Player  │
│Activity│  │Activity│   │ Activity │
└────────┘  └───┬────┘   └──────────┘
                │
        ┌───────┴────────┐
        │                │
        ▼                ▼
┌──────────────┐  ┌──────────────┐
│   Channel    │  │   Category   │
│   Adapter    │  │   Adapter    │
└──────────────┘  └──────────────┘
```

## Key Design Patterns

### 1. Repository Pattern
- `MediaRepository` abstracts data sources
- Handles API calls and data transformation
- Provides clean interface to UI layer

### 2. Adapter Pattern
- `ChannelAdapter` and `CategoryAdapter` for RecyclerView
- Custom ViewHolders for item views
- Focus handling in adapters

### 3. Observer Pattern (via Coroutines)
- Lifecycle-aware coroutine scopes
- Asynchronous API calls with `lifecycleScope.launch`
- Result handling with Kotlin Result type

### 4. Singleton Pattern
- `ApiClient` for network client management
- `AppPreferences` wrapper for SharedPreferences

## Navigation Flow

```
Focus Navigation (D-Pad):
┌─────────────────────────────────┐
│  Tabs (Horizontal)               │
│  [Live TV] → [Movies] → [Series] │
└─────────────┬───────────────────┘
              │
              ▼ (Down)
┌─────────────────────────────────┐
│  Categories (Horizontal)         │
│  [All] → [News] → [Sports]       │
└─────────────┬───────────────────┘
              │
              ▼ (Down)
┌─────────────────────────────────┐
│  Content List (Vertical)         │
│  Channel 1                       │
│  Channel 2 ← Focus → Preview     │
│  Channel 3                       │
└─────────────────────────────────┘
```

## Data Models Hierarchy

```
XtreamModels (API Response)
    │
    ├── XtreamAuthResponse
    ├── XtreamCategory
    ├── XtreamStream
    ├── XtreamMovie
    └── XtreamSeries
    
    ▼ Transform
    
MediaModels (App Domain)
    │
    ├── Channel
    ├── Category
    ├── Movie
    ├── Series
    └── Episode
```

## Technology Stack

```
┌──────────────────────────────────┐
│         Presentation Layer        │
│  • Activities (Login, Main)       │
│  • Fragments (if used)            │
│  • Custom Views                   │
│  • ViewBinding                    │
└───────────┬──────────────────────┘
            │
┌───────────▼──────────────────────┐
│         Business Logic Layer      │
│  • Repository                     │
│  • Data Transformations           │
│  • SharedPreferences              │
└───────────┬──────────────────────┘
            │
┌───────────▼──────────────────────┐
│         Data Layer                │
│  • Retrofit (API Calls)           │
│  • OkHttp (HTTP Client)           │
│  • Gson (JSON Parsing)            │
│  • ExoPlayer (Media Playback)     │
└───────────────────────────────────┘
```

## Security Considerations

1. **Credentials Storage**: Stored in SharedPreferences (consider encryption for production)
2. **Network Security**: Uses OkHttp with HTTPS support
3. **Input Validation**: Validates user inputs before API calls
4. **Error Handling**: Graceful error handling prevents crashes
5. **Cleartext Traffic**: Allowed for compatibility (configure as needed)

## Performance Optimizations

1. **Image Loading**: Glide with caching
2. **List Rendering**: RecyclerView with ViewHolder pattern
3. **Async Operations**: Coroutines for non-blocking calls
4. **Player**: ExoPlayer with adaptive streaming
5. **Memory**: Proper lifecycle management and resource cleanup
