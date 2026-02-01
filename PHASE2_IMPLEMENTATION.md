# Phase 2: EPG, M3U, Search & Sync Pipeline Implementation

## Summary

Phase 2 builds upon the Phase 1 foundation to add EPG (Electronic Program Guide) support, M3U playlist parsing, comprehensive search functionality, and a robust data synchronization pipeline.

## Changes Made

### 1. EPG Integration

#### Data Models
- **EpgProgram** - Domain model for EPG data with utility methods
  - `isLive()` - Check if program is currently airing
  - `isUpcoming()` - Check if program will air in the future
  - `isPast()` - Check if program has ended
  - `getDuration()` - Get program duration

#### Room Database
- **EpgProgramEntity** - Room entity for EPG programs with optimized indices
  - Indexed by `channelId`, `startTime`, and `endTime` for fast queries
  - Stores program metadata (title, description, category, icon)
  - Tracks last update timestamp

#### DAO Operations
- **EpgDao** - Comprehensive EPG data access
  - `getProgramsByChannel()` - Get all EPG for a channel
  - `getProgramsByChannelAndTimeRange()` - Get EPG within time window
  - `getCurrentProgram()` - Get currently airing program
  - `getUpcomingPrograms()` - Get next N upcoming programs
  - `getProgramsByTimeRange()` - Get EPG across all channels
  - `deleteOldPrograms()` - Cleanup expired EPG data
  - Full CRUD operations with conflict resolution

#### Repository Integration
- `getEpgForChannel()` - Retrieve all EPG for a channel from cache
- `getEpgForChannelInTimeRange()` - Get EPG within specific time window
- `getCurrentProgram()` - Get currently playing program
- `getUpcomingPrograms()` - Get upcoming programs with limit
- `cacheEpgPrograms()` - Store EPG data locally
- `cleanupOldEpgData()` - Remove expired EPG entries

### 2. M3U Playlist Support

#### Enhanced M3U Parser
The existing M3UParser utility already supports:
- Parsing `#EXTINF` directives
- Extracting tvg-id, tvg-name, tvg-logo attributes
- Extracting group-title (category)
- Converting to Channel models with direct source URLs

#### Repository Integration
- **loadM3UPlaylist()** - Parse M3U content from string
  - Supports standard M3U format
  - Extracts channel metadata
  - Returns list of Channel objects

- **loadM3UPlaylistFromUrl()** - Fetch and parse M3U from URL
  - Downloads M3U file via HTTP
  - Parses content automatically
  - Handles network errors gracefully

- **mergeM3UChannels()** - Integrate M3U channels into database
  - Inserts/updates channels in Room database
  - Extracts and caches unique categories
  - Preserves existing favorites and recents
  - Supports mixing Xtream and M3U sources

### 3. Search Functionality

#### DAO Enhancements

**ChannelDao**
- `searchChannelsByName()` - Search all channels by name
- `searchChannelsByNameInCategory()` - Search within specific category

**MovieDao**
- `searchMoviesByTitle()` - Search movies by title
- `searchMoviesByTitleInCategory()` - Search within specific category
- `searchMoviesByGenre()` - Filter by genre
- `searchMoviesByYear()` - Filter by release year

**SeriesDao**
- `searchSeriesByTitle()` - Search series by title
- `searchSeriesByTitleInCategory()` - Search within specific category
- `searchSeriesByGenre()` - Filter by genre

#### Repository Search Methods
- `searchChannels(query, categoryId?)` - Search live TV channels
- `searchMovies(query, categoryId?)` - Search movies
- `searchSeries(query, categoryId?)` - Search TV series
- `searchMoviesByGenre(genre)` - Find movies by genre
- `searchSeriesByGenre(genre)` - Find series by genre

All search operations:
- Use local cache (Room database)
- Support case-insensitive matching via LIKE queries
- Preserve favorite status
- Return results in appropriate sort order
- Handle errors gracefully with logging

### 4. Sync Pipeline

#### Sync Operations

**syncAllData()**
- Orchestrates full data synchronization
- Syncs Live TV, Movies, and Series sequentially
- Cleans up old EPG data (>24 hours old)
- Continues on partial failures
- Returns combined result status

