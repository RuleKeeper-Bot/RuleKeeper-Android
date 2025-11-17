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
import cc.rulekeeper.dashboard.data.model.Warning
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarningsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var warnings by remember { mutableStateOf<List<Warning>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var warningToDelete by remember { mutableStateOf<Warning?>(null) }
    
    // Load warnings on screen launch
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.moderationService.getWarnings(guildId)
                if (response.isSuccessful && response.body() != null) {
                    warnings = response.body()!!
                } else {
                    errorMessage = "Failed to load warnings"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load warnings"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Delete confirmation dialog
    if (warningToDelete != null) {
        ConfirmationDialog(
            title = "Remove Warning",
            message = "Are you sure you want to remove this warning for ${warningToDelete!!.username}? This action cannot be undone.",
            confirmButtonText = "Remove",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.moderationService.deleteWarning(
                            guildId,
                            warningToDelete!!.id
                        )
                        if (response.isSuccessful) {
                            warnings = warnings - warningToDelete!!
                        } else {
                            errorMessage = "Failed to delete warning"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to delete warning"
                    } finally {
                        warningToDelete = null
                    }
                }
            },
            onDismiss = {
                warningToDelete = null
            },
            isDestructive = true
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warned Users") },
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add warning")
            }
        }
    ) { padding ->
        WarningsList(
            warnings = warnings,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRemoveWarning = { warning ->
                warningToDelete = warning
            },
            modifier = Modifier.padding(padding)
        )
    }
    
    if (showAddDialog) {
        AddWarningDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { userId, reason ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.moderationService.addWarning(
                            guildId,
                            userId,
                            mapOf("reason" to reason)
                        )
                        if (response.isSuccessful) {
                            // Reload warnings
                            val getResponse = apiClient.moderationService.getWarnings(guildId)
                            if (getResponse.isSuccessful && getResponse.body() != null) {
                                warnings = getResponse.body()!!
                            }
                            showAddDialog = false
                        } else {
                            errorMessage = "Failed to add warning"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to add warning"
                    }
                }
            }
        )
    }
}

@Composable
fun WarningsList(
    warnings: List<Warning>,
    isLoading: Boolean,
    errorMessage: String?,
    onRemoveWarning: (Warning) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        warnings.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("No active warnings")
                    Text(
                        "Tap + to add a warning",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(warnings) { warning ->
                    WarningCard(
                        warning = warning,
                        onDelete = { onRemoveWarning(warning) }
                    )
                }
            }
        }
    }
}

@Composable
fun WarningCard(
    warning: Warning,
    onDelete: () -> Unit
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "User ID: ${warning.userId}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Warned by: ${warning.warnedBy}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }
            
            Text(
                warning.reason ?: "No reason provided",
                style = MaterialTheme.typography.bodyMedium
            )
            
            warning.timestamp?.let { timestamp ->
                val formattedDate = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    val date = inputFormat.parse(timestamp)
                    date?.let { outputFormat.format(it) } ?: timestamp
                } catch (e: Exception) {
                    timestamp
                }
                
                Text(
                    "Warned on: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Warning") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (userId.isNotBlank() && reason.isNotBlank()) {
                        onConfirm(userId, reason)
                    }
                }
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
