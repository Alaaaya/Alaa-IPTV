# Testing Guide for Alaa IPTV

## Prerequisites for Testing

### Hardware Requirements
- Android TV device (Android 5.0 or higher) OR
- Android TV emulator in Android Studio
- TV remote or USB keyboard for navigation

### Software Requirements
- Android Studio Arctic Fox or newer
- Android SDK 21-34
- IPTV provider credentials (Xtream Codes compatible) OR M3U playlist URL

## Build Instructions

### Using Android Studio
1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the Alaa-IPTV directory
4. Wait for Gradle sync to complete
5. Connect Android TV device or start emulator
6. Click "Run" (Shift+F10) and select target device

### Using Command Line
```bash
cd Alaa-IPTV
./gradlew assembleDebug
# APK will be in app/build/outputs/apk/debug/
```

## Manual Testing Checklist

### Login Screen Testing
- [ ] Launch app shows login screen
- [ ] Server URL field accepts valid URLs
- [ ] Username field accepts text input
- [ ] Password field accepts text with masking
- [ ] Tab navigation works between fields
- [ ] Login button is focusable and clickable
- [ ] Valid credentials authenticate successfully
- [ ] Invalid credentials show error message
- [ ] Network error displays appropriate message
- [ ] Credentials are saved after successful login
- [ ] Return to app after successful login skips login screen

### Main Screen Navigation
- [ ] Main screen loads after successful login
- [ ] Live TV tab is selected by default
- [ ] Tabs are navigable with D-Pad left/right
- [ ] Tab focus is visible
- [ ] Tab selection changes content area

### Category Navigation
- [ ] Categories load horizontally
- [ ] "All Channels" category appears first
- [ ] D-Pad left/right navigates categories
- [ ] Category focus is visible with scaling effect
- [ ] Selected category filters content list
- [ ] Category changes update channel list

### Channel List Navigation
- [ ] Channels load in vertical list
- [ ] D-Pad up/down navigates channels
- [ ] Channel focus is visible with scaling
- [ ] Focused channel updates preview panel
- [ ] Channel icons load correctly
- [ ] Channel number displays correctly
- [ ] Favorite indicator shows for favorited channels

### Preview Panel
- [ ] Preview updates when channel focus changes
- [ ] Channel name displays correctly
- [ ] Channel number displays in info
- [ ] Channel icon/poster displays
- [ ] Play button is visible and focusable
- [ ] Favorite button is visible and focusable
- [ ] Favorite button shows correct state (filled/outline heart)

### Favorites Functionality
- [ ] Click favorite button adds to favorites
- [ ] Click favorite button again removes from favorites
- [ ] Toast message confirms add/remove action
- [ ] Favorite state persists after app restart
- [ ] Favorites tab shows only favorited items
- [ ] Favorite indicator appears on items in all tabs

### Movies Tab
- [ ] Movies tab loads movie categories
- [ ] Movies list displays correctly
- [ ] Movie posters load
- [ ] Movie selection updates preview
- [ ] Movie playback works

### Series Tab
- [ ] Series tab loads series categories
- [ ] Series list displays correctly
- [ ] Series posters load
- [ ] Series selection updates preview
- [ ] Series episodes can be accessed

### Video Player
- [ ] Play button starts video playback
- [ ] Player screen shows with video loading
- [ ] Video plays smoothly
- [ ] Channel name displays initially
- [ ] Channel name auto-hides after 3 seconds
- [ ] Buffering indicator shows when buffering
- [ ] D-Pad center/OK pauses/plays video
- [ ] D-Pad left seeks backward 10 seconds
- [ ] D-Pad right seeks forward 10 seconds
- [ ] Back button exits player and returns to main screen
- [ ] Player releases resources on exit
- [ ] Error message displays for invalid streams

### Performance Testing
- [ ] App loads within 3 seconds
- [ ] Channel list scrolls smoothly
- [ ] Category navigation is responsive
- [ ] Preview images load quickly
- [ ] Player starts within 2 seconds
- [ ] No memory leaks during extended use
- [ ] App handles network interruptions gracefully

### Compatibility Testing
- [ ] Test on different Android TV devices
- [ ] Test with different screen sizes/resolutions
- [ ] Test with different remote controls
- [ ] Test with keyboard input
- [ ] Test with mouse (if supported)

## Known Limitations

1. **Reordering Feature**: Long-press reordering is mentioned but requires additional implementation
2. **EPG Support**: Electronic Program Guide not yet implemented
3. **Multi-language**: Currently English only
4. **Parental Controls**: Not implemented
5. **Search**: Search functionality not yet added

## Test IPTV Providers

For testing, you can use free IPTV test providers:
- Many providers offer 24-48 hour test accounts
- Some provide demo M3U playlists
- Search for "IPTV test account" or "free IPTV m3u"

**Note**: Use legitimate services only. This app is for personal use with legally obtained content.

## Automated Testing

The project structure supports automated testing:
- Unit tests for data models and repositories
- Instrumentation tests for UI components
- Espresso tests for TV navigation flows

Currently no automated tests are implemented to maintain minimal change scope.

## Reporting Issues

When reporting issues, include:
1. Device model and Android version
2. Steps to reproduce
3. Expected vs actual behavior
4. Screenshots or video if possible
5. Logcat output if available

## Debug Logging

To enable detailed logging:
```bash
adb logcat | grep "AlaaIPTV\|ExoPlayer"
```

This will show API calls, player events, and errors.
