package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.Guild
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class DashboardSection(
    val title: String,
    val icon: ImageVector
) {
    SERVER_CONFIG("Server Configuration", Icons.Default.Settings),
    LEVELING("Leveling System", Icons.Default.TrendingUp),
    MODERATION("Moderation", Icons.Default.Gavel),
    FORMS("Custom Forms", Icons.Default.Assignment),
    TICKETS("Ticket System", Icons.Default.ConfirmationNumber),
    SOCIAL_PINGS("Social Pings", Icons.Default.Notifications),
    FUN_MISC("Fun/Miscellaneous", Icons.Default.EmojiEmotions),
    BACKUP("Backup/Restore", Icons.Default.Backup)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuildDashboardScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateToCommands: () -> Unit,
    onNavigateToCommandPermissions: () -> Unit,
    onNavigateToLogging: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToBans: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToGoodbye: () -> Unit,
    onNavigateToBlockedWords: () -> Unit,
    onNavigateToAutoRoles: () -> Unit,
    onNavigateToLeveling: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToWarnings: () -> Unit,
    onNavigateToWarningActions: () -> Unit,
    onNavigateToBackups: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToServerLogs: () -> Unit,
    onNavigateToRestoreUserData: () -> Unit,
    onNavigateToRoleMenus: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToManageForms: () -> Unit,
    onNavigateToTicketTranscripts: () -> Unit,
    onNavigateToTwitchAnnouncements: () -> Unit,
    onNavigateToYouTubeAnnouncements: () -> Unit,
    onNavigateToGameRoles: () -> Unit,
    onNavigateToBirthdayManagement: () -> Unit,
    onNavigateToCraftyController: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var guild by remember { mutableStateOf<Guild?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSection by remember { mutableStateOf(DashboardSection.SERVER_CONFIG) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.guildService.getGuild(guildId)
                
                if (response.isSuccessful && response.body() != null) {
                    guild = response.body()!!
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerHeader(guild?.name ?: "Loading...")
                Divider()
                
                DashboardSection.entries.forEach { section ->
                    NavigationDrawerItem(
                        icon = { Icon(section.icon, contentDescription = null) },
                        label = { Text(section.title) },
                        selected = selectedSection == section,
                        onClick = {
                            selectedSection = section
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedSection.title) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SectionContent(
                    section = selectedSection,
                    guildId = guildId,
                    guild = guild,
                    settingsRepository = settingsRepository,
                    modifier = Modifier.padding(padding),
                    onNavigateToCommands = onNavigateToCommands,
                    onNavigateToCommandPermissions = onNavigateToCommandPermissions,
                    onNavigateToLogging = onNavigateToLogging,
                    onNavigateToModeration = onNavigateToModeration,
                    onNavigateToBans = onNavigateToBans,
                    onNavigateToWelcome = onNavigateToWelcome,
                    onNavigateToGoodbye = onNavigateToGoodbye,
                    onNavigateToBlockedWords = onNavigateToBlockedWords,
                    onNavigateToAutoRoles = onNavigateToAutoRoles,
                    onNavigateToLeveling = onNavigateToLeveling,
                    onNavigateToSpam = onNavigateToSpam,
                    onNavigateToWarnings = onNavigateToWarnings,
                    onNavigateToWarningActions = onNavigateToWarningActions,
                    onNavigateToBackups = onNavigateToBackups,
                    onNavigateToTickets = onNavigateToTickets,
                    onNavigateToServerLogs = onNavigateToServerLogs,
                    onNavigateToRestoreUserData = onNavigateToRestoreUserData,
                    onNavigateToRoleMenus = onNavigateToRoleMenus,
                    onNavigateToLeaderboard = onNavigateToLeaderboard,
                    onNavigateToManageForms = onNavigateToManageForms,
                    onNavigateToTicketTranscripts = onNavigateToTicketTranscripts,
                    onNavigateToTwitchAnnouncements = onNavigateToTwitchAnnouncements,
                    onNavigateToYouTubeAnnouncements = onNavigateToYouTubeAnnouncements,
                    onNavigateToGameRoles = onNavigateToGameRoles,
                    onNavigateToBirthdayManagement = onNavigateToBirthdayManagement,
                    onNavigateToCraftyController = onNavigateToCraftyController,
                    onNavigateToUsers = onNavigateToUsers
                )
            }
        }
    }
}

@Composable
fun DrawerHeader(guildName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = guildName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionContent(
    section: DashboardSection,
    guildId: String,
    guild: Guild?,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onNavigateToCommands: () -> Unit,
    onNavigateToCommandPermissions: () -> Unit,
    onNavigateToLogging: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToBans: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToGoodbye: () -> Unit,
    onNavigateToBlockedWords: () -> Unit,
    onNavigateToAutoRoles: () -> Unit,
    onNavigateToLeveling: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToWarnings: () -> Unit,
    onNavigateToWarningActions: () -> Unit,
    onNavigateToBackups: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToServerLogs: () -> Unit,
    onNavigateToRestoreUserData: () -> Unit,
    onNavigateToRoleMenus: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToManageForms: () -> Unit,
    onNavigateToTicketTranscripts: () -> Unit,
    onNavigateToTwitchAnnouncements: () -> Unit,
    onNavigateToYouTubeAnnouncements: () -> Unit,
    onNavigateToGameRoles: () -> Unit,
    onNavigateToBirthdayManagement: () -> Unit,
    onNavigateToCraftyController: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    when (section) {
        DashboardSection.SERVER_CONFIG -> ServerConfigSection(
            modifier = modifier,
            onNavigateToLogging = onNavigateToLogging,
            onNavigateToWelcome = onNavigateToWelcome,
            onNavigateToGoodbye = onNavigateToGoodbye,
            onNavigateToAutoRoles = onNavigateToAutoRoles,
            onNavigateToCommands = onNavigateToCommands,
            onNavigateToCommandPermissions = onNavigateToCommandPermissions,
            onNavigateToBlockedWords = onNavigateToBlockedWords,
            onNavigateToSpam = onNavigateToSpam,
            onNavigateToWarningActions = onNavigateToWarningActions,
            onNavigateToServerLogs = onNavigateToServerLogs,
            onNavigateToRestoreUserData = onNavigateToRestoreUserData,
            onNavigateToRoleMenus = onNavigateToRoleMenus
        )
        DashboardSection.LEVELING -> LevelingSection(
            modifier = modifier,
            onNavigateToLeveling = onNavigateToLeveling,
            onNavigateToLeaderboard = onNavigateToLeaderboard
        )
        DashboardSection.MODERATION -> ModerationSection(
            modifier = modifier,
            onNavigateToWarnings = onNavigateToWarnings,
            onNavigateToBans = onNavigateToBans,
            onNavigateToUsers = onNavigateToUsers
        )
        DashboardSection.FORMS -> FormsSection(
            modifier = modifier,
            onNavigateToManageForms = onNavigateToManageForms
        )
        DashboardSection.TICKETS -> TicketsSection(
            modifier = modifier,
            onNavigateToTickets = onNavigateToTickets,
            onNavigateToTicketTranscripts = onNavigateToTicketTranscripts
        )
        DashboardSection.SOCIAL_PINGS -> SocialPingsSection(
            modifier = modifier,
            onNavigateToTwitchAnnouncements = onNavigateToTwitchAnnouncements,
            onNavigateToYouTubeAnnouncements = onNavigateToYouTubeAnnouncements
        )
        DashboardSection.FUN_MISC -> FunMiscSection(
            modifier = modifier,
            onNavigateToCommands = onNavigateToCommands,
            onNavigateToGameRoles = onNavigateToGameRoles,
            onNavigateToBirthdayManagement = onNavigateToBirthdayManagement,
            onNavigateToCraftyController = onNavigateToCraftyController
        )
        DashboardSection.BACKUP -> BackupSection(
            modifier = modifier,
            onNavigateToBackups = onNavigateToBackups
        )
    }
}

@Composable
fun ServerConfigSection(
    modifier: Modifier = Modifier,
    onNavigateToLogging: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToGoodbye: () -> Unit,
    onNavigateToAutoRoles: () -> Unit,
    onNavigateToCommands: () -> Unit,
    onNavigateToCommandPermissions: () -> Unit,
    onNavigateToBlockedWords: () -> Unit,
    onNavigateToSpam: () -> Unit,
    onNavigateToWarningActions: () -> Unit,
    onNavigateToServerLogs: () -> Unit,
    onNavigateToRestoreUserData: () -> Unit,
    onNavigateToRoleMenus: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Commands", Icons.Default.Terminal, onNavigateToCommands),
            SectionItem("Command Permissions", Icons.Default.Lock, onNavigateToCommandPermissions),
            SectionItem("Blocked Words", Icons.Default.Block, onNavigateToBlockedWords),
            SectionItem("Logging", Icons.Default.Description, onNavigateToLogging),
            SectionItem("Server Logs", Icons.Default.List, onNavigateToServerLogs),
            SectionItem("Welcome Messages", Icons.Default.WavingHand, onNavigateToWelcome),
            SectionItem("Goodbye Messages", Icons.Default.ExitToApp, onNavigateToGoodbye),
            SectionItem("Auto Assign Roles", Icons.Default.AutoAwesome, onNavigateToAutoRoles),
            SectionItem("Spam", Icons.Default.Shield, onNavigateToSpam),
            SectionItem("Warning Actions", Icons.Default.Gavel, onNavigateToWarningActions),
            SectionItem("Restore User Data", Icons.Default.Restore, onNavigateToRestoreUserData),
            SectionItem("Role Menus", Icons.Default.Menu, onNavigateToRoleMenus)
        )
    )
}

@Composable
fun LevelingSection(
    modifier: Modifier = Modifier,
    onNavigateToLeveling: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Leveling", Icons.Default.TrendingUp, onNavigateToLeveling),
            SectionItem("Leaderboard", Icons.Default.EmojiEvents, onNavigateToLeaderboard)
        )
    )
}

