# UI Design & Screenshots

## Design System

### Color Palette

#### Primary Colors
- **Primary Blue**: `#2196F3` - Main accent color, buttons, focus indicators
- **Accent Blue**: `#64B5F6` - Hover states, secondary elements
- **Dark Blue**: `#1976D2` - Active/pressed states

#### Background Colors
- **Dark Background**: `#0D1117` - Main app background
- **Card Background**: `#161B22` - Card and panel backgrounds
- **Glassmorphism**: `rgba(255, 255, 255, 0.15)` - Translucent overlay effect

#### Text Colors
- **Text Primary**: `#FFFFFF` - Main text, titles
- **Text Secondary**: `#B0B0B0` - Subtitles, metadata
- **Text Hint**: `#808080` - Placeholder text

### Typography
- **Title**: 32sp, Bold (Login screen)
- **Section Title**: 24sp, Bold (Preview title)
- **Body Large**: 20sp, Bold (Channel name overlay)
- **Body**: 16sp (Category names, buttons)
- **Caption**: 14sp (Error messages, metadata)

### Spacing System
- **Extra Small**: 4dp
- **Small**: 8dp
- **Medium**: 12dp
- **Large**: 16dp
- **Extra Large**: 24dp
- **XXL**: 48dp (Screen padding)

### Border Radius
- **Small**: 8dp (Inputs, small cards)
- **Medium**: 12dp (Content cards)
- **Large**: 16dp (Glassmorphism panels)

## Screen Designs

### 1. Login Screen

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                    │
│                                                                    │
│                                                                    │
│              ┌──────────────────────────────────────┐            │
│              │                                      │            │
│              │   Welcome to Alaa IPTV              │            │
│              │                                      │            │
│              │   ┌──────────────────────────────┐  │            │
│              │   │ Server URL                   │  │            │
│              │   │ http://example.com:8080      │  │            │
│              │   └──────────────────────────────┘  │            │
│              │                                      │            │
│              │   ┌──────────────────────────────┐  │            │
│              │   │ Username                     │  │            │
│              │   │ your_username                │  │            │
│              │   └──────────────────────────────┘  │            │
│              │                                      │            │
│              │   ┌──────────────────────────────┐  │            │
│              │   │ Password                     │  │            │
│              │   │ ••••••••••                   │  │            │
│              │   └──────────────────────────────┘  │            │
│              │                                      │            │
│              │   ┌──────────────────────────────┐  │            │
│              │   │         [ Login ]            │  │            │
│              │   └──────────────────────────────┘  │            │
│              │                                      │            │
│              │            ⟳ Loading...              │            │
│              │                                      │            │
│              └──────────────────────────────────────┘            │
│                                                                    │
│                                                                    │
└──────────────────────────────────────────────────────────────────┘

Design Elements:
- Centered glassmorphism card (600dp width)
- 48dp padding around container
- Translucent white background with blur
- Blue focus indicators on inputs
- Large, bold title text
- Consistent spacing between elements
```

### 2. Main Screen - Live TV

```
┌──────────────────────────────────────────────────────────────────┐
│ ┌────┬────┬────┬────┐                                            │
│ │Live│Mov │Ser │Fav │                                            │
│ │ TV │ies│ies│ ♥ │         PREVIEW PANEL                        │
│ └────┴────┴────┴────┘                                            │
│                                                                    │
│ ┌──────────────────┐  ┌──────────────────────────────────────┐  │
│ │[All][News][Sports]│  │                                      │  │
│ └──────────────────┘  │         [Channel Poster]             │  │
│                        │                                      │  │
│ ┌──────────────────┐  │                                      │  │
│ │ 📺 CNN           │  │                                      │  │
│ │ Ch 101           │  └──────────────────────────────────────┘  │
│ ├──────────────────┤                                            │
│ │►📺 BBC News      │  CNN International                         │
│ │ Ch 102      ❤   │  Channel 102                               │
│ ├──────────────────┤                                            │
│ │ 📺 Fox News      │  ┌──────────┐  ┌────┐                     │
│ │ Ch 103           │  │  [ Play ]  │  │ ♡ │                     │
│ ├──────────────────┤  └──────────┘  └────┘                     │
│ │ 📺 Sky News      │                                            │
│ │ Ch 104           │                                            │
│ ├──────────────────┤                                            │
│ │ 📺 MSNBC         │                                            │
│ │ Ch 105           │                                            │
│ └──────────────────┘                                            │
└──────────────────────────────────────────────────────────────────┘

