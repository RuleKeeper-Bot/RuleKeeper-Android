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
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthdayManagementScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var enabled by remember { mutableStateOf(false) }
    var announcementChannel by remember { mutableStateOf("") }
    var birthdayRole by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    
    // Birthday list management
    var birthdays by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoadingBirthdays by remember { mutableStateOf(false) }
    var birthdayToDelete by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAddBirthdayDialog by remember { mutableStateOf(false) }
    var birthdayErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load birthdays
    fun loadBirthdays() {
        scope.launch {
            try {
                isLoadingBirthdays = true
                birthdayErrorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.usersService.getBirthdays(guildId)
                if (response.isSuccessful) {
                    birthdays = response.body() ?: emptyList()
                } else {
                    birthdayErrorMessage = "Failed to load birthdays: ${response.code()}"
                }
            } catch (e: Exception) {
                birthdayErrorMessage = "Error loading birthdays: ${e.message}"
            } finally {
                isLoadingBirthdays = false
            }
        }
    }
    
    // Load existing configuration
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.configService.getBirthdaysConfig(guildId)
                if (response.isSuccessful && response.body() != null) {
                    val config = response.body()!!
                    enabled = config["enabled"] as? Boolean ?: false
                    announcementChannel = config["channel_id"] as? String ?: ""
                    birthdayRole = config["role_id"] as? String ?: ""
                }
            } catch (e: Exception) {
                // Silently fail, use defaults
            }
        }
        // Load birthdays list
        loadBirthdays()
    }
    
    // Confirmation dialog for birthday deletion
    if (birthdayToDelete != null) {
        ConfirmationDialog(
            title = "Delete Birthday",
            message = "Are you sure you want to delete the birthday for ${birthdayToDelete!!["username"] ?: "this user"}?",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val userId = birthdayToDelete!!["user_id"]?.toString() ?: return@launch
                        val response = apiClient.usersService.deleteBirthday(guildId, userId)
                        if (response.isSuccessful) {
                            loadBirthdays()
                            birthdayToDelete = null
                        } else {
                            birthdayErrorMessage = "Failed to delete birthday: ${response.code()}"
                            birthdayToDelete = null
                        }
                    } catch (e: Exception) {
                        birthdayErrorMessage = "Error deleting birthday: ${e.message}"
                        birthdayToDelete = null
                    }
                }
            },
            onDismiss = { birthdayToDelete = null },
            isDestructive = true
        )
    }
    
    // Add birthday dialog
    if (showAddBirthdayDialog) {
        AddBirthdayDialog(
            onDismiss = { showAddBirthdayDialog = false },
            onAdd = { birthdayData ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.usersService.setBirthday(guildId, birthdayData)
                        if (response.isSuccessful) {
                            loadBirthdays()
                            showAddBirthdayDialog = false
                        } else {
                            birthdayErrorMessage = "Failed to set birthday: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        birthdayErrorMessage = "Error setting birthday: ${e.message}"
                    }
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Birthday Management") },
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
                                Icons.Default.Cake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Birthday System",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Celebrate member birthdays with automatic announcements and special roles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Enable Birthday System", style = MaterialTheme.typography.titleMedium)
                            Switch(checked = enabled, onCheckedChange = { enabled = it })
                        }
                    }
                }
            }
            
            if (enabled) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Configuration", style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = announcementChannel,
                                onValueChange = { announcementChannel = it },
                                label = { Text("Announcement Channel ID") },
                                placeholder = { Text("Where to post birthday messages") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = birthdayRole,
                                onValueChange = { birthdayRole = it },
                                label = { Text("Birthday Role ID") },
                                placeholder = { Text("Special role for birthday members") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text(
                                "API endpoints: GET/PUT /config/{guild_id}/birthdays, GET /users/{guild_id}/birthdays",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (saveMessage != null) {
                                Text(
                                    saveMessage!!,
                                    color = if (saveMessage!!.contains("success", ignoreCase = true))
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        saveMessage = null
                                        try {
                                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                settingsRepository.getCachedAccessToken()
                                            }
                                            val config = mapOf(
                                                "enabled" to enabled.toString(),
                                                "channel_id" to announcementChannel,
                                                "role_id" to birthdayRole
                                            )
                                            val response = apiClient.configService.updateBirthdaysConfig(guildId, config)
                                            if (response.isSuccessful) {
                                                saveMessage = "Configuration saved successfully!"
                                            } else {
                                                saveMessage = "Failed to save configuration"
                                            }
                                        } catch (e: Exception) {
                                            saveMessage = "Error: ${e.message}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Saving...")
                                } else {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Configuration")
                                }
                            }
                        }
                    }
                }
                
                // User Birthdays List
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("User Birthdays", style = MaterialTheme.typography.titleMedium)
                                OutlinedButton(
                                    onClick = { showAddBirthdayDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Birthday")
                                }
                            }
                            
                            if (isLoadingBirthdays) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else if (birthdayErrorMessage != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            birthdayErrorMessage!!,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            } else if (birthdays.isEmpty()) {
                                Text(
                                    "No birthdays set yet. Click 'Add Birthday' to set one.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                birthdays.forEach { birthday ->
                                    BirthdayCard(
                                        birthday = birthday,
                                        onDelete = { birthdayToDelete = birthday }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BirthdayCard(
    birthday: Map<String, Any>,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = birthday["username"]?.toString() ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "🎂 ${birthday["birthday"]?.toString() ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "User ID: ${birthday["user_id"]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete birthday")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBirthdayDialog(
    onDismiss: () -> Unit,
    onAdd: (Map<String, Any>) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var birthdayDate by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Birthday") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Discord user ID") }
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Display name") }
                )
                
                OutlinedTextField(
                    value = birthdayDate,
                    onValueChange = { birthdayDate = it },
                    label = { Text("Birthday") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("MM-DD or YYYY-MM-DD") },
                    supportingText = { Text("Format: MM-DD (e.g., 03-15) or YYYY-MM-DD (e.g., 1990-03-15)") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (userId.isNotBlank() && birthdayDate.isNotBlank()) {
                        onAdd(mapOf(
                            "user_id" to userId,
                            "username" to username.ifBlank { "User $userId" },
                            "birthday" to birthdayDate
                        ))
                    }
                },
                enabled = userId.isNotBlank() && birthdayDate.isNotBlank()
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
