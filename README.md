# RuleKeeper Android Dashboard

A modern Android application for managing your RuleKeeper Discord bot instances. Built with Kotlin and Jetpack Compose, this app provides a mobile-optimized interface to access all RuleKeeper features on the go.

## Features

### Key Features

- **Flexible Authentication**
  - Bot Admin login with username/password
  - Discord OAuth login
  - JWT token-based authentication
  - Secure token storage

- **Configurable API URL**
  - Connect to any RuleKeeper instance
  - Support for custom/self-hosted servers
  - Easy server switching

- **Mobile-Optimized UI**
  - Modern Material Design 3
  - Dark/Light theme support
  - Responsive layouts for phones and tablets
  - Intuitive navigation

- **Server Management**
  - View all servers where the bot is installed
  - Access server-specific dashboards
  - Manage commands, logging, moderation, and more

### Implemented Features

- **Authentication & Settings**
  - Custom API URL configuration
  - Bot Admin login (username/password)
  - Discord OAuth login with mobile deep link support
  - JWT token management
  - Secure OAuth code handling with single-use enforcement

- **Server Management**
  - Guild (server) list with icons
  - Server selection and navigation
  - Real-time server information

- **Commands System**
  - View all custom commands
  - Add/edit/delete commands
  - Configure command responses
  - Ephemeral command support

- **Welcome Messages**
  - Enable/disable welcome messages
  - Channel selection
  - Text or embed messages
  - Placeholder support ({user}, {server}, {membercount})
  - Custom embed configuration

- **Goodbye Messages**
  - Enable/disable goodbye messages
  - Channel selection
  - Text or embed messages
  - Placeholder support
  - Custom embed configuration

- **Blocked Words**
  - Add/remove blocked words
  - Warning message configuration
  - Custom embed for warnings

- **Auto Roles**
  - Role selection for auto-assignment
  - Multi-role support
  - New member auto-role assignment

- **Leveling System**
  - XP per message configuration
  - XP cooldown settings
  - Level-up messages
  - Level rewards (role rewards)
  - Leaderboard view

- **Spam Protection**
  - Message spam detection
  - Mention limit configuration
  - Duplicate message detection
  - Auto-mute settings
  - Channel and role exclusions

- **Warnings System**
  - View active warnings
  - Add/remove warnings
  - Warning threshold configuration
  - Automatic actions (timeout/kick/ban)
  - Auto-expire warnings

- **Backup Management**
  - Create manual backups
  - Download backups
  - Restore from backup
  - Scheduled automatic backups (daily/weekly/monthly)
  - Backup with message history option
  - Configure backup schedules with max retention

- **Tickets System**
  - View all tickets (open, in progress, closed)
  - Create new tickets
  - Filter tickets by status
  - Close/reopen tickets
  - Ticket categories
  - Auto-close inactive tickets
  - Configure ticket settings

- **Twitch Announcements**
  - Manage stream go-live notifications
  - Configure streamer channels to monitor
  - Customize announcement messages
  - Select Discord channel and role mentions
  - Template variables: {streamer}, {title}, {game}, {url}, {role}
  - Enable/disable individual announcements
  - Up to 15 streamers per user

- **YouTube Announcements**
  - Manage video upload notifications
  - Configure YouTube channels to monitor
  - Customize announcement messages
  - Select Discord channel and role mentions
  - Template variables: {channel}, {title}, {url}, {role}
  - Enable/disable individual announcements
  - Up to 15 channels per user

- **Crafty Controller Integration**
  - Manage multiple Crafty Controller instances
  - Add/edit/delete Crafty instances
  - Test connection to Crafty API
  - View all Minecraft servers across instances
  - Real-time server status (running/stopped/unknown)
  - Start/stop/restart servers remotely
  - Server details (name, port, description)
  - Automatic configuration loading
  - Success/failure feedback for all operations

- **Command Permissions**
  - Set role-based permissions for all commands (built-in + custom)
  - Whitelist/blacklist specific roles
  - Per-command permission management
  - Export/import permission configurations
  - View all available commands

