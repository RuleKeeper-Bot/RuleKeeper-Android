package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.WarningAction
import cc.rulekeeper.dashboard.data.model.WarningActionsUpdate
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarningActionsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // List of warning actions (mutable)
    var actions by remember { mutableStateOf<List<WarningActionItem>>(emptyList()) }
    
    // Load actions from API
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                val response = apiClient.configService.getWarningActions(guildId)
                if (response.isSuccessful && response.body() != null) {
                    actions = response.body()!!.actions.map { action ->
                        WarningActionItem(
                            warningCount = action.warningCount.toString(),
                            action = action.action,
                            duration = formatDuration(action.durationSeconds)
                        )
                    }
                    // If no actions, add one blank row
                    if (actions.isEmpty()) {
                        actions = listOf(WarningActionItem())
                    }
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
                title = { Text("Warning Actions") },
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
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Automated Moderation",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Configure automatic actions when members reach specific warning counts.",
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Configuration saved successfully!",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                
                errorMessage?.let { error ->
                    item {
                        Card(
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
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            actions = actions + WarningActionItem()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Action")
                    }
                }
                
                itemsIndexed(actions) { index, item ->
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
                                Text(
                                    "Action ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(
                                    onClick = {
                                        actions = actions.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            OutlinedTextField(
                                value = item.warningCount,
                                onValueChange = { value ->
                                    actions = actions.toMutableList().apply {
                                        this[index] = item.copy(warningCount = value)
                                    }
                                },
                                label = { Text("Number of Warnings") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                            
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = when (item.action) {
                                        "timeout" -> "Timeout"
                                        "kick" -> "Kick"
                                        "ban" -> "Ban"
                                        else -> "Select Action"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Action") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Timeout") },
                                        onClick = {
                                            actions = actions.toMutableList().apply {
                                                this[index] = item.copy(action = "timeout")
                                            }
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Kick") },
                                        onClick = {
                                            actions = actions.toMutableList().apply {
                                                this[index] = item.copy(
                                                    action = "kick",
                                                    duration = "" // Clear duration for non-timeout actions
                                                )
                                            }
                                            expanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Ban") },
                                        onClick = {
                                            actions = actions.toMutableList().apply {
                                                this[index] = item.copy(
                                                    action = "ban",
                                                    duration = "" // Clear duration for non-timeout actions
                                                )
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                            
                            if (item.action == "timeout") {
                                OutlinedTextField(
                                    value = item.duration,
                                    onValueChange = { value ->
                                        actions = actions.toMutableList().apply {
                                            this[index] = item.copy(duration = value)
                                        }
                                    },
                                    label = { Text("Duration") },
                                    placeholder = { Text("e.g. 1h, 30m, 45s") },
                                    modifier = Modifier.fillMaxWidth(),
                                    supportingText = { Text("Formats: 45s, 30m, 1h, 2d, 1w") },
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    errorMessage = null
                                    showSuccess = false
                                    
                                    // Validate and convert actions
                                    val validActions = actions.mapNotNull { item ->
                                        val count = item.warningCount.toIntOrNull()
                                        if (count == null || count <= 0 || item.action.isBlank()) {
                                            return@mapNotNull null
                                        }
                                        
                                        // Parse duration if timeout
                                        var durationSeconds: Int? = null
                                        if (item.action == "timeout") {
                                            if (item.duration.isBlank()) {
                                                errorMessage = "Timeout actions require a duration"
                                                return@launch
                                            }
                                            durationSeconds = parseDuration(item.duration)
                                            if (durationSeconds == null) {
                                                errorMessage = "Invalid duration format: ${item.duration}"
                                                return@launch
                                            }
                                        }
                                        
                                        WarningAction(
                                            warningCount = count,
                                            action = item.action,
                                            durationSeconds = durationSeconds
                                        )
                                    }
                                    
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    
                                    apiClient.configService.updateWarningActions(
                                        guildId,
                                        WarningActionsUpdate(actions = validActions)
                                    )
                                    
                                    showSuccess = true
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to save configuration"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = actions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Configuration")
                    }
                }
                
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "• Actions: Timeout (with duration), Kick, or Ban\n" +
                                "• Duration formats: 45s, 30m, 1h, 2d, 1w\n" +
                                "• Actions are triggered automatically when a member reaches the specified warning count",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data class for UI state
data class WarningActionItem(
    val warningCount: String = "",
    val action: String = "",
    val duration: String = ""
)

// Helper function to format duration from seconds
private fun formatDuration(seconds: Int?): String {
    if (seconds == null) return ""
    return when {
        seconds % 604800 == 0 -> "${seconds / 604800}w"
        seconds % 86400 == 0 -> "${seconds / 86400}d"
        seconds % 3600 == 0 -> "${seconds / 3600}h"
        seconds % 60 == 0 -> "${seconds / 60}m"
        else -> "${seconds}s"
    }
}

// Helper function to parse duration string to seconds
private fun parseDuration(duration: String): Int? {
    val trimmed = duration.trim().lowercase()
    val regex = """^(\d+)([smhdw]?)$""".toRegex()
    val match = regex.matchEntire(trimmed) ?: return null
    
    val (value, unit) = match.destructured
    val numValue = value.toIntOrNull() ?: return null
    
    return when (unit) {
        "s", "" -> numValue
        "m" -> numValue * 60
        "h" -> numValue * 3600
        "d" -> numValue * 86400
        "w" -> numValue * 604800
        else -> null
    }
}
