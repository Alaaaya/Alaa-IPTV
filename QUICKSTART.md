# Quick Start Guide - Alaa IPTV

## Getting Started in 5 Minutes

### Step 1: Prerequisites
- Android TV device or emulator
- IPTV provider credentials OR M3U playlist URL

### Step 2: Installation

#### Option A: Install from APK
1. Download the APK from releases
2. Transfer to Android TV
3. Enable "Install from Unknown Sources"
4. Install the APK

#### Option B: Build from Source
```bash
git clone https://github.com/Alaaaya/Alaa-IPTV.git
cd Alaa-IPTV
./gradlew assembleDebug
# Install APK from app/build/outputs/apk/debug/
```

#### Option C: Android Studio
1. Open Android Studio
2. Open Project → Select Alaa-IPTV folder
3. Wait for Gradle sync
4. Run on Android TV device/emulator

### Step 3: First Launch

1. **Login Screen Appears**
   - Enter your Server URL (e.g., `http://example.com:8080`)
   - Enter Username
   - Enter Password
   - Click "Login"

2. **Wait for Authentication**
   - App validates credentials
   - Downloads content list
   - Navigates to main screen

3. **Main Screen Loaded**
   - You'll see Live TV tab selected
   - Categories at top
   - Channel list on left
   - Preview panel on right

### Step 4: Basic Navigation

#### Using Remote Control
- **Arrow Keys**: Navigate between items
- **OK/Select**: Choose item or play
- **Back**: Return to previous screen
- **Long Press OK**: Enable reorder mode (planned)

#### Tab Switching
- Press **Left/Right** arrows when on tabs
- Tabs: Live TV → Movies → Series → Favorites

#### Category Selection
- Press **Down** from tabs to reach categories
- Press **Left/Right** to scroll categories
- Press **OK** to filter content

#### Channel Selection
- Press **Down** from categories to reach channel list
- Press **Up/Down** to scroll channels
- Preview updates automatically

#### Playing Content
- Select channel with **OK**
- Press **Play** button or **OK** again
- Video starts playing

### Step 5: Using Features

#### Add to Favorites
1. Select any channel
2. Focus on Favorite button (♡)
3. Press OK to toggle
4. Switch to Favorites tab to see all favorites

#### Watch Movies
1. Click Movies tab
2. Select category
3. Choose movie
4. Press Play

#### Browse Series
1. Click Series tab
2. Select series
3. Choose episode
4. Press Play

### Step 6: Player Controls

In video player:
- **OK/Center**: Play/Pause
- **Left Arrow**: Rewind 10 seconds
- **Right Arrow**: Forward 10 seconds
- **Back**: Exit player

## Common Use Cases

### Use Case 1: Quick Channel Surfing
```
1. Launch app (already logged in)
2. Use Up/Down to scroll channels
3. Preview shows on right
4. Press OK to play
```

### Use Case 2: Find Specific Movie
```
1. Click Movies tab
2. Select category (e.g., Action)
3. Scroll to find movie
4. Press Play
```

### Use Case 3: Manage Favorites
```
1. Browse channels
2. Press Favorite (♡) for liked channels
3. Go to Favorites tab
4. Quick access to favorite content
```

## Troubleshooting Quick Fixes

### Can't Login?
- Check internet connection
- Verify server URL format: `http://server:port`
- Confirm username/password are correct
- Try another IPTV provider

### No Channels Showing?
- Wait for content to load (may take 10-30 seconds)
- Check if provider is online
- Try switching categories
- Restart app

### Video Won't Play?
- Check internet speed (5+ Mbps recommended)
- Try another channel
- Verify stream URL is valid
- Check device codec support

### Navigation Not Working?
- Ensure using TV remote or D-Pad
- Try clicking with mouse (if available)
- Restart app
- Check focus is visible on item

### App Crashes?
- Clear app data
- Reinstall app
- Check device has sufficient memory
- Report issue with logs

## Tips & Tricks

1. **Faster Navigation**: Hold arrow keys to scroll quickly
2. **Remember Position**: App remembers last viewed content
3. **Better Performance**: Close other apps before watching
4. **Network**: Use wired connection for best quality
5. **Updates**: Check for updates regularly

## Getting Help

- **Documentation**: Check README.md for detailed docs
- **Testing Guide**: See TESTING.md for known issues
- **Architecture**: Review ARCHITECTURE.md for technical details
- **Issues**: Report bugs on GitHub

## Default Controls Reference

```
Remote Control Mapping:
├── Navigation
│   ├── D-Pad Up/Down: Scroll lists
│   ├── D-Pad Left/Right: Switch tabs/categories
│   └── OK/Select: Choose/Confirm
├── Playback
│   ├── Play/Pause: Toggle playback
│   ├── Rewind: -10 seconds
│   └── Fast Forward: +10 seconds
└── System
    └── Back: Return/Exit
```

## Sample Server Configuration

For testing purposes, you might use:
```
Server URL: http://your-provider.com:8080
Username: your_username
Password: your_password
```

**Note**: Replace with your actual IPTV provider details.

## Next Steps

After getting comfortable with basics:
1. Explore all content categories
2. Build your favorites collection
3. Try different video quality settings
4. Experiment with series episodes
5. Share feedback for improvements

---

**Need More Help?**
- Full Documentation: [README.md](README.md)
- Technical Guide: [ARCHITECTURE.md](ARCHITECTURE.md)
- Testing Info: [TESTING.md](TESTING.md)
- Issues: [GitHub Issues](https://github.com/Alaaaya/Alaa-IPTV/issues)