- **Role Menus**
  - Create and manage role menus
  - Configure menu types (buttons, dropdowns, reactions)
  - Add/remove roles from menus
  - Set role limits (min/max roles per user)
  - Configure menu messages and embeds
  - Deploy menus to specific channels
  - Edit and delete existing menus

- **Forms Builder**
  - Create custom forms with multiple field types
  - Field types: text, textarea, number, select, checkbox
  - Required/optional field configuration
  - Form enable/disable toggle
  - View form submissions
  - Edit/delete forms
  - Submission management

- **Game Roles**
  - Automatically assign roles based on detected games
  - Add/edit/delete game role mappings
  - Configure game detection for Discord activities
  - Role assignment automation

- **Birthday Management**
  - Configure birthday announcements
  - Set announcement channel and birthday role
  - Add/edit/delete user birthdays
  - View all registered birthdays
  - Automatic birthday notifications

- **Leaderboard**
  - View server XP leaderboard
  - Top 100 users display
  - Rank, username, level, and XP information
  - Medal indicators for top 3 positions
  - Refresh functionality

- **User Management**
  - View all server users
  - User details (XP, level, warnings, birthdays)
  - Manually modify user XP
  - Add/remove XP from users
  - Search and filter users

- **Server Logs**
  - View server event logs
  - Filter logs by event type
  - Date range filtering
  - Log details and timestamps

- **Ticket Transcripts**
  - View all closed ticket transcripts
  - Download transcripts
  - Search tickets by user or content
  - Transcript details and messages

- **Restore User Data**
  - Restore user data from backups
  - Select specific users to restore
  - XP, warnings, and other data restoration
  - Backup file selection

- **Logging Configuration**
  - Configure event logging channels
  - Enable/disable specific log events
  - Log format customization

- **Moderation Tools**
  - View moderation statistics
  - Quick access to warnings and bans
  - User moderation actions

## Requirements

- Android 7.0 (API 24) or higher
- Internet connection
- RuleKeeper bot instance with REST API enabled
- For Discord OAuth: Backend must support `/mobile-callback` route
- For Discord OAuth: Custom redirect URI configured in Discord Developer Portal

## Installation

### Option 1: Build from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/RuleKeeper-Bot/RuleKeeper-Android.git
   cd RuleKeeper-Android
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `RuleKeeper-Android` folder
   - Wait for Gradle sync to complete

3. **Build and run:**
   - Connect your Android device or start an emulator
   - Click the "Run" button in Android Studio
   - Or use: `./gradlew installDebug`

### Option 2: Download APK

