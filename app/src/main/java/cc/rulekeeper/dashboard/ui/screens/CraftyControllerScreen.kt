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

data class CraftyInstance(
    val id: Int,
    val name: String,
    val apiUrl: String,
    val apiToken: String,
    val description: String?,
    val enabled: Boolean
)

data class MinecraftServer(
    val id: Int,
    val serverId: String,
    val serverName: String,
    val description: String?,
    val port: Int?,
    val instanceName: String,
    val running: Boolean?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CraftyControllerScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // State for configuration
    var isLoadingConfig by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var instances by remember { mutableStateOf<List<CraftyInstance>>(emptyList()) }
    var servers by remember { mutableStateOf<List<MinecraftServer>>(emptyList()) }
    
    // State for dialogs
    var showAddInstanceDialog by remember { mutableStateOf(false) }
    var editingInstance by remember { mutableStateOf<CraftyInstance?>(null) }
    var showServerActionsDialog by remember { mutableStateOf(false) }
    var selectedServer by remember { mutableStateOf<MinecraftServer?>(null) }
    
    // State for actions
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isPerformingAction by remember { mutableStateOf(false) }
    
    // Load configuration
    fun loadConfiguration() {
        scope.launch {
            isLoadingConfig = true
            loadError = null
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load instances
                val response = apiClient.configService.getCraftyConfig(guildId)
                if (response.isSuccessful && response.body() != null) {
                    val config = response.body()!!
                    val instancesList = config["instances"] as? List<Map<String, Any>> ?: emptyList()
                    instances = instancesList.map { inst ->
                        CraftyInstance(
                            id = (inst["id"] as? Double)?.toInt() ?: 0,
                            name = inst["name"] as? String ?: "",
                            apiUrl = inst["api_url"] as? String ?: "",
                            apiToken = inst["api_token"] as? String ?: "",
                            description = inst["description"] as? String,
                            enabled = inst["enabled"] as? Boolean ?: true
                        )
                    }
                    
                    // Load servers
                    if (instances.isNotEmpty()) {
                        val serversResponse = apiClient.configService.getCraftyServers(guildId)
                        if (serversResponse.isSuccessful && serversResponse.body() != null) {
                            val serversData = serversResponse.body()!!
                            val serversList = serversData["servers"] as? List<Map<String, Any>> ?: emptyList()
                            servers = serversList.map { srv ->
                                MinecraftServer(
                                    id = (srv["id"] as? Double)?.toInt() ?: 0,
                                    serverId = srv["server_id"] as? String ?: "",
                                    serverName = srv["server_name"] as? String ?: "",
                                    description = srv["description"] as? String,
                                    port = (srv["port"] as? Double)?.toInt(),
                                    instanceName = srv["instance_name"] as? String ?: "",
                                    running = srv["running"] as? Boolean
                                )
                            }
                        }
                    }
                } else {
                    loadError = "Failed to load configuration: ${response.code()}"
                }
            } catch (e: Exception) {
                loadError = "Error: ${e.message}"
            } finally {
                isLoadingConfig = false
            }
        }
    }
    
    // Load on first composition
    LaunchedEffect(guildId) {
        loadConfiguration()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crafty Controller") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { loadConfiguration() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            if (!isLoadingConfig && instances.isEmpty()) {
                FloatingActionButton(
                    onClick = { showAddInstanceDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Instance")
                }
            }
        }
    ) { padding ->
        if (isLoadingConfig) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Loading Crafty configuration...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error message
                if (loadError != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    loadError!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                // Action message
                if (actionMessage != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    actionMessage!!,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                // Header card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Crafty Controller Integration",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Manage Minecraft servers through Crafty Controller. Start, stop, and restart servers directly from Discord.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Crafty Instances section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Crafty Instances",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (instances.isNotEmpty()) {
                            IconButton(onClick = { showAddInstanceDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Instance")
                            }
                        }
                    }
                }
                
                if (instances.isEmpty()) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Crafty Instances",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Add a Crafty Controller instance to manage your Minecraft servers",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { showAddInstanceDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Instance")
                                }
                            }
                        }
                    }
                } else {
                    items(instances) { instance ->
                        CraftyInstanceCard(
                            instance = instance,
                            onEdit = { editingInstance = instance },
                            onDelete = {
                                scope.launch {
                                    isPerformingAction = true
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val response = apiClient.configService.deleteCraftyInstance(guildId, instance.id)
                                        if (response.isSuccessful) {
                                            actionMessage = "Instance '${instance.name}' deleted successfully!"
                                            loadConfiguration()
                                        } else {
                                            actionMessage = "Failed to delete instance: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        actionMessage = "Error: ${e.message}"
                                    } finally {
                                        isPerformingAction = false
                                    }
                                }
                            },
                            onTest = {
                                scope.launch {
                                    isPerformingAction = true
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val response = apiClient.configService.testCraftyInstance(guildId, instance.id)
                                        if (response.isSuccessful) {
                                            val result = response.body()
                                            val success = result?.get("success") as? Boolean ?: false
                                            val message = result?.get("message") as? String ?: "Test completed"
                                            actionMessage = if (success) {
                                                "✓ Connection successful: $message"
                                            } else {
                                                "✗ Connection failed: $message"
                                            }
                                        } else {
                                            actionMessage = "Failed to test connection: ${response.code()}"
                                        }
                                    } catch (e: Exception) {
                                        actionMessage = "Test error: ${e.message}"
                                    } finally {
                                        isPerformingAction = false
                                    }
                                }
                            }
                        )
                    }
                }
                
                // Minecraft Servers section
                if (instances.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Minecraft Servers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (servers.isEmpty()) {
                        item {
                            Card {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.VideogameAsset,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No Servers Found",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Sync your Crafty instances to see your Minecraft servers here",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(servers) { server ->
                            MinecraftServerCard(
                                server = server,
                                onAction = {
                                    selectedServer = server
                                    showServerActionsDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add/Edit Instance Dialog
    if (showAddInstanceDialog || editingInstance != null) {
        AddEditInstanceDialog(
            instance = editingInstance,
            onDismiss = {
                showAddInstanceDialog = false
                editingInstance = null
            },
            onSave = { name, apiUrl, apiToken, description ->
                scope.launch {
                    isPerformingAction = true
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        val response = if (editingInstance != null) {
                            // Update existing instance
                            val updateData = mapOf(
                                "name" to name,
                                "server_url" to apiUrl,
                                "api_token" to apiToken,
                                "description" to description
                            )
                            apiClient.configService.updateCraftyInstance(guildId, editingInstance!!.id, updateData)
                        } else {
                            // Add new instance
                            val instanceData = mapOf(
                                "name" to name,
                                "server_url" to apiUrl,
                                "api_token" to apiToken,
                                "description" to description
                            )
                            apiClient.configService.addCraftyInstance(guildId, instanceData)
                        }
                        
                        if (response.isSuccessful) {
                            actionMessage = "Instance ${if (editingInstance != null) "updated" else "added"} successfully!"
                            showAddInstanceDialog = false
                            editingInstance = null
                            loadConfiguration()
                        } else {
                            actionMessage = "Failed to save instance: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        actionMessage = "Error: ${e.message}"
                    } finally {
                        isPerformingAction = false
                    }
                }
            }
        )
    }
    
    // Server Actions Dialog
    if (showServerActionsDialog && selectedServer != null) {
        ServerActionsDialog(
            server = selectedServer!!,
            onDismiss = { showServerActionsDialog = false },
            onAction = { action ->
                scope.launch {
                    isPerformingAction = true
                    showServerActionsDialog = false
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        
                        actionMessage = "Sending $action command to ${selectedServer!!.serverName}..."
                        
                        val response = apiClient.configService.performServerAction(
                            guildId,
                            selectedServer!!.id,
                            action
                        )
                        
                        if (response.isSuccessful) {
                            val result = response.body()
                            val success = result?.get("success") as? Boolean ?: false
                            val message = result?.get("message") as? String ?: "Action completed"
                            
                            actionMessage = if (success) {
                                "✓ ${selectedServer!!.serverName}: $message"
                            } else {
                                "✗ ${selectedServer!!.serverName}: $message"
                            }
                            
                            // Refresh server list after action
                            kotlinx.coroutines.delay(2000) // Wait 2 seconds for server status to update
                            loadConfiguration()
                        } else {
                            actionMessage = "Failed to perform $action: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        actionMessage = "Error performing $action: ${e.message}"
                    } finally {
                        isPerformingAction = false
                    }
                }
            }
        )
    }
}

@Composable
fun CraftyInstanceCard(
    instance: CraftyInstance,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        instance.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (instance.description != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            instance.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        instance.apiUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Test Connection") },
                        leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onTest()
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AssistChip(
                onClick = { },
                label = { Text(if (instance.enabled) "Enabled" else "Disabled") },
                leadingIcon = {
                    Icon(
                        if (instance.enabled) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (instance.enabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun MinecraftServerCard(
    server: MinecraftServer,
    onAction: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint = when (server.running) {
                            true -> MaterialTheme.colorScheme.primary
                            false -> MaterialTheme.colorScheme.error
                            null -> MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(12.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        server.serverName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (server.description != null) {
                        Text(
                            server.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "Instance: ${server.instanceName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (server.port != null) {
                        Text(
                            "Port: ${server.port}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (server.running) {
                    true -> {
                        // Server is running
                        OutlinedButton(
                            onClick = onAction,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restart")
                        }
                        Button(
                            onClick = onAction,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                    false -> {
                        // Server is stopped
                        Button(
                            onClick = onAction,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Server")
                        }
                    }
                    null -> {
                        // Status unknown
                        OutlinedButton(
                            onClick = onAction,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false
                        ) {
                            Text("Status Unknown")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditInstanceDialog(
    instance: CraftyInstance?,
    onDismiss: () -> Unit,
    onSave: (name: String, apiUrl: String, apiToken: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(instance?.name ?: "") }
    var apiUrl by remember { mutableStateOf(instance?.apiUrl ?: "") }
    var apiToken by remember { mutableStateOf(instance?.apiToken ?: "") }
    var description by remember { mutableStateOf(instance?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (instance != null) "Edit Instance" else "Add Instance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Instance Name") },
                    placeholder = { Text("Primary Server") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    label = { Text("API URL") },
                    placeholder = { Text("https://crafty.example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text("API Token") },
                    placeholder = { Text("Your Crafty API token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Main production server") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, apiUrl, apiToken, description) },
                enabled = name.isNotBlank() && apiUrl.isNotBlank() && apiToken.isNotBlank()
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

@Composable
fun ServerActionsDialog(
    server: MinecraftServer,
    onDismiss: () -> Unit,
    onAction: (action: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(server.serverName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select an action for this server:")
                
                if (server.running == true) {
                    OutlinedCard(
                        onClick = {
                            onAction("restart")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Text("Restart Server")
                        }
                    }
                    
                    OutlinedCard(
                        onClick = {
                            onAction("stop")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Text("Stop Server")
                        }
                    }
                } else if (server.running == false) {
                    OutlinedCard(
                        onClick = {
                            onAction("start")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Text("Start Server")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
