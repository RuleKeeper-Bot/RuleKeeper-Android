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

data class BackupSchedule(
    val id: Int,
    val guildId: String,
    val frequencyValue: Int,
    val frequencyUnit: String,
    val startTime: String?,
    val startDate: String?,
    val maxBackups: Int,
    val enabled: Boolean,
    val timezone: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSchedulesScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var schedules by remember { mutableStateOf<List<BackupSchedule>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var scheduleToDelete by remember { mutableStateOf<BackupSchedule?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<BackupSchedule?>(null) }
    
    val loadSchedules: suspend () -> Unit = {
        try {
            isLoading = true
            errorMessage = null
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            val response = apiClient.backupsService.getBackupSchedules(guildId)
            if (response.isSuccessful && response.body() != null) {
                schedules = response.body()!!.mapNotNull { map ->
                    try {
                        BackupSchedule(
                            id = (map["id"] as? Number)?.toInt() ?: 0,
                            guildId = map["guild_id"]?.toString() ?: guildId,
                            frequencyValue = (map["frequency_value"] as? Number)?.toInt() ?: 1,
                            frequencyUnit = map["frequency_unit"]?.toString() ?: "days",
                            startTime = map["start_time"]?.toString(),
                            startDate = map["start_date"]?.toString(),
                            maxBackups = (map["max_backups"] as? Number)?.toInt() ?: 7,
                            enabled = ((map["enabled"] as? Number)?.toInt() ?: 0) == 1,
                            timezone = map["timezone"]?.toString()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                errorMessage = "Failed to load schedules: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading schedules: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        loadSchedules()
    }
    
    // Delete confirmation
    if (scheduleToDelete != null) {
        ConfirmationDialog(
            title = "Delete Schedule",
            message = "Delete this backup schedule? Scheduled backups will no longer run.",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.backupsService.deleteBackupSchedule(
                            guildId,
                            scheduleToDelete!!.id
                        )
                        if (response.isSuccessful) {
                            successMessage = "Schedule deleted successfully"
                            loadSchedules()
                        } else {
                            errorMessage = "Failed to delete schedule: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error deleting schedule: ${e.message}"
                    } finally {
                        scheduleToDelete = null
                    }
                }
            },
            onDismiss = { scheduleToDelete = null },
            isDestructive = true
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Backup Schedules")
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
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Schedule") }
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
            
            // Schedules list
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (schedules.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No schedules configured",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "Create a schedule to automate backups",
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
                    items(schedules) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onToggle = {
                                scope.launch {
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val updateData = mapOf("enabled" to if (schedule.enabled) 0 else 1)
                                        val response = apiClient.backupsService.updateBackupSchedule(
                                            guildId,
                                            schedule.id,
                                            updateData
                                        )
                                        if (response.isSuccessful) {
                                            successMessage = "Schedule ${if (schedule.enabled) "disabled" else "enabled"}"
                                            loadSchedules()
                                        } else {
                                            errorMessage = "Failed to update schedule"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error updating schedule: ${e.message}"
                                    }
                                }
                            },
                            onEdit = { scheduleToEdit = schedule },
                            onDelete = { scheduleToDelete = schedule }
                        )
                    }
                }
            }
        }
    }
    
    // Create/Edit dialog
    if (showCreateDialog || scheduleToEdit != null) {
        ScheduleDialog(
            schedule = scheduleToEdit,
            onDismiss = {
                showCreateDialog = false
                scheduleToEdit = null
            },
            onSave = { frequencyValue, frequencyUnit, startTime, maxBackups, timezone ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        val scheduleData = mapOf(
                            "frequency" to frequencyUnit,
                            "frequency_value" to frequencyValue,
                            "frequency_unit" to frequencyUnit,
                            "time" to startTime,
                            "start_time" to startTime,
                            "max_backups" to maxBackups,
                            "timezone" to timezone
                        )
                        
                        val response = if (scheduleToEdit != null) {
                            apiClient.backupsService.updateBackupSchedule(
                                guildId,
                                scheduleToEdit!!.id,
                                scheduleData
                            )
                        } else {
                            apiClient.backupsService.createBackupSchedule(guildId, scheduleData)
                        }
                        
                        if (response.isSuccessful) {
                            successMessage = if (scheduleToEdit != null) 
                                "Schedule updated successfully" 
                            else 
                                "Schedule created successfully"
                            loadSchedules()
                        } else {
                            errorMessage = "Failed to save schedule: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error saving schedule: ${e.message}"
                    } finally {
                        showCreateDialog = false
                        scheduleToEdit = null
                    }
                }
            }
        )
    }
}

@Composable
fun ScheduleCard(
    schedule: BackupSchedule,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                            text = "Every ${schedule.frequencyValue} ${schedule.frequencyUnit}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (schedule.enabled) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Active", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        } else {
                            AssistChip(
                                onClick = {},
                                label = { Text("Disabled", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    schedule.startTime?.let {
                        Text(
                            text = "At $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "Keep ${schedule.maxBackups} backups • ${schedule.timezone ?: "UTC"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = schedule.enabled,
                    onCheckedChange = { onToggle() }
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
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
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    schedule: BackupSchedule?,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, Int, String) -> Unit
) {
    var frequencyValue by remember { mutableStateOf(schedule?.frequencyValue?.toString() ?: "1") }
    var frequencyUnit by remember { mutableStateOf(schedule?.frequencyUnit ?: "days") }
    var startTime by remember { mutableStateOf(schedule?.startTime ?: "00:00") }
    var maxBackups by remember { mutableStateOf(schedule?.maxBackups?.toString() ?: "7") }
    var timezone by remember { mutableStateOf(schedule?.timezone ?: "UTC") }
    var expandedUnit by remember { mutableStateOf(false) }
    
    val frequencyUnits = listOf("days", "weeks", "months", "years")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule != null) "Edit Schedule" else "Create Schedule") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Frequency value
                OutlinedTextField(
                    value = frequencyValue,
                    onValueChange = { frequencyValue = it },
                    label = { Text("Frequency (number)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Frequency unit dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit }
                ) {
                    OutlinedTextField(
                        value = frequencyUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        frequencyUnits.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    frequencyUnit = unit
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
                
                // Start time
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start time (HH:MM)") },
                    placeholder = { Text("14:30") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Max backups
                OutlinedTextField(
                    value = maxBackups,
                    onValueChange = { maxBackups = it },
                    label = { Text("Keep max backups") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Timezone
                OutlinedTextField(
                    value = timezone,
                    onValueChange = { timezone = it },
                    label = { Text("Timezone") },
                    placeholder = { Text("UTC, America/New_York, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val freqVal = frequencyValue.toIntOrNull() ?: 1
                    val maxBkp = maxBackups.toIntOrNull() ?: 7
                    onSave(freqVal, frequencyUnit, startTime, maxBkp, timezone)
                }
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
