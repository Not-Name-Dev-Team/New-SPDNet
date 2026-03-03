# SPDNet (Shattered Pixel Dungeon Online)

SPDNet is a multiplayer online version of [Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/), an open-source traditional roguelike dungeon crawler with randomized levels and enemies, and hundreds of items to collect and use. It's based on the [source code of Pixel Dungeon](https://github.com/00-Evan/pixel-dungeon-gradle), by [Watabou](https://watabou.itch.io/).

This project adds multiplayer functionality to the classic Shattered Pixel Dungeon experience, allowing players to explore dungeons together, chat, and share their adventures in real-time.

## Features

- **Multiplayer Gameplay**: Explore dungeons with other players in real-time
- **Player Interaction**: See other players move through the dungeon, chat with them, and share items
- **Leaderboards**: Compete with other players and view rankings
- **Cross-Platform**: Supports Android and Desktop platforms
- **Web Dashboard**: View game statistics and player information via the web interface

## Download

The game is distributed through **GitHub Releases**:

[![GitHub Releases](https://shatteredpixel.com/assets/images/badges/github.png)](https://github.com/Not-Name-Dev-Team/New-SPDNet/releases)

## Web Dashboard

Access the game web dashboard at:

**http://jdsalingzx.top:10061**

The dashboard provides:
- Player statistics and leaderboards
- Real-time player activity
- Game records and achievements

## Server Information

- **Default Server Port**: 21687
- **Communication Protocol**: Socket.IO
- **Database**: SQLite for player data and game records

## Technical Stack

### Client (Android/Desktop)
- **Language**: Java
- **JSON Library**: fastjson1 (Android 6 compatibility)
- **Network**: Socket.IO client

### Server
- **Framework**: Spring Boot
- **Language**: Java
- **JSON Library**: fastjson2
- **Socket Server**: netty-socketio
- **Frontend**: Vue.js

## Project Structure

```
├── core/           # Core game logic (Shattered Pixel Dungeon)
├── android/        # Android-specific code
├── desktop/        # Desktop-specific code
├── ios/            # iOS-specific code
├── server/         # Multiplayer server (Spring Boot + Vue.js frontend)
├── spdnet/         # New multiplayer classes
└── spdnetbutcopy/  # Modified original game classes
```

## Development

### Package Structure

- **`me.catand.spdnet.*`**: New classes created from scratch for multiplayer functionality
- **`me.catand.spdnetbutcopy.*`**: Classes that replace/modify original game classes

### Building

See the documentation in `/docs` for compilation guides:
- [Compiling for Android](docs/getting-started-android.md)
- [Compiling for desktop platforms](docs/getting-started-desktop.md)
- [Compiling for iOS](docs/getting-started-ios.md)

## Credits

- Original Pixel Dungeon by [Watabou](https://watabou.itch.io/)
- Shattered Pixel Dungeon by [Evan Debenham](https://shatteredpixel.com/)
- SPDNet multiplayer modifications by catand

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**, the same license as the original Shattered Pixel Dungeon.

## Acknowledgments

Special thanks to:

- **[Watabou](https://watabou.itch.io/)**, the creator of the original Pixel Dungeon, for creating the foundation of this amazing roguelike experience.

- **[Evan Debenham](https://shatteredpixel.com/)**, the creator of Shattered Pixel Dungeon, for transforming Pixel Dungeon into an incredible open-source roguelike and for actively supporting the modding community. Your dedication to open-source development has made projects like SPDNet possible. Thank you for encouraging and embracing community modifications!

- **[saqfish](https://github.com/saqfish)**, the creator of the original SPDNet prototype, for pioneering the multiplayer concept for Shattered Pixel Dungeon and inspiring this project. Your work laid the groundwork for what SPDNet has become today.

## Note

This is a fan-made multiplayer modification and is not officially affiliated with or endorsed by the original Shattered Pixel Dungeon developers.
