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
fun AutoRolesScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var autoRoles by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var availableRoles by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var roleToDelete by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    val loadAutoRoles: suspend () -> Unit = {
        try {
            isLoading = true
            errorMessage = null
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            // Load existing auto roles
            val autoRolesResponse = apiClient.roleService.getAutoRoles(guildId)
            if (autoRolesResponse.isSuccessful && autoRolesResponse.body() != null) {
                autoRoles = autoRolesResponse.body()!!
            }
            
            // Load available guild roles
            val rolesResponse = apiClient.guildService.getRoles(guildId)
            if (rolesResponse.isSuccessful) {
                availableRoles = rolesResponse.body()
                    ?.filter { it.name != "@everyone" }
                    ?.map { it.id to it.name } 
                    ?: emptyList()
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        loadAutoRoles()
    }
    
    // Delete confirmation dialog
    if (roleToDelete != null) {
        ConfirmationDialog(
            title = "Delete Auto Role",
            message = "Are you sure you want to remove this auto-assign role? New members will no longer receive this role automatically.",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val roleId = roleToDelete!!["role_id"]?.toString() ?: return@launch
                        val response = apiClient.roleService.deleteAutoRole(guildId, roleId)
                        if (response.isSuccessful) {
                            successMessage = "Auto role removed successfully"
                            loadAutoRoles()
                        } else {
                            errorMessage = "Failed to delete auto role"
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
    
    // Add dialog
    if (showAddDialog) {
        AddAutoRoleDialog(
            availableRoles = availableRoles,
            onDismiss = { showAddDialog = false },
            onConfirm = { roleId, _ ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val request = mapOf(
                            "role_id" to roleId
                        )
                        val response = apiClient.roleService.createAutoRole(guildId, request)
                        if (response.isSuccessful) {
                            successMessage = "Auto role added successfully"
                            loadAutoRoles()
                        } else {
                            errorMessage = "Failed to create auto role"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        showAddDialog = false
                    }
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto-Assign Roles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Auto Role")
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
                            "Automatically assign roles to members when they join or meet certain conditions",
                            style = MaterialTheme.typography.bodyMedium
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
            
            // Error message
            if (errorMessage != null) {
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
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer
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
                    autoRoles.isEmpty() -> {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
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
                                    "No Auto Roles Configured",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Set up roles that are automatically assigned to new members or based on triggers.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showAddDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Auto Role")
                                }
                            }
                        }
                    }
                }
            }
            
            if (autoRoles.isNotEmpty()) {
                items(autoRoles) { autoRole ->
                    AutoRoleCard(
                        autoRole = autoRole,
                        availableRoles = availableRoles,
                        onDelete = { roleToDelete = autoRole }
                    )
                }
            }
        }
    }
}

@Composable
fun AutoRoleCard(
    autoRole: Map<String, Any>,
    availableRoles: List<Pair<String, String>>,
    onDelete: () -> Unit
) {
    val roleId = autoRole["role_id"]?.toString() ?: ""
    val roleName = availableRoles.find { it.first == roleId }?.second ?: "Unknown Role"
    
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
                        roleName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Auto-assigned on member join",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Role ID: $roleId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Default.LocalOffer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAutoRoleDialog(
    availableRoles: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var selectedRoleId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Auto Role") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Role selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = availableRoles.find { it.first == selectedRoleId }?.second ?: "Select a role",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableRoles.forEach { (roleId, roleName) ->
                            DropdownMenuItem(
                                text = { Text(roleName) },
                                onClick = {
                                    selectedRoleId = roleId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Text(
                    "This role will be automatically assigned when a new member joins the server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRoleId, "") },
                enabled = selectedRoleId.isNotBlank()
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
