package cc.rulekeeper.dashboard.data.model

import cc.rulekeeper.dashboard.data.api.JsonArrayToStringAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class Guild(
    @SerializedName("guild_id")
    val id: String,
    val name: String,
    val icon: String?,
    @SerializedName("owner")
    val isOwner: Boolean? = null,
    val permissions: Long? = null,
    @SerializedName("has_bot")
    val hasBot: Boolean? = null
)

data class GuildListResponse(
    val guilds: List<Guild>,
    val total: Int
)

data class GuildCommand(
    val id: Int?,
    @SerializedName("command_name")
    val commandName: String,
    val content: String?,
    val description: String?,
    val ephemeral: Boolean = false,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("modified_at")
    val modifiedAt: String?,
    @SerializedName("is_builtin")
    val isBuiltin: Boolean = false
)

data class CreateCommandRequest(
    @SerializedName("command_name")
    val commandName: String,
    val content: String,
    val description: String = "",
    val ephemeral: Boolean = false
)

data class LogConfig(
    @SerializedName("log_channel_id")
    val logChannelId: String?,
    @SerializedName("log_config_update")
    val logConfigUpdate: Boolean = true,
    @SerializedName("message_delete")
    val messageDelete: Boolean = true,
    @SerializedName("bulk_message_delete")
    val bulkMessageDelete: Boolean = true,
    @SerializedName("message_edit")
    val messageEdit: Boolean = true,
    @SerializedName("invite_create")
    val inviteCreate: Boolean = true,
    @SerializedName("invite_delete")
    val inviteDelete: Boolean = true,
    @SerializedName("member_role_add")
    val memberRoleAdd: Boolean = true,
    @SerializedName("member_role_remove")
    val memberRoleRemove: Boolean = true,
    @SerializedName("member_timeout")
    val memberTimeout: Boolean = true,
    @SerializedName("member_warn")
    val memberWarn: Boolean = true,
    @SerializedName("member_unwarn")
    val memberUnwarn: Boolean = true,
    @SerializedName("member_ban")
    val memberBan: Boolean = true,
    @SerializedName("member_unban")
    val memberUnban: Boolean = true,
    @SerializedName("member_nickname_change")
    val memberNicknameChange: Boolean = true,
    @SerializedName("role_create")
    val roleCreate: Boolean = true,
    @SerializedName("role_delete")
    val roleDelete: Boolean = true,
    @SerializedName("role_update")
    val roleUpdate: Boolean = true,
    @SerializedName("channel_create")
    val channelCreate: Boolean = true,
    @SerializedName("channel_delete")
    val channelDelete: Boolean = true,
    @SerializedName("channel_update")
    val channelUpdate: Boolean = true,
    @SerializedName("emoji_create")
    val emojiCreate: Boolean = true,
    @SerializedName("emoji_name_change")
    val emojiNameChange: Boolean = true,
    @SerializedName("emoji_delete")
    val emojiDelete: Boolean = true,
    @SerializedName("backup_created")
    val backupCreated: Boolean = true,
    @SerializedName("backup_failed")
    val backupFailed: Boolean = true,
    @SerializedName("backup_deleted")
    val backupDeleted: Boolean = true,
    @SerializedName("backup_restored")
    val backupRestored: Boolean = true,
    @SerializedName("backup_restore_failed")
    val backupRestoreFailed: Boolean = true,
    @SerializedName("backup_schedule_created")
    val backupScheduleCreated: Boolean = true,
    @SerializedName("backup_schedule_deleted")
    val backupScheduleDeleted: Boolean = true,
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_users")
    val excludedUsers: String? = "[]",
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_roles")
    val excludedRoles: String? = "[]",
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_channels")
    val excludedChannels: String? = "[]",
    @SerializedName("log_bots")
    val logBots: Boolean = true,
    @SerializedName("log_self")
    val logSelf: Boolean = false
)

