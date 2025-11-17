package cc.rulekeeper.dashboard.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.screens.*

sealed class Screen(val route: String) {
    object InitialSetup : Screen("initial_setup")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object GuildList : Screen("guild_list")
    object GuildDashboard : Screen("guild_dashboard/{guildId}") {
        fun createRoute(guildId: String) = "guild_dashboard/$guildId"
    }
    object Commands : Screen("commands/{guildId}") {
        fun createRoute(guildId: String) = "commands/$guildId"
    }
    object CommandPermissions : Screen("command_permissions/{guildId}") {
        fun createRoute(guildId: String) = "command_permissions/$guildId"
    }
    object Logging : Screen("logging/{guildId}") {
        fun createRoute(guildId: String) = "logging/$guildId"
    }
    object Moderation : Screen("moderation/{guildId}") {
        fun createRoute(guildId: String) = "moderation/$guildId"
    }
    object Bans : Screen("bans/{guildId}") {
        fun createRoute(guildId: String) = "bans/$guildId"
    }
    object Welcome : Screen("welcome/{guildId}") {
        fun createRoute(guildId: String) = "welcome/$guildId"
    }
    object Goodbye : Screen("goodbye/{guildId}") {
        fun createRoute(guildId: String) = "goodbye/$guildId"
    }
    object BlockedWords : Screen("blocked_words/{guildId}") {
        fun createRoute(guildId: String) = "blocked_words/$guildId"
    }
    object AutoRoles : Screen("auto_roles/{guildId}") {
        fun createRoute(guildId: String) = "auto_roles/$guildId"
    }
    object Leveling : Screen("leveling/{guildId}") {
        fun createRoute(guildId: String) = "leveling/$guildId"
    }
    object Spam : Screen("spam/{guildId}") {
        fun createRoute(guildId: String) = "spam/$guildId"
    }
    object Warnings : Screen("warnings/{guildId}") {
        fun createRoute(guildId: String) = "warnings/$guildId"
    }
    object WarningActions : Screen("warning_actions/{guildId}") {
        fun createRoute(guildId: String) = "warning_actions/$guildId"
    }
    object Backups : Screen("backups/{guildId}") {
        fun createRoute(guildId: String) = "backups/$guildId"
    }
    object Tickets : Screen("tickets/{guildId}") {
        fun createRoute(guildId: String) = "tickets/$guildId"
    }
    object ServerLogs : Screen("server_logs/{guildId}") {
        fun createRoute(guildId: String) = "server_logs/$guildId"
    }
    object RestoreUserData : Screen("restore_user_data/{guildId}") {
        fun createRoute(guildId: String) = "restore_user_data/$guildId"
    }
    object RoleMenus : Screen("role_menus/{guildId}") {
        fun createRoute(guildId: String) = "role_menus/$guildId"
    }
    object Leaderboard : Screen("leaderboard/{guildId}") {
        fun createRoute(guildId: String) = "leaderboard/$guildId"
    }
    object ManageForms : Screen("manage_forms/{guildId}") {
        fun createRoute(guildId: String) = "manage_forms/$guildId"
    }
    object TicketTranscripts : Screen("ticket_transcripts/{guildId}") {
        fun createRoute(guildId: String) = "ticket_transcripts/$guildId"
    }
    object TwitchAnnouncements : Screen("twitch_announcements/{guildId}") {
        fun createRoute(guildId: String) = "twitch_announcements/$guildId"
    }
    object YouTubeAnnouncements : Screen("youtube_announcements/{guildId}") {
        fun createRoute(guildId: String) = "youtube_announcements/$guildId"
    }
    object GameRoles : Screen("game_roles/{guildId}") {
        fun createRoute(guildId: String) = "game_roles/$guildId"
    }
    object BirthdayManagement : Screen("birthday_management/{guildId}") {
        fun createRoute(guildId: String) = "birthday_management/$guildId"
    }
    object CraftyController : Screen("crafty_controller/{guildId}") {
        fun createRoute(guildId: String) = "crafty_controller/$guildId"
    }
    object BackupSchedules : Screen("backup_schedules/{guildId}") {
        fun createRoute(guildId: String) = "backup_schedules/$guildId"
    }
    object Users : Screen("users/{guildId}") {
        fun createRoute(guildId: String) = "users/$guildId"
    }
    object UserDetail : Screen("users/{guildId}/{userId}") {
        fun createRoute(guildId: String, userId: String) = "users/$guildId/$userId"
    }
    object UserEdit : Screen("users/{guildId}/{userId}/edit") {
        fun createRoute(guildId: String, userId: String) = "users/$guildId/$userId/edit"
    }
    object FormEdit : Screen("forms/{guildId}/{formId}/edit") {
        fun createRoute(guildId: String, formId: String) = "forms/$guildId/$formId/edit"
    }
    object FormSubmissions : Screen("forms/{guildId}/{formId}/submissions") {
        fun createRoute(guildId: String, formId: String) = "forms/$guildId/$formId/submissions"
    }
    object FormSubmissionDetail : Screen("forms/{guildId}/{formId}/submissions/{submissionId}") {
        fun createRoute(guildId: String, formId: String, submissionId: String) = "forms/$guildId/$formId/submissions/$submissionId"
    }
}

