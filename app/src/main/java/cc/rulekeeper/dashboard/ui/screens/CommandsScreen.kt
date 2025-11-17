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
import cc.rulekeeper.dashboard.data.model.GuildCommand
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var commands by remember { mutableStateOf<List<GuildCommand>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var commandToEdit by remember { mutableStateOf<GuildCommand?>(null) }
    var commandToDelete by remember { mutableStateOf<GuildCommand?>(null) }
    var showExportImportMenu by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(guildId) {
        loadCommands(guildId, settingsRepository, scope) { loadedCommands, error ->
            commands = loadedCommands
            errorMessage = error
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Commands") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showExportImportMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showExportImportMenu,
                            onDismissRequest = { showExportImportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Commands") },
                                onClick = {
                                    showExportImportMenu = false
                                    scope.launch {
                                        try {
                                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                settingsRepository.getCachedAccessToken()
                                            }
                                            val response = apiClient.commandService.exportCommands(guildId)
                                            if (response.isSuccessful) {
                                                exportMessage = "Commands exported successfully!"
                                            } else {
                                                exportMessage = "Failed to export commands: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            exportMessage = "Error exporting commands: ${e.message}"
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Commands") },
                                onClick = {
                                    showExportImportMenu = false
                                    showImportDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                }
                            )
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add command")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add command")
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
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            errorMessage ?: "An error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                commands.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Terminal,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("No commands yet")
                            Text(
                                "Tap + to create your first command",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        
                        items(commands) { command ->
                            CommandCard(
                                command = command,
                                onEdit = { commandToEdit = command },
                                onDelete = { commandToDelete = command }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Import Commands Dialog
    if (showImportDialog) {
        ImportCommandsDialog(
            onDismiss = { showImportDialog = false },
            onImport = { jsonData ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.commandService.importCommands(guildId, jsonData)
                        if (response.isSuccessful) {
                            exportMessage = "Commands imported successfully!"
                            showImportDialog = false
                            // Reload commands
                            loadCommands(guildId, settingsRepository, scope) { loadedCommands, error ->
                                commands = loadedCommands
                                errorMessage = error
                            }
                        } else {
                            exportMessage = "Failed to import commands: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        exportMessage = "Error importing commands: ${e.message}"
                    }
                }
            }
        )
    }
    
    // Add Command Dialog
    if (showAddDialog) {
        AddCommandDialog(
            guildId = guildId,
            settingsRepository = settingsRepository,
            onDismiss = { showAddDialog = false },
            onCommandAdded = {
                showAddDialog = false
                // Reload commands
                loadCommands(guildId, settingsRepository, scope) { loadedCommands, error ->
                    commands = loadedCommands
                    errorMessage = error
                }
            }
        )
    }
    
    // Edit Command Dialog
    commandToEdit?.let { command ->
        EditCommandDialog(
            guildId = guildId,
            command = command,
            settingsRepository = settingsRepository,
            onDismiss = { commandToEdit = null },
            onCommandUpdated = {
                commandToEdit = null
                // Reload commands
                loadCommands(guildId, settingsRepository, scope) { loadedCommands, error ->
                    commands = loadedCommands
                    errorMessage = error
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    commandToDelete?.let { command ->
        ConfirmationDialog(
            title = "Delete Command",
            message = "Are you sure you want to delete /${command.commandName}? This action cannot be undone.",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        val response = apiClient.commandService.deleteCommand(guildId, command.commandName)
                        
                        if (response.isSuccessful) {
                            // Sync commands with Discord
                            apiClient.commandService.syncCommands(guildId)
                            commandToDelete = null
                            // Reload commands
                            loadCommands(guildId, settingsRepository, scope) { loadedCommands, error ->
                                commands = loadedCommands
                                errorMessage = error
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    }
                }
            },
            onDismiss = { commandToDelete = null },
            isDestructive = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommandDialog(
    guildId: String,
    settingsRepository: SettingsRepository,
    onDismiss: () -> Unit,
    onCommandAdded: () -> Unit
) {
    var commandName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var ephemeral by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Command") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = commandName,
                    onValueChange = { commandName = it.lowercase().replace(" ", "_") },
                    label = { Text("Command Name") },
                    placeholder = { Text("ping") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    leadingIcon = { Text("/") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Responds with pong") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Response") },
                    placeholder = { Text("Pong! 🏓") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ephemeral (only visible to user)")
                    Switch(
                        checked = ephemeral,
                        onCheckedChange = { ephemeral = it },
                        enabled = !isLoading
                    )
                }
                
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            errorMessage ?: "",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commandName.isBlank() || content.isBlank()) {
                        errorMessage = "Command name and response are required"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            
                            val commandRequest = cc.rulekeeper.dashboard.data.model.CreateCommandRequest(
                                commandName = commandName,
                                content = content,
                                description = description.ifBlank { "Custom command" },
                                ephemeral = ephemeral
                            )
                            
                            val response = apiClient.commandService.createCommand(guildId, commandRequest)
                            
                            if (response.isSuccessful) {
                                // Sync commands with Discord
                                apiClient.commandService.syncCommands(guildId)
                                onCommandAdded()
                            } else {
                                errorMessage = "Failed to create command: ${response.code()}"
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCommandDialog(
    guildId: String,
    command: GuildCommand,
    settingsRepository: SettingsRepository,
    onDismiss: () -> Unit,
    onCommandUpdated: () -> Unit
) {
    var commandName by remember { mutableStateOf(command.commandName) }
    var description by remember { mutableStateOf(command.description ?: "") }
    var content by remember { mutableStateOf(command.content ?: "") }
    var ephemeral by remember { mutableStateOf(command.ephemeral) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Command") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = commandName,
                    onValueChange = { },
                    label = { Text("Command Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    leadingIcon = { Text("/") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Responds with pong") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Response") },
                    placeholder = { Text("Pong! 🏓") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ephemeral (only visible to user)")
                    Switch(
                        checked = ephemeral,
                        onCheckedChange = { ephemeral = it },
                        enabled = !isLoading
                    )
                }
                
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            errorMessage ?: "",
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isBlank()) {
                        errorMessage = "Response is required"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    scope.launch {
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            
                            val commandRequest = cc.rulekeeper.dashboard.data.model.CreateCommandRequest(
                                commandName = commandName,
                                content = content,
                                description = description.ifBlank { "Custom command" },
                                ephemeral = ephemeral
                            )
                            
                            val response = apiClient.commandService.updateCommand(guildId, commandName, commandRequest)
                            
                            if (response.isSuccessful) {
                                // Sync commands with Discord
                                apiClient.commandService.syncCommands(guildId)
                                onCommandUpdated()
                            } else {
                                errorMessage = "Failed to update command: ${response.code()}"
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CommandCard(
    command: GuildCommand,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    Text(
                        "/${command.commandName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (command.ephemeral) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Ephemeral") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (!command.description.isNullOrBlank()) {
                Text(
                    command.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                command.content ?: "No content",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun loadCommands(
    guildId: String,
    settingsRepository: SettingsRepository,
    scope: kotlinx.coroutines.CoroutineScope,
    onResult: (List<GuildCommand>, String?) -> Unit
) {
    scope.launch {
        try {
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                kotlinx.coroutines.runBlocking {
                    settingsRepository.accessToken.first()
                }
            }
            val response = apiClient.commandService.getCommands(guildId)
            
            if (response.isSuccessful && response.body() != null) {
                onResult(response.body()!!, null)
            } else {
                onResult(emptyList(), "Failed to load commands")
            }
        } catch (e: Exception) {
            onResult(emptyList(), "Error: ${e.message}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCommandsDialog(
    onDismiss: () -> Unit,
    onImport: (Map<String, Any>) -> Unit
) {
    var jsonData by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Commands") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Paste the JSON data exported from another server:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = jsonData,
                    onValueChange = { jsonData = it },
                    label = { Text("JSON Data") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("{\"commands\": [...]}") },
                    minLines = 8
                )
                
                Text(
                    "This will import commands from the provided JSON data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (jsonData.isNotBlank()) {
                        // Parse JSON string to Map
                        try {
                            // For simplicity, we'll pass the raw JSON as a map with a "data" key
                            // The API should handle JSON parsing on the backend
                            onImport(mapOf("data" to jsonData))
                        } catch (e: Exception) {
                            // Handle parse error
                        }
                    }
                },
                enabled = jsonData.isNotBlank()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