Design Elements:
- Left panel: 1/3 width
- Right panel: 2/3 width
- Tab bar with glassmorphism background
- Horizontal category scrolling
- Vertical channel list with icons
- Focus indicator (►) and scaling effect
- Preview with large poster/icon
- Favorite indicator (❤) on saved items
- Glassmorphism cards throughout
```

### 3. Main Screen - Movies

```
┌──────────────────────────────────────────────────────────────────┐
│ ┌────┬────┬────┬────┐                                            │
│ │Live│►Mov│Ser │Fav │                                            │
│ │ TV │ies│ies│ ♥ │         PREVIEW PANEL                        │
│ └────┴────┴────┴────┘                                            │
│                                                                    │
│ ┌──────────────────┐  ┌──────────────────────────────────────┐  │
│ │[All][Action][Drama]│ │                                      │  │
│ └──────────────────┘  │      [Movie Poster/Cover]            │  │
│                        │                                      │  │
│ ┌──────────────────┐  │                                      │  │
│ │ 🎬 Inception      │  │                                      │  │
│ ├──────────────────┤  └──────────────────────────────────────┘  │
│ │►🎬 The Matrix     │                                            │
│ │      ❤          │  The Matrix (1999)                          │
│ ├──────────────────┤  ⭐ 8.7 | 2h 16m                            │
│ │ 🎬 Interstellar  │                                            │
│ ├──────────────────┤  ┌──────────┐  ┌────┐                     │
│ │ 🎬 Tenet         │  │  [ Play ]  │  │ ❤ │                     │
│ ├──────────────────┤  └──────────┘  └────┘                     │
│ │ 🎬 Dark Knight   │                                            │
│ └──────────────────┘                                            │
└──────────────────────────────────────────────────────────────────┘

Design Elements:
- Movies tab highlighted
- Movie-specific categories
- Movie posters in preview
- Rating and duration display
- Same layout structure as Live TV
```

### 4. Video Player

```
┌──────────────────────────────────────────────────────────────────┐
│ ┌────────────────┐                                               │
│ │ CNN International│                                              │
│ └────────────────┘                                               │
│                                                                    │
│                                                                    │
│                                                                    │
│                                                                    │
│                      [ VIDEO PLAYING ]                            │
│                                                                    │
│                                                                    │
│                                                                    │
│                                                                    │
│                                                                    │
│                                                                    │
│ ══════════════════════════════════════════════════════════       │
│ 00:15:23                                                          │
└──────────────────────────────────────────────────────────────────┘

Design Elements:
- Full-screen video
- Channel name overlay (top-left, auto-hides)
- Minimal controls (appears on interaction)
- Progress bar at bottom
- Dark overlay for controls
- Buffering indicator when loading

