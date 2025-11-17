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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Backup(
    val id: String,
    val guildId: String,
    val createdAt: Long,
    val filePath: String?,
    val scheduled: Boolean,
    val shareId: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    onNavigateToSchedules: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var backups by remember { mutableStateOf<List<Backup>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var backupToDelete by remember { mutableStateOf<Backup?>(null) }
    var backupToRestore by remember { mutableStateOf<Backup?>(null) }
    var showShareDialog by remember { mutableStateOf<Backup?>(null) }
    var isCreatingBackup by remember { mutableStateOf(false) }
    
    val loadBackups: suspend () -> Unit = {
        try {
            isLoading = true
            errorMessage = null
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            val backupsResponse = apiClient.backupsService.getBackups(guildId)
            if (backupsResponse.isSuccessful && backupsResponse.body() != null) {
                backups = backupsResponse.body()!!.mapNotNull { map ->
                    try {
                        Backup(
                            id = map["id"]?.toString() ?: "",
                            guildId = map["guild_id"]?.toString() ?: guildId,
                            createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L,
                            filePath = map["file_path"]?.toString(),
                            scheduled = ((map["scheduled"] as? Number)?.toInt() ?: 0) == 1,
                            shareId = map["share_id"]?.toString()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                errorMessage = "Failed to load backups: ${backupsResponse.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading backups: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        loadBackups()
    }
    
    // Delete confirmation
    if (backupToDelete != null) {
        ConfirmationDialog(
            title = "Delete Backup",
            message = "Delete this backup? This cannot be undone!",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.backupsService.deleteBackup(guildId, backupToDelete!!.id)
                        if (response.isSuccessful) {
                            successMessage = "Backup deleted successfully"
                            loadBackups()
                        } else {
                            errorMessage = "Failed to delete backup: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error deleting backup: ${e.message}"
                    } finally {
                        backupToDelete = null
                    }
                }
            },
            onDismiss = { backupToDelete = null },
            isDestructive = true
        )
    }
    
    // Restore confirmation
    if (backupToRestore != null) {
        ConfirmationDialog(
            title = "Restore Backup",
            message = "Restore this backup? This will overwrite current server settings!",
            confirmButtonText = "Restore",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.backupsService.restoreBackup(guildId, backupToRestore!!.id)
                        if (response.isSuccessful) {
                            successMessage = "Backup restore started"
                        } else {
                            errorMessage = "Failed to restore backup: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error restoring backup: ${e.message}"
                    } finally {
                        backupToRestore = null
                    }
                }
            },
            onDismiss = { backupToRestore = null },
            isDestructive = true
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Server Backups")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSchedules) {
                        Icon(Icons.Default.Schedule, contentDescription = "Schedules")
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
            ExtendedFloatingActionButton(
                onClick = {
                    scope.launch {
                        isCreatingBackup = true
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            val response = apiClient.backupsService.createBackup(guildId, emptyMap())
                            if (response.isSuccessful) {
                                successMessage = "Backup creation started"
                                kotlinx.coroutines.delay(2000)
                                loadBackups()
                            } else {
                                errorMessage = "Failed to create backup: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error creating backup: ${e.message}"
                        } finally {
                            isCreatingBackup = false
                        }
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (isCreatingBackup) "Creating..." else "Create Backup Now") },
                expanded = !isCreatingBackup
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages
            successMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(message, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { successMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
            
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }
            
            // Backups list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (backups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No backups found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Create your first backup to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(backups) { backup ->
                        BackupCard(
                            backup = backup,
                            onDownload = {
                                scope.launch {
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val response = apiClient.backupsService.downloadBackup(guildId, backup.id)
                                        if (response.isSuccessful) {
                                            successMessage = "Backup download started"
                                        } else {
                                            errorMessage = "Download failed: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Download error: ${e.message}"
                                    }
                                }
                            },
                            onRestore = { backupToRestore = backup },
                            onDelete = { backupToDelete = backup },
                            onShare = { showShareDialog = backup }
                        )
                    }
                }
            }
        }
    }
    
    // Share dialog
    showShareDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showShareDialog = null },
            title = { Text("Share Backup") },
            text = {
                Column {
                    if (backup.shareId != null) {
                        Text("Share link created! Anyone with this link can import this backup.")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = "https://your-instance.com/share/backup/${backup.shareId}",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Create a share link for this backup? Anyone with the link will be able to import it to their server.")
                    }
                }
            },
            confirmButton = {
                if (backup.shareId == null) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    val response = apiClient.backupsService.shareBackup(guildId, backup.id)
                                    if (response.isSuccessful && response.body() != null) {
                                        val shareId = response.body()!!["share_id"]?.toString()
                                        if (shareId != null) {
                                            successMessage = "Share link created"
                                            loadBackups()
                                        } else {
                                            errorMessage = "Failed to get share ID"
                                        }
                                    } else {
                                        errorMessage = "Failed to create share link: ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Failed to create share link: ${e.message}"
                                }
                                showShareDialog = null
                            }
                        }
                    ) {
                        Text("Create Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun BackupCard(
    backup: Backup,
    onDownload: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(backup.createdAt * 1000))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Backup ID: ${backup.id.take(12)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (backup.scheduled) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Auto", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        }
                    }
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Download")
                }
                
                Button(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Restore")
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (backup.shareId != null) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        if (backup.shareId != null) Icons.Default.Link else Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (backup.shareId != null) "Shared" else "Share")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Remove")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSchedule(
    autoBackupEnabled: Boolean,
    onAutoBackupToggle: (Boolean) -> Unit,
    backupFrequency: String,
    onFrequencyChange: (String) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Automatic Backups", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = onAutoBackupToggle
                        )
                    }
                    
                    if (autoBackupEnabled) {
                        Text("Backup Frequency:", style = MaterialTheme.typography.bodyMedium)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = backupFrequency == "daily",
                                onClick = { onFrequencyChange("daily") },
                                label = { Text("Daily") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = backupFrequency == "weekly",
                                onClick = { onFrequencyChange("weekly") },
                                label = { Text("Weekly") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            FilterChip(
                                selected = backupFrequency == "monthly",
                                onClick = { onFrequencyChange("monthly") },
                                label = { Text("Monthly") },
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Text(
                        "Automatic backups will preserve your server configuration regularly",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        item {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Schedule Settings")
            }
        }
    }
}

@Composable
fun CreateBackupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean) -> Unit
) {
    var backupName by remember { mutableStateOf("Backup ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}") }
    var includeMessages by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    label = { Text("Backup Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include message history")
                    Checkbox(
                        checked = includeMessages,
                        onCheckedChange = { includeMessages = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (backupName.isNotBlank()) {
                        onConfirm(backupName, includeMessages)
                    }
                }
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
