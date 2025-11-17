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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ServerLog(
    val id: String,
    val timestamp: String,
    val type: String,
    val userId: String?,
    val userName: String?,
    val channelId: String?,
    val channelName: String?,
    val action: String,
    val details: String?,
    val moderatorId: String?,
    val moderatorName: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerLogsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf<List<ServerLog>>(emptyList()) }
    var logTypes by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    
    fun loadLogs(type: String? = null) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Build query parameters
                val params = mutableMapOf<String, String>()
                if (type != null && type.isNotBlank()) {
                    params["type"] = type
                }
                params["limit"] = "100"
                
                android.util.Log.d("ServerLogsScreen", "Loading logs with params: $params")
                
                val response = apiClient.logsService.getServerLogs(guildId, params)
                
                android.util.Log.d("ServerLogsScreen", "Response code: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    android.util.Log.d("ServerLogsScreen", "Response body: $body")
                    
                    val logsData = body["logs"] as? List<*>
                    android.util.Log.d("ServerLogsScreen", "Logs data: ${logsData?.size ?: 0} items")
                    
                    logs = logsData?.mapNotNull { logMap ->
                        if (logMap is Map<*, *>) {
                            ServerLog(
                                id = logMap["id"]?.toString() ?: "",
                                timestamp = logMap["timestamp"]?.toString() ?: "",
                                type = logMap["type"]?.toString() ?: "",
                                userId = logMap["user_id"]?.toString(),
                                userName = logMap["user_name"]?.toString(),
                                channelId = logMap["channel_id"]?.toString(),
                                channelName = logMap["channel_name"]?.toString(),
                                action = logMap["action"]?.toString() ?: "",
                                details = logMap["details"]?.toString(),
                                moderatorId = logMap["moderator_id"]?.toString(),
                                moderatorName = logMap["moderator_name"]?.toString()
                            )
                        } else null
                    } ?: emptyList()
                    
                    android.util.Log.d("ServerLogsScreen", "Parsed ${logs.size} logs")
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("ServerLogsScreen", "Failed to load logs: $errorBody")
                    errorMessage = "Failed to load logs: ${response.code()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("ServerLogsScreen", "Error loading logs", e)
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load log types
                val typesResponse = apiClient.logsService.getLogTypes(guildId)
                if (typesResponse.isSuccessful && typesResponse.body() != null) {
                    val types = typesResponse.body()!!["types"] as? List<*>
                    logTypes = types?.mapNotNull { it?.toString() } ?: emptyList()
                }
            } catch (e: Exception) {
                // Ignore error for types
            }
            
            // Load initial logs
            loadLogs()
        }
    }
    
    if (showFilterDialog) {
        FilterDialog(
            logTypes = logTypes,
            selectedType = selectedType,
            onDismiss = { showFilterDialog = false },
            onApply = { type ->
                selectedType = type
                showFilterDialog = false
                loadLogs(type)
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            if (selectedType != null) Icons.Default.FilterAlt else Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    IconButton(onClick = { loadLogs(selectedType) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                    settingsRepository.getCachedAccessToken()
                                }
                                val params = mutableMapOf<String, String>()
                                if (selectedType != null && selectedType!!.isNotBlank()) {
                                    params["type"] = selectedType!!
                                }
                                val response = apiClient.logsService.exportLogs(guildId, params)
                                if (response.isSuccessful) {
                                    exportMessage = "Logs exported successfully!"
                                } else {
                                    exportMessage = "Failed to export logs: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                exportMessage = "Error exporting logs: ${e.message}"
                            }
                        }
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export")
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
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                logs.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No logs found",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            if (selectedType != null) "Try removing filters" else "No server activity logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedType != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(onClick = {
                                selectedType = null
                                loadLogs(null)
                            }) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (exportMessage != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (exportMessage!!.contains("success", ignoreCase = true))
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else
                                            MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (exportMessage!!.contains("success", ignoreCase = true))
                                                Icons.Default.CheckCircle
                                            else
                                                Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (exportMessage!!.contains("success", ignoreCase = true))
                                                MaterialTheme.colorScheme.tertiary
                                            else
                                                MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            exportMessage!!,
                                            color = if (exportMessage!!.contains("success", ignoreCase = true))
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            else
                                                MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (selectedType != null) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.FilterAlt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                "Filtered: $selectedType",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedType = null
                                                loadLogs(null)
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Clear filter",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        items(logs) { log ->
                            LogEntryCard(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(log: ServerLog) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        log.type.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        log.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    getIconForLogType(log.type),
                    contentDescription = null,
                    tint = getColorForLogType(log.type)
                )
            }
            
            Divider()
            
            Text(
                log.action,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (log.userName != null || log.channelName != null || log.moderatorName != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    log.userName?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    log.channelName?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    log.moderatorName?.let {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Moderator: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Display details if available
            log.details?.let { details ->
                if (details.isNotBlank()) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Details:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    logTypes: List<String>,
    selectedType: String?,
    onDismiss: () -> Unit,
    onApply: (String?) -> Unit
) {
    var tempSelectedType by remember { mutableStateOf(selectedType) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Logs") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    FilterOption(
                        label = "All Types",
                        selected = tempSelectedType == null,
                        onClick = { tempSelectedType = null }
                    )
                }
                
                items(logTypes) { type ->
                    FilterOption(
                        label = type.replace("_", " "),
                        selected = tempSelectedType == type,
                        onClick = { tempSelectedType = type }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(tempSelectedType) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FilterOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}

@Composable
fun getIconForLogType(type: String) = when {
    type.contains("MESSAGE") -> Icons.Default.Message
    type.contains("MEMBER") -> Icons.Default.Person
    type.contains("ROLE") -> Icons.Default.Shield
    type.contains("CHANNEL") -> Icons.Default.Tag
    type.contains("VOICE") -> Icons.Default.VoiceChat
    type.contains("BAN") || type.contains("KICK") || type.contains("WARN") -> Icons.Default.Block
    else -> Icons.Default.Info
}

@Composable
fun getColorForLogType(type: String) = when {
    type.contains("DELETE") || type.contains("BAN") || type.contains("KICK") -> MaterialTheme.colorScheme.error
    type.contains("CREATE") || type.contains("JOIN") -> MaterialTheme.colorScheme.primary
    type.contains("UPDATE") || type.contains("EDIT") -> MaterialTheme.colorScheme.tertiary
    type.contains("WARN") || type.contains("TIMEOUT") -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