@Composable
fun RuleKeeperNavigation(
    settingsRepository: SettingsRepository,
    oauthCode: String? = null,
    onOauthCodeProcessed: () -> Unit = {},
    navController: NavHostController = rememberNavController()
) {
    val accessToken by settingsRepository.accessToken.collectAsState(initial = null)
    
    // Determine start destination based on authentication state
    val startDestination = if (accessToken != null) {
        Screen.GuildList.route
    } else {
        Screen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.InitialSetup.route) {
            InitialSetupScreen(
                settingsRepository = settingsRepository,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.InitialSetup.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                settingsRepository = settingsRepository,
                oauthCode = oauthCode,
                onOauthCodeProcessed = onOauthCodeProcessed,
                onLoginSuccess = {
                    navController.navigate(Screen.GuildList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.InitialSetup.route)
                }
            )
        }
        
        composable(Screen.GuildList.route) {
            GuildListScreen(
                settingsRepository = settingsRepository,
                onGuildSelected = { guildId ->
                    navController.navigate(Screen.GuildDashboard.createRoute(guildId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.GuildDashboard.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            GuildDashboardScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateToCommands = {
                    navController.navigate(Screen.Commands.createRoute(guildId))
                },
                onNavigateToCommandPermissions = {
                    navController.navigate(Screen.CommandPermissions.createRoute(guildId))
                },
                onNavigateToLogging = {
                    navController.navigate(Screen.Logging.createRoute(guildId))
                },
                onNavigateToModeration = {
                    navController.navigate(Screen.Moderation.createRoute(guildId))
                },
                onNavigateToBans = {
                    navController.navigate(Screen.Bans.createRoute(guildId))
                },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.createRoute(guildId))
                },
                onNavigateToGoodbye = {
                    navController.navigate(Screen.Goodbye.createRoute(guildId))
                },
                onNavigateToBlockedWords = {
                    navController.navigate(Screen.BlockedWords.createRoute(guildId))
                },
                onNavigateToAutoRoles = {
                    navController.navigate(Screen.AutoRoles.createRoute(guildId))
                },
                onNavigateToLeveling = {
                    navController.navigate(Screen.Leveling.createRoute(guildId))
                },
                onNavigateToSpam = {
                    navController.navigate(Screen.Spam.createRoute(guildId))
                },
                onNavigateToWarnings = {
                    navController.navigate(Screen.Warnings.createRoute(guildId))
                },
                onNavigateToWarningActions = {
                    navController.navigate(Screen.WarningActions.createRoute(guildId))
                },
                onNavigateToBackups = {
                    navController.navigate(Screen.Backups.createRoute(guildId))
                },
                onNavigateToTickets = {
                    navController.navigate(Screen.Tickets.createRoute(guildId))
                },
                onNavigateToServerLogs = {
                    navController.navigate(Screen.ServerLogs.createRoute(guildId))
                },
                onNavigateToRestoreUserData = {
                    navController.navigate(Screen.RestoreUserData.createRoute(guildId))
                },
                onNavigateToRoleMenus = {
                    navController.navigate(Screen.RoleMenus.createRoute(guildId))
                },
                onNavigateToLeaderboard = {
                    navController.navigate(Screen.Leaderboard.createRoute(guildId))
                },
                onNavigateToManageForms = {
                    navController.navigate(Screen.ManageForms.createRoute(guildId))
                },
                onNavigateToTicketTranscripts = {
                    navController.navigate(Screen.TicketTranscripts.createRoute(guildId))
                },
                onNavigateToTwitchAnnouncements = {
                    navController.navigate(Screen.TwitchAnnouncements.createRoute(guildId))
                },
                onNavigateToYouTubeAnnouncements = {
                    navController.navigate(Screen.YouTubeAnnouncements.createRoute(guildId))
                },
                onNavigateToGameRoles = {
                    navController.navigate(Screen.GameRoles.createRoute(guildId))
                },
                onNavigateToBirthdayManagement = {
                    navController.navigate(Screen.BirthdayManagement.createRoute(guildId))
                },
                onNavigateToCraftyController = {
                    navController.navigate(Screen.CraftyController.createRoute(guildId))
                },
                onNavigateToUsers = {
                    navController.navigate(Screen.Users.createRoute(guildId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Commands.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            CommandsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CommandPermissions.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            CommandPermissionsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Logging.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            LoggingScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Moderation.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            ModerationScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Bans.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            BansScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Welcome.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            WelcomeConfigScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Goodbye.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            GoodbyeConfigScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BlockedWords.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            BlockedWordsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AutoRoles.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            AutoRolesScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Leveling.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            LevelingScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Spam.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            SpamConfigScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Warnings.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            WarningsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.WarningActions.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            WarningActionsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Backups.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            BackupsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSchedules = {
                    navController.navigate(Screen.BackupSchedules.createRoute(guildId))
                }
            )
        }
        
        composable(Screen.BackupSchedules.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            BackupSchedulesScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Tickets.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            TicketsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ServerLogs.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            ServerLogsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RestoreUserData.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            RestoreUserDataScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RoleMenus.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            RoleMenusScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Leaderboard.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            LeaderboardScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ManageForms.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            ManageFormsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditForm = { formId ->
                    navController.navigate(Screen.FormEdit.createRoute(guildId, formId))
                },
                onNavigateToFormSubmissions = { formId ->
                    navController.navigate(Screen.FormSubmissions.createRoute(guildId, formId))
                }
            )
        }
        
        composable(Screen.TicketTranscripts.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            TicketTranscriptsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TwitchAnnouncements.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            TwitchAnnouncementsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.YouTubeAnnouncements.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            YouTubeAnnouncementsScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.GameRoles.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            GameRolesScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.BirthdayManagement.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            BirthdayManagementScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.CraftyController.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            CraftyControllerScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Users.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            cc.rulekeeper.dashboard.ui.screens.users.UsersListScreen(
                guildId = guildId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(guildId, userId))
                }
            )
        }
        
        composable(Screen.UserDetail.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            cc.rulekeeper.dashboard.ui.screens.users.UserDetailScreen(
                guildId = guildId,
                userId = userId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = {
                    navController.navigate(Screen.UserEdit.createRoute(guildId, userId))
                }
            )
        }
        
        composable(Screen.FormEdit.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            val formId = backStackEntry.arguments?.getString("formId") ?: return@composable
            cc.rulekeeper.dashboard.ui.screens.forms.FormEditScreen(
                guildId = guildId,
                formId = formId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.FormSubmissions.route) { backStackEntry ->
            val guildId = backStackEntry.arguments?.getString("guildId") ?: return@composable
            val formId = backStackEntry.arguments?.getString("formId") ?: return@composable
            cc.rulekeeper.dashboard.ui.screens.forms.FormSubmissionsScreen(
                guildId = guildId,
                formId = formId,
                settingsRepository = settingsRepository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSubmissionDetail = { submissionId ->
                    navController.navigate(Screen.FormSubmissionDetail.createRoute(guildId, formId, submissionId))
                }
            )
        }
    }
}
