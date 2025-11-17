package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.Channel
import cc.rulekeeper.dashboard.data.model.LogConfig
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggingScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // All log config fields matching database schema
    var logChannelId by remember { mutableStateOf("") }
    var logConfigUpdate by remember { mutableStateOf(true) }
    var messageDelete by remember { mutableStateOf(true) }
    var bulkMessageDelete by remember { mutableStateOf(true) }
    var messageEdit by remember { mutableStateOf(true) }
    var inviteCreate by remember { mutableStateOf(true) }
    var inviteDelete by remember { mutableStateOf(true) }
    var memberRoleAdd by remember { mutableStateOf(true) }
    var memberRoleRemove by remember { mutableStateOf(true) }
    var memberTimeout by remember { mutableStateOf(true) }
    var memberWarn by remember { mutableStateOf(true) }
    var memberUnwarn by remember { mutableStateOf(true) }
    var memberBan by remember { mutableStateOf(true) }
    var memberUnban by remember { mutableStateOf(true) }
    var memberNicknameChange by remember { mutableStateOf(true) }
    var roleCreate by remember { mutableStateOf(true) }
    var roleDelete by remember { mutableStateOf(true) }
    var roleUpdate by remember { mutableStateOf(true) }
    var channelCreate by remember { mutableStateOf(true) }
    var channelDelete by remember { mutableStateOf(true) }
    var channelUpdate by remember { mutableStateOf(true) }
    var emojiCreate by remember { mutableStateOf(true) }
    var emojiNameChange by remember { mutableStateOf(true) }
    var emojiDelete by remember { mutableStateOf(true) }
    var backupCreated by remember { mutableStateOf(true) }
    var backupFailed by remember { mutableStateOf(true) }
    var backupDeleted by remember { mutableStateOf(true) }
    var backupRestored by remember { mutableStateOf(true) }
    var backupRestoreFailed by remember { mutableStateOf(true) }
    var backupScheduleCreated by remember { mutableStateOf(true) }
    var backupScheduleDeleted by remember { mutableStateOf(true) }
    var logBots by remember { mutableStateOf(true) }
    var logSelf by remember { mutableStateOf(false) }
    var excludedChannels by remember { mutableStateOf("[]") }
    var excludedRoles by remember { mutableStateOf("[]") }
    var excludedUsers by remember { mutableStateOf("[]") }
    
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var showChannelDropdown by remember { mutableStateOf(false) }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load channels
                val channelsResponse = apiClient.guildService.getChannels(guildId)
                if (channelsResponse.isSuccessful && channelsResponse.body() != null) {
                    channels = channelsResponse.body()!!.filter { it.type == 0 } // Text channels only
                }
                
                // Load current log config
                val configResponse = apiClient.configService.getLogConfig(guildId)
                if (configResponse.isSuccessful && configResponse.body() != null) {
                    val config = configResponse.body()!!
                    logChannelId = config.logChannelId ?: ""
                    logConfigUpdate = config.logConfigUpdate
                    messageDelete = config.messageDelete
                    bulkMessageDelete = config.bulkMessageDelete
                    messageEdit = config.messageEdit
                    inviteCreate = config.inviteCreate
                    inviteDelete = config.inviteDelete
                    memberRoleAdd = config.memberRoleAdd
                    memberRoleRemove = config.memberRoleRemove
                    memberTimeout = config.memberTimeout
                    memberWarn = config.memberWarn
                    memberUnwarn = config.memberUnwarn
                    memberBan = config.memberBan
                    memberUnban = config.memberUnban
                    memberNicknameChange = config.memberNicknameChange
                    roleCreate = config.roleCreate
                    roleDelete = config.roleDelete
                    roleUpdate = config.roleUpdate
                    channelCreate = config.channelCreate
                    channelDelete = config.channelDelete
                    channelUpdate = config.channelUpdate
                    emojiCreate = config.emojiCreate
                    emojiNameChange = config.emojiNameChange
                    emojiDelete = config.emojiDelete
                    backupCreated = config.backupCreated
                    backupFailed = config.backupFailed
                    backupDeleted = config.backupDeleted
                    backupRestored = config.backupRestored
                    backupRestoreFailed = config.backupRestoreFailed
                    backupScheduleCreated = config.backupScheduleCreated
                    backupScheduleDeleted = config.backupScheduleDeleted
                    logBots = config.logBots
                    logSelf = config.logSelf
                    excludedChannels = config.excludedChannels ?: "[]"
                    excludedRoles = config.excludedRoles ?: "[]"
                    excludedUsers = config.excludedUsers ?: "[]"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load configuration"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logging Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Event Logging",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Configure which server events are logged to a specific channel.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                if (showSuccess) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                "Logging configuration saved successfully!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                if (errorMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Log Channel", style = MaterialTheme.typography.titleMedium)
                            
                            ExposedDropdownMenuBox(
                                expanded = showChannelDropdown,
                                onExpandedChange = { showChannelDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = channels.find { it.id == logChannelId }?.name ?: "Select channel",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Log Channel") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showChannelDropdown)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showChannelDropdown,
                                    onDismissRequest = { showChannelDropdown = false }
                                ) {
                                    channels.forEach { channel ->
                                        DropdownMenuItem(
                                            text = { Text("# ${channel.name}") },
                                            onClick = {
                                                logChannelId = channel.id
                                                showChannelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Message Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Message Deletions", messageDelete) { messageDelete = it }
                            LoggingToggleItem("Bulk Message Deletions", bulkMessageDelete) { bulkMessageDelete = it }
                            LoggingToggleItem("Message Edits", messageEdit) { messageEdit = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Member Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Member Role Added", memberRoleAdd) { memberRoleAdd = it }
                            LoggingToggleItem("Member Role Removed", memberRoleRemove) { memberRoleRemove = it }
                            LoggingToggleItem("Member Timeout", memberTimeout) { memberTimeout = it }
                            LoggingToggleItem("Member Warn", memberWarn) { memberWarn = it }
                            LoggingToggleItem("Member Unwarn", memberUnwarn) { memberUnwarn = it }
                            LoggingToggleItem("Member Bans", memberBan) { memberBan = it }
                            LoggingToggleItem("Member Unbans", memberUnban) { memberUnban = it }
                            LoggingToggleItem("Member Nickname Changes", memberNicknameChange) { memberNicknameChange = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Server Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Role Created", roleCreate) { roleCreate = it }
                            LoggingToggleItem("Role Deleted", roleDelete) { roleDelete = it }
                            LoggingToggleItem("Role Updated", roleUpdate) { roleUpdate = it }
                            LoggingToggleItem("Channel Created", channelCreate) { channelCreate = it }
                            LoggingToggleItem("Channel Deleted", channelDelete) { channelDelete = it }
                            LoggingToggleItem("Channel Updated", channelUpdate) { channelUpdate = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Emoji Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Emoji Created", emojiCreate) { emojiCreate = it }
                            LoggingToggleItem("Emoji Name Changed", emojiNameChange) { emojiNameChange = it }
                            LoggingToggleItem("Emoji Deleted", emojiDelete) { emojiDelete = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Invite Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Invite Created", inviteCreate) { inviteCreate = it }
                            LoggingToggleItem("Invite Deleted", inviteDelete) { inviteDelete = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Backup Events", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Backup Created", backupCreated) { backupCreated = it }
                            LoggingToggleItem("Backup Failed", backupFailed) { backupFailed = it }
                            LoggingToggleItem("Backup Deleted", backupDeleted) { backupDeleted = it }
                            LoggingToggleItem("Backup Restored", backupRestored) { backupRestored = it }
                            LoggingToggleItem("Backup Restore Failed", backupRestoreFailed) { backupRestoreFailed = it }
                            LoggingToggleItem("Backup Schedule Created", backupScheduleCreated) { backupScheduleCreated = it }
                            LoggingToggleItem("Backup Schedule Deleted", backupScheduleDeleted) { backupScheduleDeleted = it }
                        }
                    }
                }
                
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Additional Options", style = MaterialTheme.typography.titleMedium)
                            
                            LoggingToggleItem("Log Config Updates", logConfigUpdate) { logConfigUpdate = it }
                            LoggingToggleItem("Log Bot Actions", logBots) { logBots = it }
                            LoggingToggleItem("Log Self Actions", logSelf) { logSelf = it }
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    errorMessage = null
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    
                                    val config = LogConfig(
                                        logChannelId = logChannelId.ifBlank { null },
                                        messageDelete = messageDelete,
                                        messageEdit = messageEdit,
                                        bulkMessageDelete = bulkMessageDelete,
                                        memberBan = memberBan,
                                        memberUnban = memberUnban,
                                        memberRoleAdd = memberRoleAdd,
                                        memberRoleRemove = memberRoleRemove,
                                        memberTimeout = memberTimeout,
                                        memberWarn = memberWarn,
                                        memberUnwarn = memberUnwarn,
                                        memberNicknameChange = memberNicknameChange,
                                        channelCreate = channelCreate,
                                        channelDelete = channelDelete,
                                        channelUpdate = channelUpdate,
                                        roleCreate = roleCreate,
                                        roleDelete = roleDelete,
                                        roleUpdate = roleUpdate,
                                        emojiCreate = emojiCreate,
                                        emojiNameChange = emojiNameChange,
                                        emojiDelete = emojiDelete,
                                        inviteCreate = inviteCreate,
                                        inviteDelete = inviteDelete,
                                        backupCreated = backupCreated,
                                        backupFailed = backupFailed,
                                        backupDeleted = backupDeleted,
                                        backupRestored = backupRestored,
                                        backupRestoreFailed = backupRestoreFailed,
                                        backupScheduleCreated = backupScheduleCreated,
                                        backupScheduleDeleted = backupScheduleDeleted,
                                        logConfigUpdate = logConfigUpdate,
                                        logBots = logBots,
                                        logSelf = logSelf,
                                        excludedChannels = excludedChannels,
                                        excludedRoles = excludedRoles,
                                        excludedUsers = excludedUsers
                                    )
                                    
                                    apiClient.configService.updateLogConfig(guildId, config)
                                    showSuccess = true
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to save configuration"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = logChannelId.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configuration")
                    }
                }
            }
        }
    }
}

@Composable
fun LoggingToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