Controls (visible on interaction):
┌──────────────────────────────────────────────────────────────────┐
│                                                                    │
│                      [ VIDEO PLAYING ]                            │
│                                                                    │
│                                                                    │
│                         ▶ | | ◀                                   │
│                      (Play/Pause)                                 │
│                                                                    │
│ ══════════════════●═══════════════════════════════════           │
│ 00:15:23                                      -01:24:37           │
└──────────────────────────────────────────────────────────────────┘
```

### 5. Favorites View

```
┌──────────────────────────────────────────────────────────────────┐
│ ┌────┬────┬────┬────┐                                            │
│ │Live│Mov │Ser │►Fav│                                            │
│ │ TV │ies│ies│ ♥ │         PREVIEW PANEL                        │
│ └────┴────┴────┴────┘                                            │
│                                                                    │
│ (No categories shown in Favorites)                                │
│                                                                    │
│ ┌──────────────────┐  ┌──────────────────────────────────────┐  │
│ │►📺 BBC News ❤   │  │                                      │  │
│ │ Ch 102           │  │      [Channel/Content Poster]        │  │
│ ├──────────────────┤  │                                      │  │
│ │ 🎬 The Matrix ❤  │  │                                      │  │
│ ├──────────────────┤  │                                      │  │
│ │ 📺 CNN      ❤    │  │                                      │  │
│ │ Ch 101           │  └──────────────────────────────────────┘  │
│ ├──────────────────┤                                            │
│ │ 📺 Sky Sports❤   │  BBC News                                  │
│ │ Ch 230           │  Channel 102                               │
│ ├──────────────────┤                                            │
│ │ 🎬 Inception ❤   │  ┌──────────┐  ┌────┐                     │
│ └──────────────────┘  │  [ Play ]  │  │ ❤ │                     │
│                        └──────────┘  └────┘                     │
└──────────────────────────────────────────────────────────────────┘

Design Elements:
- Mixed content types (Live TV, Movies, Series)
- All items show favorite indicator (❤)
- No category filtering
- Same preview functionality
```

## UI States

### Focus States
- **Unfocused**: Default glassmorphism background
- **Focused**: 
  - Border: 2dp solid blue (#2196F3)
  - Scale: 105% (1.05x)
  - Background: Lighter glassmorphism
  - Smooth animation (200ms)

### Button States
- **Normal**: Blue background (#2196F3)
- **Focused**: Lighter blue (#64B5F6), 110% scale
- **Pressed**: Dark blue (#1976D2)
- **Disabled**: Gray (#808080), 50% opacity

### Loading States
- **Initial Load**: Full-screen progress spinner
- **Content Loading**: Small spinner in preview panel
- **Video Buffering**: Spinner overlay on player

### Error States
- **Login Error**: Red text below login button
- **Network Error**: Toast message + error text
- **Player Error**: Error message overlay on player

## Animations

### Focus Animation
```
Duration: 200ms
Easing: DecelerateInterpolator
Properties: scaleX, scaleY (1.0 → 1.05)
```

### Tab Switch
```
Duration: 150ms
Easing: FastOutSlowIn
Properties: alpha (0 → 1), translateX
```

### Preview Update
```
Duration: 300ms
Easing: DecelerateInterpolator
Properties: alpha (0 → 1)
Image: Fade in with Glide
```

## Accessibility

### Focus Order
1. Tabs (horizontal)
2. Categories (horizontal)
3. Content list (vertical)
4. Preview buttons (horizontal)

### Visual Indicators
- Focus border: 2dp blue outline
- Scale effect: 105-110%
- Color change on focus
- Cursor indicator (►) on selected item

### Text Contrast
- White text on dark background: 21:1 ratio
- Blue buttons with white text: 4.5:1 ratio
- Secondary text (gray): 7:1 ratio

## Responsive Design

### Different Screen Sizes

#### 720p (1280x720)
- Reduce padding to 32dp
- Smaller font sizes (-2sp)
- Compact spacing

#### 1080p (1920x1080) - Optimal
- Default sizing
- 48dp padding
- Standard spacing

#### 4K (3840x2160)
- Increase padding to 64dp
- Larger font sizes (+4sp)
- More generous spacing

### Orientation
- **Landscape Only**: Optimized for TV viewing
- Layout fixed to landscape orientation
- No portrait mode support

## Implementation Notes

### Custom Views
- No default Leanback library components
- Custom RecyclerView implementation
- Manual focus handling throughout

### Material Design
- Glass morphism effect using alpha channels
- Elevation for depth
- Rounded corners for modern look
- Consistent color system

### Performance
- Hardware acceleration enabled
- Smooth 60fps animations
- Efficient image loading with Glide
- Optimized RecyclerView rendering