data class WelcomeConfig(
    val enabled: Boolean = false,
    @SerializedName("channel_id")
    val channelId: String?,
    @SerializedName("message_type")
    val messageType: String = "text",
    @SerializedName("message_content")
    val messageContent: String?,
    @SerializedName("embed_title")
    val embedTitle: String?,
    @SerializedName("embed_description")
    val embedDescription: String?,
    @SerializedName("embed_color")
    val embedColor: Int = 0x00FF00,
    @SerializedName("embed_thumbnail")
    val embedThumbnail: Boolean = true,
    @SerializedName("show_server_icon")
    val showServerIcon: Boolean = false
)

data class GoodbyeConfig(
    val enabled: Boolean = false,
    @SerializedName("channel_id")
    val channelId: String?,
    @SerializedName("message_type")
    val messageType: String = "text",
    @SerializedName("message_content")
    val messageContent: String?,
    @SerializedName("embed_title")
    val embedTitle: String?,
    @SerializedName("embed_description")
    val embedDescription: String?,
    @SerializedName("embed_color")
    val embedColor: Int = 0xFF0000,
    @SerializedName("embed_thumbnail")
    val embedThumbnail: Boolean = true,
    @SerializedName("show_server_icon")
    val showServerIcon: Boolean = false
)

data class SpamConfig(
    val enabled: Boolean = true,
    @SerializedName("spam_threshold")
    val spamThreshold: Int = 5,
    @SerializedName("spam_time_window")
    val spamTimeWindow: Int = 10,
    @SerializedName("mention_threshold")
    val mentionThreshold: Int = 3,
    @SerializedName("mention_time_window")
    val mentionTimeWindow: Int = 30,
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_channels")
    val excludedChannels: String = "[]",
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_roles")
    val excludedRoles: String = "[]",
    @SerializedName("spam_strikes_before_warning")
    val spamStrikesBeforeWarning: Int = 1,
    @SerializedName("no_xp_duration")
    val noXpDuration: Int = 60
)

data class LevelConfig(
    val cooldown: Int = 60,
    @SerializedName("xp_min")
    val xpMin: Int = 15,
    @SerializedName("xp_max")
    val xpMax: Int = 25,
    @SerializedName("level_channel")
    val levelChannel: String?,
    @SerializedName("announce_level_up")
    val announceLevelUp: Boolean = true,
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_channels")
    val excludedChannels: String = "[]",
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("xp_boost_roles")
    val xpBoostRoles: String = "{}",
    @SerializedName("embed_title")
    val embedTitle: String = "🎉 Level Up!",
    @SerializedName("embed_description")
    val embedDescription: String = "{user} has reached level **{level}**!",
    @SerializedName("embed_color")
    val embedColor: Int = 16766720,
    @SerializedName("give_xp_to_bots")
    val giveXpToBots: Boolean = false,
    @SerializedName("give_xp_to_self")
    val giveXpToSelf: Boolean = false,
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("cooldown_bypass_users")
    val cooldownBypassUsers: String = "[]",
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("cooldown_bypass_roles")
    val cooldownBypassRoles: String = "[]"
)

data class WarningAction(
    @SerializedName("warning_count")
    val warningCount: Int,
    val action: String, // "timeout", "kick", or "ban"
    @SerializedName("duration_seconds")
    val durationSeconds: Int? = null
)

data class WarningActionsResponse(
    val actions: List<WarningAction>
)

data class WarningActionsUpdate(
    val actions: List<WarningAction>
)

data class RestoreSettings(
    @SerializedName("guild_id")
    val guildId: String,
    @SerializedName("restore_roles")
    val restoreRoles: Boolean = true,
    @SerializedName("restore_xp")
    val restoreXp: Boolean = true,
    @SerializedName("restore_nickname")
    val restoreNickname: Boolean = true,
    @JsonAdapter(JsonArrayToStringAdapter::class)
    @SerializedName("excluded_roles")
    val excludedRoles: String = "[]"
)

data class Warning(
    val id: String,
    @SerializedName("user_id")
    val userId: String,
    val username: String?,
    val reason: String?,
    val timestamp: String?,
    @SerializedName("warned_by")
    val warnedBy: String?,
    @SerializedName("moderator_id")
    val moderatorId: String?
)

