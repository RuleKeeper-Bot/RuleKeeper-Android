package cc.rulekeeper.dashboard.data.api

import cc.rulekeeper.dashboard.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/discord/callback")
    suspend fun discordCallback(@Body request: DiscordCallbackRequest): Response<LoginResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<LoginResponse>
    
    @GET("auth/verify")
    suspend fun verifyToken(): Response<Map<String, Any>>
    
    @POST("auth/logout")
    suspend fun logout(): Response<Map<String, String>>
}

interface GuildService {
    @GET("guilds")
    suspend fun getGuilds(): Response<GuildListResponse>
    
    @GET("guilds/{guild_id}")
    suspend fun getGuild(@Path("guild_id") guildId: String): Response<Guild>
    
    @GET("guilds/{guild_id}/channels")
    suspend fun getChannels(@Path("guild_id") guildId: String): Response<List<Channel>>
    
    @GET("guilds/{guild_id}/channels")
    suspend fun getGuildChannels(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @GET("guilds/{guild_id}/roles")
    suspend fun getRoles(@Path("guild_id") guildId: String): Response<List<Role>>
    
    @GET("guilds/{guild_id}/roles")
    suspend fun getGuildRoles(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @GET("guilds/{guild_id}/settings")
    suspend fun getGuildSettings(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @PUT("guilds/{guild_id}/settings")
    suspend fun updateGuildSettings(
        @Path("guild_id") guildId: String,
        @Body settings: Map<String, Any>
    ): Response<Map<String, Any>>
}

interface CommandService {
    @GET("commands/{guild_id}")
    suspend fun getCommands(
        @Path("guild_id") guildId: String,
        @Query("include_builtin") includeBuiltin: Boolean = false
    ): Response<List<GuildCommand>>
    
    @POST("commands/{guild_id}")
    suspend fun createCommand(
        @Path("guild_id") guildId: String,
        @Body request: CreateCommandRequest
    ): Response<Map<String, Any>>
    
    @PUT("commands/{guild_id}/{command_name}")
    suspend fun updateCommand(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String,
        @Body request: CreateCommandRequest
    ): Response<Map<String, Any>>
    
    @DELETE("commands/{guild_id}/{command_name}")
    suspend fun deleteCommand(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String
    ): Response<Map<String, Any>>
    
    @POST("commands/{guild_id}/sync")
    suspend fun syncCommands(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @GET("commands/{guild_id}/export")
    suspend fun exportCommands(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("commands/{guild_id}/import")
    suspend fun importCommands(
        @Path("guild_id") guildId: String,
        @Body data: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @POST("commands/{guild_id}/delete-all")
    suspend fun deleteAllCommands(@Path("guild_id") guildId: String): Response<Map<String, Any>>
}

interface ConfigService {
    @GET("config/{guild_id}/logging")
    suspend fun getLogConfig(@Path("guild_id") guildId: String): Response<LogConfig>
    
    @PUT("config/{guild_id}/logging")
    suspend fun updateLogConfig(
        @Path("guild_id") guildId: String,
        @Body config: LogConfig
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/welcome")
    suspend fun getWelcomeConfig(@Path("guild_id") guildId: String): Response<WelcomeConfig>
    
    @PUT("config/{guild_id}/welcome")
    suspend fun updateWelcomeConfig(
        @Path("guild_id") guildId: String,
        @Body config: WelcomeConfig
    ): Response<WelcomeConfig>
    
    @GET("config/{guild_id}/goodbye")
    suspend fun getGoodbyeConfig(@Path("guild_id") guildId: String): Response<GoodbyeConfig>
    
    @PUT("config/{guild_id}/goodbye")
    suspend fun updateGoodbyeConfig(
        @Path("guild_id") guildId: String,
        @Body config: GoodbyeConfig
    ): Response<GoodbyeConfig>
    
    @GET("config/{guild_id}/spam")
    suspend fun getSpamConfig(@Path("guild_id") guildId: String): Response<SpamConfig>
    
    @PUT("config/{guild_id}/spam")
    suspend fun updateSpamConfig(
        @Path("guild_id") guildId: String,
        @Body config: SpamConfig
    ): Response<SpamConfig>
    
    @GET("config/{guild_id}/leveling")
    suspend fun getLevelConfig(@Path("guild_id") guildId: String): Response<LevelConfig>
    
    @PUT("config/{guild_id}/leveling")
    suspend fun updateLevelConfig(
        @Path("guild_id") guildId: String,
        @Body config: LevelConfig
    ): Response<LevelConfig>
    
    @GET("config/{guild_id}/birthdays")
    suspend fun getBirthdaysConfig(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @PUT("config/{guild_id}/birthdays")
    suspend fun updateBirthdaysConfig(
        @Path("guild_id") guildId: String,
        @Body config: Map<String, String>
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/twitch")
    suspend fun getTwitchConfig(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @PUT("config/{guild_id}/twitch")
    suspend fun updateTwitchConfig(
        @Path("guild_id") guildId: String,
        @Body config: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/youtube")
    suspend fun getYouTubeConfig(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @PUT("config/{guild_id}/youtube")
    suspend fun updateYouTubeConfig(
        @Path("guild_id") guildId: String,
        @Body config: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/crafty")
    suspend fun getCraftyConfig(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("config/{guild_id}/crafty")
    suspend fun addCraftyInstance(
        @Path("guild_id") guildId: String,
        @Body instance: Map<String, String>
    ): Response<Map<String, Any>>
    
    @PUT("config/{guild_id}/crafty/{instance_id}")
    suspend fun updateCraftyInstance(
        @Path("guild_id") guildId: String,
        @Path("instance_id") instanceId: Int,
        @Body instance: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("config/{guild_id}/crafty/{instance_id}")
    suspend fun deleteCraftyInstance(
        @Path("guild_id") guildId: String,
        @Path("instance_id") instanceId: Int
    ): Response<Map<String, Any>>
    
    @POST("config/{guild_id}/crafty/{instance_id}/test")
    suspend fun testCraftyInstance(
        @Path("guild_id") guildId: String,
        @Path("instance_id") instanceId: Int
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/crafty/servers")
    suspend fun getCraftyServers(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("config/{guild_id}/crafty/servers/{server_id}/{action}")
    suspend fun performServerAction(
        @Path("guild_id") guildId: String,
        @Path("server_id") serverId: Int,
        @Path("action") action: String
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/level-rewards")
    suspend fun getLevelRewards(@Path("guild_id") guildId: String): Response<LevelRewardsResponse>
    
    @POST("config/{guild_id}/level-rewards")
    suspend fun addLevelReward(
        @Path("guild_id") guildId: String,
        @Body reward: AddLevelRewardRequest
    ): Response<Map<String, Any>>
    
    @DELETE("config/{guild_id}/level-rewards/{level}")
    suspend fun deleteLevelReward(
        @Path("guild_id") guildId: String,
        @Path("level") level: Int
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/warning-actions")
    suspend fun getWarningActions(@Path("guild_id") guildId: String): Response<WarningActionsResponse>
    
    @PUT("config/{guild_id}/warning-actions")
    suspend fun updateWarningActions(
        @Path("guild_id") guildId: String,
        @Body update: WarningActionsUpdate
    ): Response<Map<String, Any>>
    
    @GET("config/{guild_id}/restore-settings")
    suspend fun getRestoreSettings(@Path("guild_id") guildId: String): Response<RestoreSettings>
    
    @PUT("config/{guild_id}/restore-settings")
    suspend fun updateRestoreSettings(
        @Path("guild_id") guildId: String,
        @Body settings: RestoreSettings
    ): Response<Map<String, Any>>
}

interface TwitchService {
    @GET("twitch/{guild_id}/announcements")
    suspend fun getAnnouncements(@Path("guild_id") guildId: String): Response<TwitchAnnouncementsResponse>
    
    @POST("twitch/{guild_id}/announcements")
    suspend fun addAnnouncement(
        @Path("guild_id") guildId: String,
        @Body request: AddTwitchAnnouncementRequest
    ): Response<Map<String, Any>>
    
    @PUT("twitch/{guild_id}/announcements/{announcement_id}")
    suspend fun updateAnnouncement(
        @Path("guild_id") guildId: String,
        @Path("announcement_id") announcementId: Int,
        @Body request: UpdateTwitchAnnouncementRequest
    ): Response<Map<String, Any>>
    
    @DELETE("twitch/{guild_id}/announcements/{announcement_id}")
    suspend fun deleteAnnouncement(
        @Path("guild_id") guildId: String,
        @Path("announcement_id") announcementId: Int
    ): Response<Map<String, Any>>
}

interface YouTubeService {
    @GET("youtube/{guild_id}/announcements")
    suspend fun getAnnouncements(@Path("guild_id") guildId: String): Response<YouTubeAnnouncementsResponse>
    
    @POST("youtube/{guild_id}/announcements")
    suspend fun addAnnouncement(
        @Path("guild_id") guildId: String,
        @Body request: AddYouTubeAnnouncementRequest
    ): Response<Map<String, Any>>
    
    @PUT("youtube/{guild_id}/announcements/{announcement_id}")
    suspend fun updateAnnouncement(
        @Path("guild_id") guildId: String,
        @Path("announcement_id") announcementId: Int,
        @Body request: UpdateYouTubeAnnouncementRequest
    ): Response<Map<String, Any>>
    
    @DELETE("youtube/{guild_id}/announcements/{announcement_id}")
    suspend fun deleteAnnouncement(
        @Path("guild_id") guildId: String,
        @Path("announcement_id") announcementId: Int
    ): Response<Map<String, Any>>
}

interface ModerationService {
    @GET("moderation/{guild_id}/warnings")
    suspend fun getWarnings(@Path("guild_id") guildId: String): Response<List<Warning>>
    
    @GET("moderation/{guild_id}/warnings/{user_id}")
    suspend fun getUserWarnings(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String
    ): Response<List<Warning>>
    
    @POST("moderation/{guild_id}/warnings/{user_id}")
    suspend fun addWarning(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    @DELETE("moderation/{guild_id}/warnings/{warning_id}")
    suspend fun deleteWarning(
        @Path("guild_id") guildId: String,
        @Path("warning_id") warningId: String
    ): Response<Map<String, Any>>
    
    @GET("moderation/{guild_id}/bans")
    suspend fun getBans(@Path("guild_id") guildId: String): Response<BansResponse>
    
    @GET("moderation/{guild_id}/blocked-words")
    suspend fun getBlockedWords(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("moderation/{guild_id}/blocked-words")
    suspend fun addBlockedWord(
        @Path("guild_id") guildId: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    @DELETE("moderation/{guild_id}/blocked-words/{word_id}")
    suspend fun deleteBlockedWord(
        @Path("guild_id") guildId: String,
        @Path("word_id") wordId: Int
    ): Response<Map<String, Any>>
}

interface BackupsService {
    @GET("backups/{guild_id}")
    suspend fun getBackups(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("backups/{guild_id}")
    suspend fun createBackup(
        @Path("guild_id") guildId: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    @GET("backups/{guild_id}/{backup_id}")
    suspend fun getBackup(
        @Path("guild_id") guildId: String,
        @Path("backup_id") backupId: String
    ): Response<Map<String, Any>>
    
    @DELETE("backups/{guild_id}/{backup_id}")
    suspend fun deleteBackup(
        @Path("guild_id") guildId: String,
        @Path("backup_id") backupId: String
    ): Response<Map<String, Any>>
    
    @GET("backups/{guild_id}/{backup_id}/download")
    suspend fun downloadBackup(
        @Path("guild_id") guildId: String,
        @Path("backup_id") backupId: String
    ): Response<Map<String, Any>>
    
    @POST("backups/{guild_id}/{backup_id}/restore")
    suspend fun restoreBackup(
        @Path("guild_id") guildId: String,
        @Path("backup_id") backupId: String
    ): Response<Map<String, Any>>
    
    @POST("backups/{guild_id}/{backup_id}/share")
    suspend fun shareBackup(
        @Path("guild_id") guildId: String,
        @Path("backup_id") backupId: String
    ): Response<Map<String, Any>>
    
    @GET("backups/{guild_id}/schedules")
    suspend fun getBackupSchedules(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("backups/{guild_id}/schedules")
    suspend fun createBackupSchedule(
        @Path("guild_id") guildId: String,
        @Body schedule: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @PUT("backups/{guild_id}/schedules/{schedule_id}")
    suspend fun updateBackupSchedule(
        @Path("guild_id") guildId: String,
        @Path("schedule_id") scheduleId: Int,
        @Body schedule: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("backups/{guild_id}/schedules/{schedule_id}")
    suspend fun deleteBackupSchedule(
        @Path("guild_id") guildId: String,
        @Path("schedule_id") scheduleId: Int
    ): Response<Map<String, Any>>
}

interface TicketsService {
    @GET("tickets/{guild_id}/menus")
    suspend fun getTicketMenus(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("tickets/{guild_id}/menus")
    suspend fun createTicketMenu(
        @Path("guild_id") guildId: String,
        @Body menu: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("tickets/{guild_id}/menus/{menu_id}")
    suspend fun getTicketMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: Int
    ): Response<Map<String, Any>>
    
    @PUT("tickets/{guild_id}/menus/{menu_id}")
    suspend fun updateTicketMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: Int,
        @Body menu: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("tickets/{guild_id}/menus/{menu_id}")
    suspend fun deleteTicketMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: Int
    ): Response<Map<String, Any>>
    
    @GET("tickets/{guild_id}/tickets")
    suspend fun getTickets(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @GET("tickets/{guild_id}/transcripts")
    suspend fun getTranscripts(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @GET("tickets/{guild_id}/transcripts/{transcript_id}")
    suspend fun getTranscript(
        @Path("guild_id") guildId: String,
        @Path("transcript_id") transcriptId: Int
    ): Response<Map<String, Any>>
}

interface RoleService {
    @GET("roles/{guild_id}/auto-roles")
    suspend fun getAutoRoles(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("roles/{guild_id}/auto-roles")
    suspend fun createAutoRole(
        @Path("guild_id") guildId: String,
        @Body request: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("roles/{guild_id}/auto-roles/{role_id}")
    suspend fun deleteAutoRole(
        @Path("guild_id") guildId: String,
        @Path("role_id") roleId: String
    ): Response<Map<String, Any>>
    
    @GET("roles/{guild_id}/game-roles")
    suspend fun getGameRoles(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("roles/{guild_id}/game-roles")
    suspend fun createGameRole(
        @Path("guild_id") guildId: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    @PUT("roles/{guild_id}/game-roles/{game_role_id}")
    suspend fun updateGameRole(
        @Path("guild_id") guildId: String,
        @Path("game_role_id") gameRoleId: Int,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
    
    @DELETE("roles/{guild_id}/game-roles/{game_role_id}")
    suspend fun deleteGameRole(
        @Path("guild_id") guildId: String,
        @Path("game_role_id") gameRoleId: Int
    ): Response<Map<String, Any>>
}

interface FormsService {
    @GET("forms/{guild_id}/forms")
    suspend fun getForms(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("forms/{guild_id}/forms")
    suspend fun createForm(
        @Path("guild_id") guildId: String,
        @Body form: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("forms/{guild_id}/forms/{form_id}")
    suspend fun getForm(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String
    ): Response<Map<String, Any>>
    
    @PUT("forms/{guild_id}/forms/{form_id}")
    suspend fun updateForm(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String,
        @Body form: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("forms/{guild_id}/forms/{form_id}")
    suspend fun deleteForm(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String
    ): Response<Map<String, Any>>
    
    @GET("forms/{guild_id}/forms/{form_id}/submissions")
    suspend fun getFormSubmissions(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String
    ): Response<List<Map<String, Any>>>
    
    @GET("forms/{guild_id}/forms/{form_id}/submissions/{submission_id}")
    suspend fun getSubmission(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String,
        @Path("submission_id") submissionId: String
    ): Response<Map<String, Any>>
    
    @DELETE("forms/{guild_id}/forms/{form_id}/submissions/{submission_id}")
    suspend fun deleteSubmission(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String,
        @Path("submission_id") submissionId: String
    ): Response<Map<String, Any>>
    
    @GET("forms/{guild_id}/forms/{form_id}/export")
    suspend fun exportSubmissions(
        @Path("guild_id") guildId: String,
        @Path("form_id") formId: String
    ): Response<Map<String, Any>>
}

interface LogsService {
    @GET("logs/{guild_id}/logs")
    suspend fun getServerLogs(
        @Path("guild_id") guildId: String,
        @QueryMap params: Map<String, String>
    ): Response<Map<String, Any>>
    
    @GET("logs/{guild_id}/logs/types")
    suspend fun getLogTypes(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @GET("logs/{guild_id}/logs/{log_id}")
    suspend fun getLogDetail(
        @Path("guild_id") guildId: String,
        @Path("log_id") logId: String
    ): Response<Map<String, Any>>
    
    @GET("logs/{guild_id}/logs/stats")
    suspend fun getLogStats(
        @Path("guild_id") guildId: String,
        @Query("days") days: Int = 7
    ): Response<Map<String, Any>>
    
    @GET("logs/{guild_id}/logs/export")
    suspend fun exportLogs(
        @Path("guild_id") guildId: String,
        @QueryMap params: Map<String, String>
    ): Response<Map<String, Any>>
}

interface RoleMenuService {
    @GET("roles/{guild_id}/role-menus")
    suspend fun getRoleMenus(@Path("guild_id") guildId: String): Response<List<RoleMenu>>
    
    @GET("roles/{guild_id}/role-menus/{menu_id}")
    suspend fun getRoleMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: String
    ): Response<RoleMenu>
    
    @POST("roles/{guild_id}/role-menus")
    suspend fun createRoleMenu(
        @Path("guild_id") guildId: String,
        @Body menu: RoleMenuCreate
    ): Response<Map<String, Any>>
    
    @PUT("roles/{guild_id}/role-menus/{menu_id}")
    suspend fun updateRoleMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: String,
        @Body update: RoleMenuUpdate
    ): Response<Map<String, Any>>
    
    @DELETE("roles/{guild_id}/role-menus/{menu_id}")
    suspend fun deleteRoleMenu(
        @Path("guild_id") guildId: String,
        @Path("menu_id") menuId: String
    ): Response<Map<String, Any>>
}

interface LeaderboardService {
    @GET("config/{guild_id}/leaderboard")
    suspend fun getLeaderboard(
        @Path("guild_id") guildId: String,
        @Query("limit") limit: Int = 100
    ): Response<LeaderboardResponse>
}

interface UsersService {
    @GET("users/{guild_id}/users")
    suspend fun getUsers(
        @Path("guild_id") guildId: String,
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<Map<String, Any>>
    
    @GET("users/{guild_id}/users/{user_id}")
    suspend fun getUser(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String
    ): Response<Map<String, Any>>
    
    @PUT("users/{guild_id}/users/{user_id}")
    suspend fun updateUser(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String,
        @Body update: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("users/{guild_id}/users/{user_id}")
    suspend fun deleteUserData(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String
    ): Response<Map<String, Any>>
    
    @POST("users/{guild_id}/users/{user_id}/xp")
    suspend fun modifyXP(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String,
        @Body update: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("users/{guild_id}/birthdays")
    suspend fun getBirthdays(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @POST("users/{guild_id}/birthdays")
    suspend fun setBirthday(
        @Path("guild_id") guildId: String,
        @Body birthday: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("users/{guild_id}/birthdays/{user_id}")
    suspend fun deleteBirthday(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String
    ): Response<Map<String, Any>>
    
    @POST("users/{guild_id}/users/{user_id}/restore")
    suspend fun restoreUserData(
        @Path("guild_id") guildId: String,
        @Path("user_id") userId: String,
        @Body data: Map<String, Any>
    ): Response<Map<String, Any>>
}

interface PermissionsService {
    @GET("permissions/{guild_id}/permissions")
    suspend fun getPermissions(@Path("guild_id") guildId: String): Response<List<Map<String, Any>>>
    
    @GET("permissions/{guild_id}/permissions/{command_name}")
    suspend fun getPermission(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String
    ): Response<Map<String, Any>>
    
    @PUT("permissions/{guild_id}/permissions/{command_name}")
    suspend fun updatePermission(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String,
        @Body permission: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @DELETE("permissions/{guild_id}/permissions/{command_name}")
    suspend fun resetPermission(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String
    ): Response<Map<String, Any>>
    
    @POST("permissions/{guild_id}/permissions/{command_name}/roles")
    suspend fun addAllowedRole(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String,
        @Body role: Map<String, String>
    ): Response<Map<String, Any>>
    
    @POST("permissions/{guild_id}/permissions/{command_name}/channels")
    suspend fun addChannelPermission(
        @Path("guild_id") guildId: String,
        @Path("command_name") commandName: String,
        @Body channel: Map<String, String>
    ): Response<Map<String, Any>>
    
    @POST("permissions/{guild_id}/permissions/bulk")
    suspend fun bulkUpdatePermissions(
        @Path("guild_id") guildId: String,
        @Body permissions: Map<String, Any>
    ): Response<Map<String, Any>>
    
    @GET("permissions/{guild_id}/permissions/export")
    suspend fun exportPermissions(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("permissions/{guild_id}/permissions/import")
    suspend fun importPermissions(
        @Path("guild_id") guildId: String,
        @Body data: Map<String, Any>
    ): Response<Map<String, Any>>
}

interface SettingsService {
    // MFA Endpoints
    @GET("settings/mfa/status")
    suspend fun getMfaStatus(): Response<Map<String, Any>>
    
    @POST("settings/mfa/totp/enable")
    suspend fun enableTotp(): Response<Map<String, Any>>
    
    @POST("settings/mfa/totp/disable")
    suspend fun disableTotp(@Body code: Map<String, String>): Response<Map<String, Any>>
    
    @POST("settings/mfa/email/enable")
    suspend fun enableEmailMfa(@Body email: Map<String, String>): Response<Map<String, Any>>
    
    @POST("settings/mfa/email/disable")
    suspend fun disableEmailMfa(@Body code: Map<String, String>): Response<Map<String, Any>>
    
    @POST("settings/mfa/require-both")
    suspend fun setRequireBoth(@Body enabled: Map<String, Boolean>): Response<Map<String, Any>>
    
    @POST("settings/mfa/disable-all")
    suspend fun disableAllMfa(): Response<Map<String, Any>>
    
    // Announcement Configuration
    @GET("settings/{guild_id}/announcements")
    suspend fun getAnnouncementConfig(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @POST("settings/{guild_id}/announcements")
    suspend fun updateAnnouncementConfig(
        @Path("guild_id") guildId: String,
        @Body config: Map<String, Any>
    ): Response<Map<String, Any>>
    
    // Import/Export
    @POST("{guild_id}/export")
    suspend fun exportData(
        @Path("guild_id") guildId: String,
        @Body options: Map<String, List<String>>
    ): Response<okhttp3.ResponseBody>
    
    @POST("{guild_id}/import")
    suspend fun importData(
        @Path("guild_id") guildId: String,
        @Body file: okhttp3.RequestBody
    ): Response<Map<String, Any>>
    
    // Data Deletion
    @DELETE("{guild_id}/remove-all-data")
    suspend fun deleteAllServerData(@Path("guild_id") guildId: String): Response<Map<String, Any>>
    
    @DELETE("{guild_id}/remove-all-user-data")
    suspend fun deleteUserData(
        @Path("guild_id") guildId: String,
        @Body userId: Map<String, String>
    ): Response<Map<String, Any>>
    
    // Admin Tools
    @GET("admin/api/users/{user_id}/mfa-status")
    suspend fun getAdminUserMfaStatus(@Path("user_id") userId: String): Response<Map<String, Any>>
    
    @POST("admin/api/users/{user_id}/disable-mfa")
    suspend fun disableAdminUserMfa(@Path("user_id") userId: String): Response<Map<String, Any>>
}

