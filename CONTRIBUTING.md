# Contributing to Alaa IPTV

Thank you for your interest in contributing to Alaa IPTV! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- Clear description of the bug
- Steps to reproduce
- Expected vs actual behavior
- Device information (model, Android version)
- Screenshots or logs if possible

### Suggesting Features

Feature requests are welcome! Please:
- Check if the feature already exists or is planned
- Clearly describe the feature and use case
- Explain why it would benefit users

### Code Contributions

1. **Fork the Repository**
   ```bash
   git clone https://github.com/Alaaaya/Alaa-IPTV.git
   cd Alaa-IPTV
   ```

2. **Create a Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/bug-description
   ```

3. **Make Your Changes**
   - Follow the existing code style
   - Add comments for complex logic
   - Update documentation if needed
   - Test your changes thoroughly

4. **Commit Your Changes**
   ```bash
   git add .
   git commit -m "Brief description of changes"
   ```

5. **Push and Create Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```

## Code Style Guidelines

### Kotlin Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Use coroutines for async operations

### Example
```kotlin
// Good
private fun loadChannels(categoryId: String?) {
    lifecycleScope.launch {
        repository.getLiveStreams(categoryId)
            .onSuccess { channels ->
                updateChannelList(channels)
            }
            .onFailure { error ->
                showError(error.message)
            }
    }
}

// Avoid
private fun load(id: String?) { /* ... */ }
```

### XML Style
- Use meaningful IDs
- Follow Android naming conventions
- Keep layouts simple and modular

### Example
```xml
<!-- Good -->
<TextView
    android:id="@+id/channelNameText"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:textColor="@color/text_primary"
    android:textSize="16sp" />

<!-- Avoid -->
<TextView
    android:id="@+id/text1"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content" />
```

## Project Structure

Please maintain the existing structure:
```
app/src/main/java/com/alaa/iptv/
├── data/
│   ├── api/           # API services
│   ├── models/        # Data models
│   ├── preferences/   # Settings
│   └── repository/    # Data repositories
├── ui/
│   ├── login/         # Login UI
│   ├── main/          # Main UI
│   └── player/        # Player UI
└── utils/             # Utilities
```

## Testing

### Manual Testing
- Test on Android TV device or emulator
- Verify D-Pad navigation works
- Check all features function correctly
- Test with real IPTV streams

### Automated Testing (Future)
When adding tests:
- Unit tests in `src/test/`
- Instrumentation tests in `src/androidTest/`
- Follow existing test patterns

## Documentation

Update documentation when:
- Adding new features
- Changing existing behavior
- Fixing significant bugs

Files to update:
- `README.md` - Main documentation
- `QUICKSTART.md` - If affecting setup
- `ARCHITECTURE.md` - If changing structure
- `UI_DESIGN.md` - If changing UI

## Pull Request Process

1. **Before Submitting**
   - Test your changes
   - Update documentation
   - Ensure code compiles
   - Check for conflicts with main branch

2. **PR Description Should Include**
   - What changes were made
   - Why the changes were needed
   - How to test the changes
   - Screenshots for UI changes

3. **Review Process**
   - Maintainers will review your PR
   - Address any requested changes
   - Once approved, PR will be merged

## Feature Priorities

High priority features:
1. EPG (Electronic Program Guide) integration
2. Search functionality
3. Settings screen
4. Improved error handling
5. Performance optimizations

Medium priority:
1. Parental controls
2. Multiple profiles
3. Watch history
4. Recommendations

## Code of Conduct

### Our Standards
- Be respectful and inclusive
- Provide constructive feedback
- Focus on what's best for the project
- Be patient with newcomers

### Unacceptable Behavior
- Harassment or discrimination
- Trolling or insulting comments
- Publishing private information
- Other unprofessional conduct

## Questions?

- Create an issue for general questions
- Email maintainers for private inquiries
- Check existing issues/PRs first

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Alaa IPTV! 🎉
