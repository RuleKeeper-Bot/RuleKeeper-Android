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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRolesScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var gameRoles by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var roleToDelete by remember { mutableStateOf<Map<String, Any>?>(null) }
    var roleToEdit by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    val loadGameRoles: suspend () -> Unit = {
        try {
            isLoading = true
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            val response = apiClient.roleService.getGameRoles(guildId)
            if (response.isSuccessful && response.body() != null) {
                gameRoles = response.body()!!
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        scope.launch {
            loadGameRoles()
        }
    }
    
    // Delete confirmation dialog
    if (roleToDelete != null) {
        ConfirmationDialog(
            title = "Delete Game Role",
            message = "Are you sure you want to delete the game role for \"${roleToDelete!!["game_name"]}\"? This cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val roleId = (roleToDelete!!["id"] as? Number)?.toInt() ?: return@launch
                        val response = apiClient.roleService.deleteGameRole(guildId, roleId)
                        if (response.isSuccessful) {
                            successMessage = "Game role deleted successfully"
                            loadGameRoles()
                        } else {
                            errorMessage = "Failed to delete game role"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        roleToDelete = null
                    }
                }
            },
            onDismiss = {
                roleToDelete = null
            },
            isDestructive = true
        )
    }
    
    // Edit dialog
    if (roleToEdit != null) {
        EditGameRoleDialog(
            currentGameRole = roleToEdit!!,
            onDismiss = { roleToEdit = null },
            onConfirm = { gameName, roleId ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val gameRoleId = (roleToEdit!!["id"] as? Number)?.toInt() ?: return@launch
                        val request = mapOf(
                            "game_name" to gameName,
                            "role_id" to roleId
                        )
                        val response = apiClient.roleService.updateGameRole(guildId, gameRoleId, request)
                        if (response.isSuccessful) {
                            successMessage = "Game role updated successfully"
                            loadGameRoles()
                        } else {
                            errorMessage = "Failed to update game role"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        roleToEdit = null
                    }
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Roles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Game")
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
                                Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Game Roles",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Automatically assign roles based on games members are playing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Success message
            if (successMessage != null) {
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
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                successMessage!!,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    gameRoles.isEmpty() -> {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No Game Roles Configured",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Configure game-specific roles that are automatically assigned when members play certain games.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Game Role")
                                }
                            }
                        }
                    }
                }
            }
            
            if (gameRoles.isNotEmpty()) {
                items(gameRoles) { gameRole ->
                    GameRoleCard(
                        gameRole = gameRole,
                        onEdit = { roleToEdit = gameRole },
                        onDelete = { roleToDelete = gameRole }
                    )
                }
            }
        }
        
        if (showAddDialog) {
            AddGameRoleDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { gameName, roleId ->
                    scope.launch {
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            val request = mapOf(
                                "game_name" to gameName,
                                "role_id" to roleId
                            )
                            val response = apiClient.roleService.createGameRole(guildId, request)
                            if (response.isSuccessful) {
                                loadGameRoles()
                                showAddDialog = false
                            } else {
                                errorMessage = "Failed to create game role"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GameRoleCard(
    gameRole: Map<String, Any>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        gameRole["game_name"] as? String ?: "Unknown Game",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Role ID: ${gameRole["role_id"]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun AddGameRoleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var gameName by remember { mutableStateOf("") }
    var roleId by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Game Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = gameName,
                    onValueChange = { gameName = it },
                    label = { Text("Game Name") },
                    placeholder = { Text("e.g., Minecraft") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roleId,
                    onValueChange = { roleId = it },
                    label = { Text("Role ID") },
                    placeholder = { Text("Discord role ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(gameName, roleId) },
                enabled = gameName.isNotBlank() && roleId.isNotBlank()
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

@Composable
fun EditGameRoleDialog(
    currentGameRole: Map<String, Any>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var gameName by remember { mutableStateOf(currentGameRole["game_name"] as? String ?: "") }
    var roleId by remember { mutableStateOf(currentGameRole["role_id"]?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Game Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = gameName,
                    onValueChange = { gameName = it },
                    label = { Text("Game Name") },
                    placeholder = { Text("e.g., Minecraft") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = roleId,
                    onValueChange = { roleId = it },
                    label = { Text("Role ID") },
                    placeholder = { Text("Discord role ID") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(gameName, roleId) },
                enabled = gameName.isNotBlank() && roleId.isNotBlank()
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