@Composable
fun ModerationSection(
    modifier: Modifier = Modifier,
    onNavigateToWarnings: () -> Unit,
    onNavigateToBans: () -> Unit,
    onNavigateToUsers: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Users", Icons.Default.People, onNavigateToUsers),
            SectionItem("Warned Users", Icons.Default.Warning, onNavigateToWarnings),
            SectionItem("Banned Users", Icons.Default.Block, onNavigateToBans)
        )
    )
}

@Composable
fun FormsSection(
    modifier: Modifier = Modifier,
    onNavigateToManageForms: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Manage Forms", Icons.Default.Assignment, onNavigateToManageForms)
        )
    )
}

@Composable
fun TicketsSection(
    modifier: Modifier = Modifier,
    onNavigateToTickets: () -> Unit,
    onNavigateToTicketTranscripts: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Ticket Menus", Icons.Default.ConfirmationNumber, onNavigateToTickets),
            SectionItem("Ticket Transcripts", Icons.Default.Description, onNavigateToTicketTranscripts)
        )
    )
}

@Composable
fun SocialPingsSection(
    modifier: Modifier = Modifier,
    onNavigateToTwitchAnnouncements: () -> Unit,
    onNavigateToYouTubeAnnouncements: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Twitch Announcements", Icons.Default.Notifications, onNavigateToTwitchAnnouncements),
            SectionItem("YouTube Announcements", Icons.Default.VideoLibrary, onNavigateToYouTubeAnnouncements)
        )
    )
}

@Composable
fun FunMiscSection(
    modifier: Modifier = Modifier,
    onNavigateToCommands: () -> Unit,
    onNavigateToGameRoles: () -> Unit,
    onNavigateToBirthdayManagement: () -> Unit,
    onNavigateToCraftyController: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Game Roles", Icons.Default.SportsEsports, onNavigateToGameRoles),
            SectionItem("Birthday Management", Icons.Default.Cake, onNavigateToBirthdayManagement),
            SectionItem("Crafty Controller", Icons.Default.CloudQueue, onNavigateToCraftyController)
        )
    )
}

@Composable
fun BackupSection(
    modifier: Modifier = Modifier,
    onNavigateToBackups: () -> Unit
) {
    SectionList(
        modifier = modifier,
        items = listOf(
            SectionItem("Backup and Restore", Icons.Default.Backup, onNavigateToBackups)
        )
    )
}

data class SectionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun SectionList(
    modifier: Modifier = Modifier,
    items: List<SectionItem>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            SectionCard(
                title = item.title,
                icon = item.icon,
                onClick = item.onClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
