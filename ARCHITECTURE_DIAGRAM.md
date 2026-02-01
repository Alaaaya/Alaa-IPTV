# Phase 1 - Architecture Diagram

## Project Structure

```
com.alaa.iptv/
│
├── domain/                          # Domain Layer (Business Logic Contracts)
│   └── repository/
│       └── IMediaRepository.kt      # Repository interface defining data operations
│
├── data/                            # Data Layer (Data Sources & Implementation)
│   ├── local/                       # Room Database Components
│   │   ├── AppDatabase.kt           # Room database singleton
│   │   ├── entity/                  # Database entities
│   │   │   └── RoomEntities.kt      # Channel, Movie, Series, Episode, Favorite, Recent
│   │   ├── dao/                     # Data Access Objects
│   │   │   ├── ChannelDao.kt        # Channel CRUD operations
│   │   │   ├── CategoryDao.kt       # Category CRUD operations
│   │   │   ├── MovieDao.kt          # Movie CRUD operations
│   │   │   ├── SeriesDao.kt         # Series & Episode CRUD operations
│   │   │   ├── FavoriteDao.kt       # Favorites management
│   │   │   └── RecentDao.kt         # Recently viewed tracking
│   │   └── mapper/                  # Entity-Model converters
│   │       └── EntityMappers.kt     # Conversion functions
│   │
│   ├── api/                         # Network Data Source
│   │   ├── XtreamApiService.kt      # Xtream Codes API endpoints
│   │   └── ApiClient.kt             # Retrofit configuration
│   │
│   ├── models/                      # Data Models
│   │   ├── XtreamModels.kt          # API response models
│   │   └── MediaModels.kt           # Domain models (Channel, Movie, Series, etc.)
│   │
│   ├── preferences/                 # Local Preferences
│   │   └── AppPreferences.kt        # SharedPreferences wrapper
│   │
│   └── repository/                  # Repository Implementation
│       └── MediaRepository.kt       # Implements IMediaRepository
│
├── ui/                              # Presentation Layer (Activities & Adapters)
│   ├── login/
│   │   └── LoginActivity.kt         # Login screen
│   ├── main/
│   │   ├── MainActivity.kt          # Main content browser
│   │   ├── ChannelAdapter.kt        # Channel list adapter
│   │   └── CategoryAdapter.kt       # Category list adapter
│   └── player/
│       └── PlayerActivity.kt        # Video player screen
│
├── utils/                           # Utilities
│   └── M3UParser.kt                 # M3U playlist parser
│
└── core/                            # Core/Common (Reserved for future use)
```

## Data Flow

### 1. Read Operation (e.g., Get Live Channels)

```
MainActivity
    ↓ calls
IMediaRepository.getLiveStreams()
    ↓ implements
MediaRepository
    ↓ checks
Room Database (cache)
    ├─ Cache HIT → Return cached data
    └─ Cache MISS → Fetch from API
           ↓
    XtreamApiService
           ↓ network call
    Xtream Codes Server
           ↓ returns
    API Response
           ↓ transforms
    MediaRepository
           ↓ saves to cache
    Room Database
           ↓ returns
    Channel List → MainActivity
```

### 2. Write Operation (e.g., Add Favorite)

```
MainActivity (user taps favorite)
    ↓ calls
IMediaRepository.addFavorite(itemId)
    ↓ implements
MediaRepository
    ↓ determines type
Check item in Channel/Movie/Series tables
    ↓ creates
FavoriteEntity(itemId, itemType)
    ↓ inserts
FavoriteDao.insertFavorite()
    ↓ saves to
Room Database
    ↓ updates
UI shows favorite indicator
```

## Layer Responsibilities

### Domain Layer
- Defines contracts (interfaces)
- Business logic types
- No dependencies on other layers
- Can be tested independently

### Data Layer
- Implements domain contracts
- Manages data sources (API, Database, Preferences)
- Handles caching strategy
- Transforms data between layers
- Error handling

### UI Layer  
- Displays data to user
- Handles user interactions
- Depends on domain contracts only
- Updates based on data changes

## Key Design Patterns

### 1. Repository Pattern
```
IMediaRepository (interface)
        ↑ depends on
MediaRepository (implementation)
        ↓ uses
[XtreamApiService] + [Room Database]
```

Benefits:
- Single source of truth
- Abstraction of data sources
- Easy to test with mocks
- Flexible data source switching

### 2. Singleton Pattern
```
AppDatabase.getInstance(context)
```

Benefits:
- Single database instance
- Thread-safe access
- Memory efficient

### 3. DAO Pattern
```
ChannelDao → Room generates implementation
```

Benefits:
- Type-safe database queries
- Compile-time verification
- Automatic threading

### 4. Mapper Pattern
```
ChannelEntity ↔ Channel
MovieEntity ↔ Movie
```

Benefits:
- Separation of database and domain models
- Flexibility to change schemas
- Clear transformation logic

## Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Domain | Kotlin Interfaces | Define contracts |
| Data - Local | Room 2.6.1 | Local database |
| Data - Network | Retrofit 2.9.0 | API calls |
| Data - Network | OkHttp 4.12.0 | HTTP client |
| Data - Parsing | Gson 2.10.1 | JSON parsing |
| UI | Activities/Adapters | User interface |
| UI | ViewBinding | View access |
| UI | ExoPlayer (Media3) 1.3.1 | Video playback |
| Async | Coroutines 1.7.3 | Asynchronous operations |

## Benefits of This Architecture

1. **Testability**: Each layer can be tested independently
2. **Maintainability**: Clear responsibilities and boundaries
3. **Scalability**: Easy to add new features
4. **Flexibility**: Can swap implementations without affecting other layers
5. **Offline Support**: Built-in caching through Room
6. **Performance**: Reduced API calls, faster data access
7. **Type Safety**: Compile-time verification throughout
8. **Clean Code**: Following SOLID principles

## Future Extensions

This architecture supports easy addition of:
- M3U parser as another data source
- EPG data provider
- Content recommendation engine
- Multiple user profiles
- Advanced search functionality
- Download manager
- Parental controls

All can be added without modifying existing code structure.
