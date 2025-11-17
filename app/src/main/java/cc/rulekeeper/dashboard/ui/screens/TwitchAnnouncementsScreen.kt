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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.*
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwitchAnnouncementsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var announcements by remember { mutableStateOf<List<TwitchAnnouncement>>(emptyList()) }
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var announcementToEdit by remember { mutableStateOf<TwitchAnnouncement?>(null) }
    var announcementToDelete by remember { mutableStateOf<TwitchAnnouncement?>(null) }
    
    // Load data
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load announcements
                val announcementsResponse = apiClient.twitchService.getAnnouncements(guildId)
                if (announcementsResponse.isSuccessful && announcementsResponse.body() != null) {
                    announcements = announcementsResponse.body()!!.announcements
                }
                
                // Load channels
                val channelsResponse = apiClient.guildService.getChannels(guildId)
                if (channelsResponse.isSuccessful && channelsResponse.body() != null) {
                    channels = channelsResponse.body()!!
                }
                
                // Load roles
                val rolesResponse = apiClient.guildService.getRoles(guildId)
                if (rolesResponse.isSuccessful && rolesResponse.body() != null) {
                    roles = rolesResponse.body()!!
                }
                
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error loading data: ${e.message}"
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Twitch Announcements")
                        Text(
                            "Manage live stream notifications",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Stream")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                announcements.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Stream,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Twitch Announcements",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Add your first Twitch notification to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Twitch Notification")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(announcements) { announcement ->
                            TwitchAnnouncementCard(
                                announcement = announcement,
                                channels = channels,
                                roles = roles,
                                onEdit = { announcementToEdit = announcement },
                                onDelete = { announcementToDelete = announcement },
                                onToggle = { enabled ->
                                    scope.launch {
                                        try {
                                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                settingsRepository.getCachedAccessToken()
                                            }
                                            
                                            val response = apiClient.twitchService.updateAnnouncement(
                                                guildId,
                                                announcement.id,
                                                UpdateTwitchAnnouncementRequest(enabled = enabled)
                                            )
                                            
                                            if (response.isSuccessful) {
                                                // Reload announcements
                                                val announcementsResponse = apiClient.twitchService.getAnnouncements(guildId)
                                                if (announcementsResponse.isSuccessful && announcementsResponse.body() != null) {
                                                    announcements = announcementsResponse.body()!!.announcements
                                                }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error updating: ${e.message}"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add Dialog
    if (showAddDialog) {
        AddTwitchAnnouncementDialog(
            channels = channels,
            roles = roles,
            onDismiss = { showAddDialog = false },
            onAdd = { streamerId, channelId, roleId, message ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        val response = apiClient.twitchService.addAnnouncement(
                            guildId,
                            AddTwitchAnnouncementRequest(streamerId, channelId, roleId, message)
                        )
                        
                        if (response.isSuccessful) {
                            showAddDialog = false
                            // Reload announcements
                            val announcementsResponse = apiClient.twitchService.getAnnouncements(guildId)
                            if (announcementsResponse.isSuccessful && announcementsResponse.body() != null) {
                                announcements = announcementsResponse.body()!!.announcements
                            }
                        } else {
                            errorMessage = "Failed to add announcement"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    }
                }
            }
        )
    }
    
    // Edit Dialog
    announcementToEdit?.let { announcement ->
        EditTwitchAnnouncementDialog(
            announcement = announcement,
            channels = channels,
            roles = roles,
            onDismiss = { announcementToEdit = null },
            onSave = { streamerId, channelId, roleId, message ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        val response = apiClient.twitchService.updateAnnouncement(
                            guildId,
                            announcement.id,
                            UpdateTwitchAnnouncementRequest(streamerId, channelId, roleId, message)
                        )
                        
                        if (response.isSuccessful) {
                            announcementToEdit = null
                            // Reload announcements
                            val announcementsResponse = apiClient.twitchService.getAnnouncements(guildId)
                            if (announcementsResponse.isSuccessful && announcementsResponse.body() != null) {
                                announcements = announcementsResponse.body()!!.announcements
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error updating: ${e.message}"
                    }
                }
            }
        )
    }
    
    // Delete Confirmation
    announcementToDelete?.let { announcement ->
        AlertDialog(
            onDismissRequest = { announcementToDelete = null },
            title = { Text("Delete Announcement") },
            text = { Text("Are you sure you want to delete the announcement for ${announcement.streamerId}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                    settingsRepository.getCachedAccessToken()
                                }
                                
                                val response = apiClient.twitchService.deleteAnnouncement(
                                    guildId,
                                    announcement.id
                                )
                                
                                if (response.isSuccessful) {
                                    announcementToDelete = null
                                    // Reload announcements
                                    val announcementsResponse = apiClient.twitchService.getAnnouncements(guildId)
                                    if (announcementsResponse.isSuccessful && announcementsResponse.body() != null) {
                                        announcements = announcementsResponse.body()!!.announcements
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error deleting: ${e.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { announcementToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwitchAnnouncementCard(
    announcement: TwitchAnnouncement,
    channels: List<Channel>,
    roles: List<Role>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val channelName = channels.find { it.id == announcement.channelId }?.name ?: "Unknown"
    val roleName = when (announcement.roleId) {
        null -> "None"
        "@everyone" -> "@everyone"
        else -> roles.find { it.id == announcement.roleId }?.name ?: "Unknown"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Stream,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        announcement.streamerId,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                AssistChip(
                    onClick = { },
                    label = {
                        Text(if (announcement.enabled) "Active" else "Paused")
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (announcement.enabled) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = if (announcement.enabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            
            Divider()
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Tag,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    channelName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (roleName != "None") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        roleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                announcement.message,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                
                OutlinedButton(
                    onClick = { onToggle(!announcement.enabled) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (announcement.enabled) "Disable" else "Enable")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTwitchAnnouncementDialog(
    channels: List<Channel>,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String?, String) -> Unit
) {
    var streamerId by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(channels.firstOrNull()?.id ?: "") }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("{role} {streamer} is live! {title} - {url}") }
    var expandedChannel by remember { mutableStateOf(false) }
    var expandedRole by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Stream Announcement") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = streamerId,
                    onValueChange = { streamerId = it },
                    label = { Text("Streamer Username") },
                    placeholder = { Text("e.g., ninja") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedChannel,
                    onExpandedChange = { expandedChannel = it }
                ) {
                    OutlinedTextField(
                        value = channels.find { it.id == selectedChannel }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Discord Channel") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChannel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedChannel,
                        onDismissRequest = { expandedChannel = false }
                    ) {
                        channels.forEach { channel ->
                            DropdownMenuItem(
                                text = { Text("#${channel.name}") },
                                onClick = {
                                    selectedChannel = channel.id
                                    expandedChannel = false
                                }
                            )
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedRole) {
                            null -> "None"
                            "@everyone" -> "@everyone"
                            else -> roles.find { it.id == selectedRole }?.name ?: "None"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role to Mention (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedRole = null
                                expandedRole = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("@everyone") },
                            onClick = {
                                selectedRole = "@everyone"
                                expandedRole = false
                            }
                        )
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text("@${role.name}") },
                                onClick = {
                                    selectedRole = role.id
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Template Variables:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "{streamer} {title} {game} {url} {role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(streamerId, selectedChannel, selectedRole, message) },
                enabled = streamerId.isNotBlank() && selectedChannel.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTwitchAnnouncementDialog(
    announcement: TwitchAnnouncement,
    channels: List<Channel>,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String) -> Unit
) {
    var streamerId by remember { mutableStateOf(announcement.streamerId) }
    var selectedChannel by remember { mutableStateOf(announcement.channelId) }
    var selectedRole by remember { mutableStateOf(announcement.roleId) }
    var message by remember { mutableStateOf(announcement.message) }
    var expandedChannel by remember { mutableStateOf(false) }
    var expandedRole by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Stream Announcement") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = streamerId,
                    onValueChange = { streamerId = it },
                    label = { Text("Streamer Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedChannel,
                    onExpandedChange = { expandedChannel = it }
                ) {
                    OutlinedTextField(
                        value = channels.find { it.id == selectedChannel }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Discord Channel") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChannel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedChannel,
                        onDismissRequest = { expandedChannel = false }
                    ) {
                        channels.forEach { channel ->
                            DropdownMenuItem(
                                text = { Text("#${channel.name}") },
                                onClick = {
                                    selectedChannel = channel.id
                                    expandedChannel = false
                                }
                            )
                        }
                    }
                }
                
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedRole) {
                            null -> "None"
                            "@everyone" -> "@everyone"
                            else -> roles.find { it.id == selectedRole }?.name ?: "None"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role to Mention (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                selectedRole = null
                                expandedRole = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("@everyone") },
                            onClick = {
                                selectedRole = "@everyone"
                                expandedRole = false
                            }
                        )
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text("@${role.name}") },
                                onClick = {
                                    selectedRole = role.id
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Announcement Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Template Variables:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "{streamer} {title} {game} {url} {role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(streamerId, selectedChannel, selectedRole, message) },
                enabled = streamerId.isNotBlank() && selectedChannel.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
