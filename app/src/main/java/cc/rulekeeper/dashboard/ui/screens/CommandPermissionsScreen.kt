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
import cc.rulekeeper.dashboard.data.model.Role
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandPermissionsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var commands by remember { mutableStateOf<List<GuildCommand>>(emptyList()) }
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCommand by remember { mutableStateOf<GuildCommand?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showExportImportMenu by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Get all commands (built-in + custom)
                val commandsResponse = apiClient.commandService.getCommands(guildId, includeBuiltin = true)
                val rolesResponse = apiClient.guildService.getRoles(guildId)
                
                if (commandsResponse.isSuccessful && commandsResponse.body() != null) {
                    commands = commandsResponse.body()!!.sortedWith(
                        compareBy<GuildCommand> { it.isBuiltin }
                            .thenBy { it.commandName }
                    )
                } else {
                    errorMessage = "Failed to load commands"
                }
                
                if (rolesResponse.isSuccessful && rolesResponse.body() != null) {
                    roles = rolesResponse.body()!!.filter { it.name != "@everyone" }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Import Permissions Dialog
    if (showImportDialog) {
        ImportPermissionsDialog(
            onDismiss = { showImportDialog = false },
            onImport = { jsonData ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.permissionsService.importPermissions(guildId, jsonData)
                        if (response.isSuccessful) {
                            exportMessage = "Permissions imported successfully!"
                            showImportDialog = false
                            // Reload commands to reflect new permissions
                            isLoading = true
                            val commandsResponse = apiClient.commandService.getCommands(guildId, includeBuiltin = true)
                            if (commandsResponse.isSuccessful && commandsResponse.body() != null) {
                                commands = commandsResponse.body()!!.sortedWith(
                                    compareBy<GuildCommand> { it.isBuiltin }
                                        .thenBy { it.commandName }
                                )
                            }
                            isLoading = false
                        } else {
                            exportMessage = "Failed to import permissions: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        exportMessage = "Error importing permissions: ${e.message}"
                    }
                }
            }
        )
    }
    
    if (showPermissionDialog && selectedCommand != null) {
        PermissionManagementDialog(
            command = selectedCommand!!,
            roles = roles,
            onDismiss = { 
                showPermissionDialog = false
                selectedCommand = null
            },
            onSave = { permissions ->
                // Save permissions via API
                showPermissionDialog = false
                selectedCommand = null
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Command Permissions") },
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
                                text = { Text("Export Permissions") },
                                onClick = {
                                    showExportImportMenu = false
                                    scope.launch {
                                        try {
                                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                settingsRepository.getCachedAccessToken()
                                            }
                                            val response = apiClient.permissionsService.exportPermissions(guildId)
                                            if (response.isSuccessful) {
                                                exportMessage = "Permissions exported successfully!"
                                            } else {
                                                exportMessage = "Failed to export permissions: ${response.code()}"
                                            }
                                        } catch (e: Exception) {
                                            exportMessage = "Error exporting permissions: ${e.message}"
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Permissions") },
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
                commands.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No commands found")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No commands available for this server",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        "Command Permissions",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Manage which roles and users can use each command (both built-in and custom). By default, all commands are available to everyone.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        
                        // Built-in commands section
                        val builtinCommands = commands.filter { it.isBuiltin }
                        if (builtinCommands.isNotEmpty()) {
                            item {
                                Text(
                                    "Built-in Commands",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            
                            items(builtinCommands) { command ->
                                CommandPermissionCard(
                                    command = command,
                                    onManagePermissions = {
                                        selectedCommand = command
                                        showPermissionDialog = true
                                    }
                                )
                            }
                        }
                        
                        // Custom commands section
                        val customCommands = commands.filter { !it.isBuiltin }
                        if (customCommands.isNotEmpty()) {
                            item {
                                Text(
                                    "Custom Commands",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            
                            items(customCommands) { command ->
                                CommandPermissionCard(
                                    command = command,
                                    onManagePermissions = {
                                        selectedCommand = command
                                        showPermissionDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommandPermissionCard(
    command: GuildCommand,
    onManagePermissions: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "/${command.commandName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (command.isBuiltin) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "Built-in",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        command.description ?: "No description",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onManagePermissions) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Manage Permissions"
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Permissions: Everyone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                TextButton(onClick = onManagePermissions) {
                    Text("Configure")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionManagementDialog(
    command: GuildCommand,
    roles: List<Role>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    var permissionType by remember { mutableStateOf("everyone") }
    var selectedRoleIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandedRoleSelection by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Manage Permissions: /${command.commandName}")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Configure who can use this command",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = permissionType == "everyone",
                                onClick = { permissionType = "everyone" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Everyone", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "All members can use this command",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = permissionType == "specific_roles",
                                onClick = { 
                                    permissionType = "specific_roles"
                                    expandedRoleSelection = true
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Specific Roles", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Only members with selected roles",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = permissionType == "admin_only",
                                onClick = { permissionType = "admin_only" }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Admin Only", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Only administrators can use this",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                if (permissionType == "specific_roles") {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Select Roles (${selectedRoleIds.size} selected)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            
                            if (roles.isEmpty()) {
                                Text(
                                    "No roles available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(roles) { role ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = selectedRoleIds.contains(role.id),
                                                onCheckedChange = { checked ->
                                                    selectedRoleIds = if (checked) {
                                                        selectedRoleIds + role.id
                                                    } else {
                                                        selectedRoleIds - role.id
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                role.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val permissions = mutableMapOf<String, Any>("type" to permissionType)
                    if (permissionType == "specific_roles") {
                        permissions["role_ids"] = selectedRoleIds.toList()
                    }
                    onSave(permissions)
                },
                enabled = permissionType != "specific_roles" || selectedRoleIds.isNotEmpty()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPermissionsDialog(
    onDismiss: () -> Unit,
    onImport: (Map<String, Any>) -> Unit
) {
    var jsonData by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Permissions") },
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
                    placeholder = { Text("{\"permissions\": [...]}") },
                    minLines = 8
                )
                
                Text(
                    "This will import command permissions from the provided JSON data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (jsonData.isNotBlank()) {
                        onImport(mapOf("data" to jsonData))
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