data class Ban(
    @SerializedName("user_id")
    val userId: String,
    val username: String,
    val reason: String?
)

data class BansResponse(
    val bans: List<Ban>
)

data class Channel(
    val id: String,
    val name: String,
    val type: Int,
    val position: Int?
)

data class Role(
    val id: String,
    val name: String,
    val color: Int,
    val position: Int,
    val permissions: String
)

data class RoleMenu(
    val id: String,
    @SerializedName("guild_id")
    val guildId: String,
    val type: String, // "dropdown", "button", or "reactionrole"
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("message_id")
    val messageId: String? = null,
    val config: RoleMenuConfig?,
    @SerializedName("created_by")
    val createdBy: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class RoleMenuConfig(
    val title: String? = null,
    val description: String? = null,
    val roles: List<RoleMenuRole>? = null,
    val placeholder: String? = null,
    @SerializedName("min_values")
    val minValues: Int? = 1,
    @SerializedName("max_values")
    val maxValues: Int? = 1,
    val color: String? = null,
    val style: String? = null
)

data class RoleMenuRole(
    @SerializedName("role_id")
    val roleId: String,
    val label: String,
    val description: String? = null,
    val emoji: String? = null
)

data class RoleMenuCreate(
    @SerializedName("guild_id")
    val guildId: String,
    val type: String,
    @SerializedName("channel_id")
    val channelId: String,
    val config: RoleMenuConfig = RoleMenuConfig()
)

data class RoleMenuUpdate(
    val type: String? = null,
    @SerializedName("channel_id")
    val channelId: String? = null,
    val config: RoleMenuConfig? = null
)

data class LeaderboardEntry(
    @SerializedName("user_id")
    val userId: String,
    val username: String,
    val xp: Double,
    val level: Int
)

data class LeaderboardResponse(
    val leaderboard: List<LeaderboardEntry>
)

data class LevelReward(
    val level: Int,
    @SerializedName("role_id")
    val roleId: String
)

data class LevelRewardsResponse(
    val rewards: Map<String, String> // level -> role_id
)

data class AddLevelRewardRequest(
    val level: Int,
    @SerializedName("role_id")
    val roleId: String
)

data class TwitchAnnouncement(
    val id: Int,
    @SerializedName("streamer_id")
    val streamerId: String,
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("role_id")
    val roleId: String?,
    val message: String,
    val enabled: Boolean = true
)

data class TwitchAnnouncementsResponse(
    val announcements: List<TwitchAnnouncement>
)

data class AddTwitchAnnouncementRequest(
    @SerializedName("streamer_id")
    val streamerId: String,
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("role_id")
    val roleId: String?,
    val message: String
)

data class UpdateTwitchAnnouncementRequest(
    @SerializedName("streamer_id")
    val streamerId: String? = null,
    @SerializedName("channel_id")
    val channelId: String? = null,
    @SerializedName("role_id")
    val roleId: String? = null,
    val message: String? = null,
    val enabled: Boolean? = null
)

data class YouTubeAnnouncement(
    val id: Int,
    @SerializedName("channel_id_yt")
    val channelIdYt: String,
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("role_id")
    val roleId: String?,
    val message: String,
    val enabled: Boolean = true
)

data class YouTubeAnnouncementsResponse(
    val announcements: List<YouTubeAnnouncement>
)

data class AddYouTubeAnnouncementRequest(
    @SerializedName("channel_id_yt")
    val channelIdYt: String,
    @SerializedName("channel_id")
    val channelId: String,
    @SerializedName("role_id")
    val roleId: String?,
    val message: String
)

data class UpdateYouTubeAnnouncementRequest(
    @SerializedName("channel_id_yt")
    val channelIdYt: String? = null,
    @SerializedName("channel_id")
    val channelId: String? = null,
    @SerializedName("role_id")
    val roleId: String? = null,
    val message: String? = null,
    val enabled: Boolean? = null
)