**syncLiveTV()**
- Fetches and caches live TV categories
- Fetches and caches all live channels
- Updates local database with latest data
- Preserves user favorites and recents

**syncMovies()**
- Fetches and caches movie categories
- Fetches and caches all movies
- Updates local database with latest data
- Preserves user favorites and recents

**syncSeries()**
- Fetches and caches series categories
- Fetches and caches all TV series
- Updates local database with latest data
- Preserves user favorites and recents

#### Sync Features
- **Incremental Updates** - Uses `OnConflictStrategy.REPLACE` to update existing records
- **Error Handling** - Each sync operation returns Result type with success/failure
- **Logging** - Comprehensive logging for debugging and monitoring
- **Data Consistency** - Favorites and recents remain intact during sync
- **Automatic Cleanup** - Removes stale EPG data automatically

### 5. Database Migration

#### Version Update
- Database version incremented from 1 to 2
- Added `EpgProgramEntity` to entity list
- Added `epgDao()` abstract method
- Uses `fallbackToDestructiveMigration()` for development

**Note:** For production, implement proper migrations to preserve user data:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS epg_programs (
                id TEXT PRIMARY KEY NOT NULL,
                channelId TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                startTime INTEGER NOT NULL,
                endTime INTEGER NOT NULL,
                category TEXT,
                icon TEXT,
                lastUpdated INTEGER NOT NULL
            )
        """)
        database.execSQL("CREATE INDEX index_epg_programs_channelId ON epg_programs(channelId)")
        database.execSQL("CREATE INDEX index_epg_programs_startTime ON epg_programs(startTime)")
        database.execSQL("CREATE INDEX index_epg_programs_endTime ON epg_programs(endTime)")
    }
}
```

### 6. Repository Interface Updates

Updated `IMediaRepository` interface to include Phase 2 methods:
- EPG operations (6 methods)
- M3U support (3 methods)
- Search operations (5 methods)
- Sync pipeline (4 methods)

All methods follow existing patterns:
- Suspend functions for coroutine support
- Result types for error handling
- Nullable parameters for flexibility
- Consistent naming conventions

## Architecture Benefits

### EPG Integration
- **Offline Access** - EPG data cached locally for offline viewing
- **Fast Queries** - Indexed database for instant lookups
- **Time-Based Filtering** - Efficient queries by time range
- **Automatic Cleanup** - Removes old data to save space

### M3U Support
- **Format Flexibility** - Support both Xtream and M3U sources
- **Easy Migration** - Import existing M3U playlists
- **Source Mixing** - Combine multiple sources in one app
- **Standard Compliance** - Follows M3U format specifications

### Search
- **Local Performance** - All searches use cached data
- **Multiple Criteria** - Search by title, genre, category, year
- **Case Insensitive** - User-friendly search experience
- **Sorted Results** - Consistent, predictable ordering

### Sync Pipeline
- **Data Freshness** - Keep local cache up to date
- **Fault Tolerance** - Continue on partial failures
- **Incremental Updates** - Only update changed data
- **User Data Safety** - Preserves favorites and viewing history

## Usage Examples

### EPG Usage
```kotlin
// Get EPG for a channel
val epgList = repository.getEpgForChannel("channel123")

// Get current program
val currentProgram = repository.getCurrentProgram("channel123")

// Get upcoming programs
val upcoming = repository.getUpcomingPrograms("channel123", limit = 5)

// Get EPG for time range (next 3 hours)
val now = System.currentTimeMillis()
val threeHoursLater = now + (3 * 60 * 60 * 1000)
val programs = repository.getEpgForChannelInTimeRange("channel123", now, threeHoursLater)
```

### M3U Usage
```kotlin
// Load from string
val result = repository.loadM3UPlaylist(m3uContent)
result.onSuccess { channels ->
    // Merge into database
    repository.mergeM3UChannels(channels)
}

// Load from URL
val urlResult = repository.loadM3UPlaylistFromUrl("https://example.com/playlist.m3u")
urlResult.onSuccess { channels ->
    repository.mergeM3UChannels(channels)
}
```

### Search Usage
```kotlin
// Search channels
val channels = repository.searchChannels("BBC")
val channelsInCategory = repository.searchChannels("News", categoryId = "123")

// Search movies
val movies = repository.searchMovies("Matrix")
val actionMovies = repository.searchMoviesByGenre("Action")

// Search series
val series = repository.searchSeries("Breaking")
val comedySeries = repository.searchSeriesByGenre("Comedy")
```

### Sync Usage
```kotlin
// Full sync
lifecycleScope.launch {
    val result = repository.syncAllData()
    if (result.isSuccess) {
        // Update UI
    }
}

// Individual syncs
repository.syncLiveTV()
repository.syncMovies()
repository.syncSeries()
```

## Data Flow

```
UI Layer (Activities/ViewModels)
    ↓ (calls)
Repository Interface (IMediaRepository)
    ↓ (implements)
Repository Implementation (MediaRepository)
    ↓ (uses)
├─ API Service (Xtream Codes) → Network → Cache to Room
├─ M3U Parser → String/URL → Parse → Cache to Room
└─ Room Database (AppDatabase)
       ↓
    DAOs (Channel/Movie/Series/EPG)
       ↓
    Entities (with indices for performance)
       ↓
    SQLite Database (Local Storage)
```

## Performance Considerations

### Database Indices
- EPG queries optimized with indices on channelId, startTime, endTime
- Search queries use LIKE with proper indexing
- Category filtering uses indexed foreign keys

### Caching Strategy
- All API responses cached to Room immediately
- UI reads from cache first (fast)
- Background sync updates cache periodically
- Old data cleaned up automatically

### Memory Management
- DAOs return suspend functions (non-blocking)
- Large lists processed with coroutines
- Room handles cursor management automatically

## Error Handling

All Phase 2 operations include comprehensive error handling:
- Try-catch blocks around all I/O operations
- Result types for API calls (success/failure)
- Logging with Android's Log class
- Graceful degradation (return empty lists on error)
- User-facing errors propagated up through Result types

## Future Enhancements

Phase 2 architecture supports:
- EPG data from multiple sources (XMLTV, JSON EPG)
- Background sync with WorkManager
- Conflict resolution for multiple sources
- User-configurable sync intervals
- EPG notifications for favorite programs
- Advanced search with filters and sorting options
- Full-text search with FTS tables
- Search history and suggestions

## Testing Recommendations

### Unit Tests
- EPG model methods (isLive, isUpcoming, isPast)
- M3U parser with various format samples
- DAO query correctness
- Repository error handling

### Integration Tests
- Database migrations (1 → 2)
- End-to-end sync flow
- Search across large datasets
- M3U import with various formats

### UI Tests
- Search functionality in UI
- EPG display and navigation
- Sync progress indicators

## Build Instructions

No changes to build configuration required. Phase 2 uses existing dependencies:
- Room (already configured)
- Kotlin coroutines (already configured)
- OkHttp (already configured)
- Retrofit (already configured)

To build:
```bash
./gradlew assembleDebug
```

To clean and rebuild:
```bash
./gradlew clean assembleDebug
```

## Backward Compatibility

All Phase 1 functionality is preserved:
- Login flow unchanged
- Existing data operations work identically
- UI/UX unchanged
- ExoPlayer unchanged
- No breaking API changes

New features are purely additive - apps not using Phase 2 features continue to work normally.

## Known Limitations

1. **EPG Source** - Phase 2 provides EPG infrastructure but doesn't fetch EPG data automatically. Apps must implement EPG fetching from their specific sources (XMLTV, JSON, etc.).

2. **M3U EPG Mapping** - M3U parser extracts tvg-id but doesn't automatically fetch/link EPG data. Apps must implement EPG correlation logic.

3. **Sync Scheduling** - Manual sync methods provided. Apps should implement background sync with WorkManager or similar.

4. **Migration** - Currently uses destructive migration. Production apps should implement proper migrations.

5. **Search Optimization** - Basic LIKE search implemented. For large datasets, consider FTS (Full-Text Search) tables.

## Security Considerations

- M3U URLs should be validated before loading
- EPG data should be sanitized before display
- Sync operations should respect rate limits
- User data (favorites, recents) never overwritten during sync
- All database operations use parameterized queries (Room handles this)

## Conclusion

Phase 2 significantly expands the IPTV app's capabilities while maintaining the clean architecture established in Phase 1. The implementation provides a solid foundation for EPG display, multi-source support, powerful search, and reliable data synchronization.