APK releases at the [GitHub Releases page](https://github.com/RuleKeeper-Bot/RuleKeeper-Android/releases/latest)

**Note:** The APK must be signed with the same certificate to preserve the deep link association for OAuth. Installing unsigned debug builds may require reconfiguring Discord OAuth after each installation.

## Configuration

### Discord OAuth Setup (for Mobile App)

The mobile app uses a custom deep link scheme for OAuth authentication. To enable Discord login:

1. **Server Configuration:**
   - The backend must support the `/mobile-callback` route
   - This route redirects to `cc.rulekeeper.dashboard://callback` with the OAuth code

2. **Discord Developer Portal:**
   - Add the following redirect URI to your Discord application:
     - `https://your-server.com/mobile-callback` (replace with your actual server URL)
   - The web dashboard redirect URI should also be configured:
     - `https://your-server.com/callback`

3. **OAuth Flow:**
   - User clicks "Login with Discord" in the app
   - Browser opens for Discord authorization
   - After authorization, Discord redirects to `https://your-server.com/mobile-callback`
   - Server redirects to `cc.rulekeeper.dashboard://callback?code=...`
   - App receives the deep link and completes authentication
   - OAuth codes are single-use and automatically cleared after processing

### Setting up the API URL

1. Launch the app
2. On the Settings screen, enter your RuleKeeper API URL
   - Format: `https://your-server.com/api/v1/`
   - Make sure to include `/api/v1/` at the end
3. Tap "Save URL"
4. Proceed to login

### Default API URL

The default API URL is set to: `https://rulekeeper.cc/api/v1/`

You can change this in the Settings screen to point to your own RuleKeeper instance.

## Usage

### Logging In

**Bot Admin Login:**
1. Select "Bot Admin Login"
2. Enter your bot admin username and password
3. Tap "Login"

**Discord Login:**
1. Select "Discord Login"
2. Tap "Login with Discord"
3. Your browser will open for Discord authorization
4. Complete the authorization (and MFA if enabled)
5. You'll be automatically redirected back to the app
6. Login completes automatically

### Managing Servers

1. After login, you'll see a list of servers
2. Tap on a server to access its dashboard
3. The dashboard is organized into 8 sections:
   - **Server Configuration** - Commands, permissions, logging, welcome/goodbye, auto roles, spam, blocked words, role menus
   - **Leveling System** - XP configuration and leaderboard
   - **Moderation** - User management, warnings, and bans
   - **Custom Forms** - Form builder and submissions
   - **Ticket System** - Ticket menus and transcripts
   - **Social Pings** - Twitch and YouTube announcements
   - **Fun/Miscellaneous** - Game roles, birthdays, Crafty Controller
   - **Backup/Restore** - Backup management and scheduled backups
4. Tap on any feature to configure it

### Managing Features

The app provides comprehensive management for all RuleKeeper features:

#### Commands
- Create custom commands with triggers and responses
- Configure ephemeral responses
- Edit and delete commands

#### Welcome/Goodbye Messages
- Configure welcome messages for new members
- Set up goodbye messages for leaving members
- Choose between text and rich embeds
- Use placeholders: {user}, {server}, {membercount}
- Customize embed colors and content

#### Blocked Words
- Maintain a list of blocked words
- Configure warning messages for violations
- Custom embed styling for warnings

#### Auto Roles
- Select roles to automatically assign to new members
- Multi-role support
- Easy role selection interface

#### Leveling System
- Set XP per message
- Configure XP cooldown
- Create level rewards (role unlocks)
- View server leaderboard
- Customize level-up messages

#### Spam Protection
- Configure message spam thresholds
- Set mention limits
- Detect duplicate messages
- Auto-mute spammers
- Exclude specific channels and roles

#### Warnings System
- View all active warnings
- Add warnings to users
- Configure warning thresholds
- Set automatic actions (timeout/kick/ban)
- Auto-expire old warnings

#### Backups
- Create manual backups
- Schedule automatic backups (daily/weekly/monthly)
- Configure backup schedules:
  - Set frequency and start time
  - Maximum backup retention
  - Enable/disable schedules
  - Timezone configuration
- Download backups to device
- Restore from previous backups
- Include or exclude message history

#### Tickets
- View all support tickets
- Filter tickets by status (open/in progress/closed)
- Create new tickets with categories
- Close or reopen tickets
- Configure ticket system settings
- Set max tickets per user
- Auto-close inactive tickets
- Custom welcome messages for new tickets

#### Twitch Announcements
- Add Twitch streamers to monitor
- Configure announcement channel and role to mention
- Customize go-live messages with template variables
- Enable/disable notifications per streamer
- Support for multiple streamers (up to 15 per user)
- Edit or delete existing announcements

#### YouTube Announcements
- Add YouTube channels to monitor
- Configure announcement channel and role to mention
- Customize upload messages with template variables
- Enable/disable notifications per channel
- Support for multiple channels (up to 15 per user)
- Edit or delete existing announcements

#### Crafty Controller
- Connect to multiple Crafty Controller instances
- Add new instances with name, API URL, and token
- Edit existing instance configurations
- Delete instances with confirmation
- Test API connection with success/failure feedback
- View all Minecraft servers across all instances
- See real-time server status (running/stopped/unknown)
- Perform server actions:
  - Start servers
  - Stop servers
  - Restart servers
- Automatic status refresh after actions
- Color-coded status indicators (green/red/yellow)
- Detailed server information (port, description)
- Secure API token storage

#### Command Permissions
- View all available commands (built-in and custom)
- Set role-based permissions for each command
- Whitelist specific roles (only these roles can use command)
- Blacklist specific roles (these roles cannot use command)
- Export permissions to clipboard/file
- Import permissions from backup
- Clear all permissions for a command

#### Role Menus
- Create interactive role menus with multiple types:
  - Button menus (persistent buttons)
  - Dropdown menus (select menus)
  - Reaction menus (emoji reactions)
- Configure role assignments with limits:
  - Minimum roles required
  - Maximum roles allowed
  - Single-select or multi-select
- Customize menu appearance:
  - Custom titles and descriptions
  - Embed colors
  - Button labels and emojis
- Deploy menus to specific channels
- Edit and manage existing menus
- Delete menus with confirmation

#### Forms
- Create custom forms with flexible field types:
  - Text input (single line)
  - Text area (multi-line)
  - Number input
  - Select/dropdown
  - Checkbox
- Configure field properties:
  - Required vs optional
  - Field labels and placeholders
  - Dropdown options
- Enable/disable forms
- View all form submissions
- Navigate to submission details
- Edit form configuration
- Delete forms with submissions

#### Game Roles
- Automatically assign roles when users play specific games
- Add game-to-role mappings
- Edit existing game role configurations
- Delete game role mappings
- Game name detection from Discord activity
- Multiple games per server

#### Birthday Management
- Configure birthday announcement system:
  - Select announcement channel
  - Assign birthday role
  - Enable/disable birthday announcements
- Manage user birthdays:
  - Add birthdays for users
  - View all registered birthdays
  - Edit existing birthdays
  - Delete birthdays
- Automatic birthday role assignment on user's birthday
- Birthday list with usernames and dates

#### Leaderboard
- View top 100 users by XP
- Display rank, username, level, and total XP
- Medal indicators for top 3:
  - 🥇 1st place (gold)
  - 🥈 2nd place (silver)
  - 🥉 3rd place (bronze)
- Refresh to update rankings
- Smooth scrolling list

#### User Management
- View all users in the server
- Search users by username
- User profile details:
  - Current XP and level
  - Warning count
  - Birthday information
  - Join date and other metadata
- Modify user XP:
  - Add XP (with reason)
  - Remove XP (with reason)
  - Manual XP adjustment
- Navigate to user detail view
- User edit functionality

#### Server Logs
- View comprehensive server event logs
- Filter by event type:
  - Member joins/leaves
  - Message edits/deletes
  - Role changes
  - Channel changes
  - Moderation actions
- Date range filtering
- Event details with timestamps
- Pagination for large log sets

#### Ticket Transcripts
- View all closed ticket transcripts
- Search transcripts by:
  - Ticket ID
  - Username
  - Content
- Download transcript files
- View full conversation history
- Timestamp information
- User details for each message

#### Restore User Data
- Restore user data from backup files
- Select specific users to restore
- Choose data types to restore:
  - XP and levels
  - Warnings
  - Custom data
- Preview backup contents before restore
- Confirmation before restoration
- Restore progress indication

#### Logging Configuration
- Configure logging for various events:
  - Member events (join, leave, role changes)
  - Message events (edit, delete)
  - Server events (channel/role changes)
  - Moderation events (warnings, bans, kicks)
- Set log channel for each event type
- Enable/disable individual event logging
- Configure log message format
- Test logging configuration

#### Moderation Tools
- View moderation overview:
  - Total warnings
  - Total bans
  - Recent moderation actions
- Quick access to:
  - Warned users
  - Banned users
  - User management
- Moderation statistics dashboard

## Architecture

### Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Networking:** Retrofit + OkHttp
- **Async:** Kotlin Coroutines + Flow
- **Storage:** DataStore Preferences
- **Image Loading:** Coil
- **Navigation:** Compose Navigation

### Project Structure

```
app/
├── src/main/java/cc/rulekeeper/dashboard/
│   ├── data/
│   │   ├── api/
│   │   │   ├── ApiClient.kt           # Retrofit client with JWT auth
│   │   │   └── ApiServices.kt         # API service interfaces
│   │   ├── model/
│   │   │   ├── Auth.kt                # Authentication models
│   │   │   └── Guild.kt               # Guild and related models
│   │   └── repository/
│   │       ├── SettingsRepository.kt  # Settings & token storage
│   │       └── AuthRepository.kt      # Authentication logic
│   ├── ui/
│   │   ├── navigation/
│   │   │   └── Navigation.kt          # App navigation graph
│   │   ├── screens/
│   │   │   ├── InitialSetupScreen.kt  # First-time setup
│   │   │   ├── SettingsScreen.kt      # Configuration
│   │   │   ├── LoginScreen.kt         # Authentication
│   │   │   ├── GuildListScreen.kt     # Server list
│   │   │   ├── GuildDashboardScreen.kt # Feature grid
│   │   │   ├── CommandsScreen.kt      # Command management
│   │   │   ├── CommandPermissionsScreen.kt # Command role permissions
│   │   │   ├── WelcomeGoodbyeScreens.kt # Welcome/goodbye config
│   │   │   ├── BlockedWordsScreen.kt  # Blocked words
│   │   │   ├── AutoRolesScreen.kt     # Auto roles
│   │   │   ├── LevelingScreen.kt      # XP & leveling
│   │   │   ├── LeaderboardScreen.kt   # XP rankings
│   │   │   ├── SpamConfigScreen.kt    # Spam protection
│   │   │   ├── WarningsScreen.kt      # Warning system
│   │   │   ├── WarningActionsScreen.kt # Warning thresholds
│   │   │   ├── BackupsScreen.kt       # Backup management
│   │   │   ├── BackupSchedulesScreen.kt # Automated backups
│   │   │   ├── TicketsScreen.kt       # Ticket system
│   │   │   ├── TicketTranscriptsScreen.kt # Ticket history
│   │   │   ├── TwitchAnnouncementsScreen.kt # Twitch notifications
│   │   │   ├── YouTubeAnnouncementsScreen.kt # YouTube notifications
│   │   │   ├── CraftyControllerScreen.kt # Minecraft server management
│   │   │   ├── RoleMenusScreen.kt     # Interactive role menus
│   │   │   ├── ManageFormsScreen.kt   # Form builder
│   │   │   ├── GameRolesScreen.kt     # Game-based role assignment
│   │   │   ├── BirthdayManagementScreen.kt # Birthday system
│   │   │   ├── ServerLogsScreen.kt    # Server event logs
│   │   │   ├── RestoreUserDataScreen.kt # Backup restoration
│   │   │   ├── LoggingScreen.kt       # Logging configuration
│   │   │   ├── ModerationScreen.kt    # Moderation dashboard
│   │   │   ├── users/
│   │   │   │   ├── UsersListScreen.kt # User management
│   │   │   │   ├── UserDetailScreen.kt # User profile
│   │   │   │   └── ModifyXPDialog.kt  # XP modification
│   │   │   └── forms/
│   │   │       ├── FormEditScreen.kt  # Form editor
│   │   │       └── FormSubmissionsScreen.kt # View submissions
│   │   └── theme/
│   │       ├── Color.kt               # RuleKeeper color scheme
│   │       ├── Type.kt                # Typography
│   │       └── Theme.kt               # Material 3 theme
│   ├── MainActivity.kt                # Main activity with OAuth deep link
│   └── RuleKeeperApplication.kt       # Application class
└── src/main/res/
    ├── values/
    │   ├── strings.xml                # String resources
    │   ├── colors.xml                 # Color resources
    │   └── themes.xml                 # XML themes
    └── xml/
        └── network_security_config.xml # Network security
```

## REST API Integration

This app integrates with the RuleKeeper REST API. The API provides endpoints for:

- **Authentication:** `/api/v1/auth/*`
- **Guilds:** `/api/v1/guilds/*`
- **Commands:** `/api/v1/commands/*`
- **Configuration:** `/api/v1/config/*`
- **Moderation:** `/api/v1/moderation/*`
- **Twitch:** `/api/v1/twitch/*`
- **YouTube:** `/api/v1/youtube/*`
- **Crafty Controller:** `/api/v1/config/{guild_id}/crafty/*`
- **And more...**

Refer to the [RuleKeeper API documentation](https://rulekeeper.cc/api/v1/docs) for full API details.

## Development

### Deep Link Configuration

The app is configured to receive OAuth callbacks via a custom deep link scheme:

- **Scheme:** `cc.rulekeeper.dashboard`
- **Host:** `callback`
- **Full URI:** `cc.rulekeeper.dashboard://callback`

This is configured in `AndroidManifest.xml` with an intent filter on the `MainActivity`. When the backend redirects to this URI with an OAuth code, the app automatically receives it and completes the login process.

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Code Style

This project follows the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Roadmap

### Completed Features
- [x] Basic authentication (Bot Admin & Discord OAuth)
- [x] Mobile OAuth with deep link support (cc.rulekeeper.dashboard://)
- [x] Single-use OAuth code enforcement
- [x] Guild (server) list and navigation
- [x] Custom commands management
- [x] Command permissions system
- [x] Welcome/Goodbye messages with embeds
- [x] Blocked words filter
- [x] Auto roles assignment
- [x] Leveling system with rewards
- [x] XP leaderboard display
- [x] Spam protection configuration
- [x] Warnings system
- [x] Warning actions (thresholds and auto-moderation)
- [x] Manual and scheduled backups
- [x] Backup schedules management
- [x] Tickets system
- [x] Ticket transcripts viewer
- [x] Twitch stream announcements
- [x] YouTube upload announcements
- [x] Crafty Controller integration
- [x] Minecraft server management (start/stop/restart)
- [x] Role menus (buttons, dropdowns, reactions)
- [x] Forms builder and submissions viewer
- [x] Game roles automation
- [x] Birthday management system
- [x] User management and XP modification
- [x] Server logs viewer
- [x] Restore user data from backups
- [x] Logging configuration
- [x] Moderation dashboard

### Planned Features
- [ ] Real-time updates via WebSocket
- [ ] Push notifications for important events
- [ ] Multiple account support
- [ ] Offline mode with local caching
- [ ] Server statistics and analytics graphs
- [ ] Home screen widgets
- [ ] Tablet-optimized split-screen layouts
- [ ] Crafty Controller advanced features:
  - [ ] Player management
  - [ ] Console access
  - [ ] File browser
  - [ ] Scheduled tasks
- [ ] Advanced form features:
  - [ ] Conditional logic
  - [ ] File uploads
  - [ ] Payment integration
- [ ] Advanced moderation:
  - [ ] Timed bans/mutes
  - [ ] Case management
  - [ ] Appeal system
- [ ] User edit functionality (currently view-only)
- [ ] Form submission detail view
- [ ] Banned users management screen

## License

This project is licensed under the MPL-2.0 License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please:
- Open an issue on [GitHub](https://github.com/RuleKeeper-Bot/RuleKeeper-Android/issues)
- Join the [RuleKeeper Discord support server](https://rulekeeper.cc/discord)
- Check the documentation

## Acknowledgments

- Built for the [RuleKeeper Discord Bot](https://github.com/RuleKeeper-Bot/RuleKeeper-Bot)
- Uses Material Design 3 components
- Inspired by the RuleKeeper web dashboard

---

**Note:** This is a mobile companion app for the RuleKeeper Discord bot. You need to have RuleKeeper set up and running with the REST API enabled to use this app.

## Feature Summary

This Android app provides **full mobile access** to all RuleKeeper bot features, organized into 8 dashboard sections with 30+ management screens. Key highlights include:

- **OAuth Integration:** Seamless Discord login with mobile deep link support (cc.rulekeeper.dashboard://)
- **Complete Feature Parity:** All web dashboard features available on mobile
- **Real-time Management:** Start/stop Minecraft servers, manage tickets, moderate users
- **Rich Configuration:** Visual editors for embeds, forms, role menus, and announcements
- **Secure Authentication:** JWT tokens with automatic OAuth code cleanup
- **Modern UI:** Material Design 3 with dark/light theme support

The app is production-ready with all core features implemented. See the Roadmap section for planned enhancements.
