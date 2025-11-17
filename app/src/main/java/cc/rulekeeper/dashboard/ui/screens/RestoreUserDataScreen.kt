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
import cc.rulekeeper.dashboard.data.model.RestoreSettings
import cc.rulekeeper.dashboard.data.model.Role
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreUserDataScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var restoreRoles by remember { mutableStateOf(true) }
    var restoreXp by remember { mutableStateOf(true) }
    var restoreNickname by remember { mutableStateOf(true) }
    var excludedRoleIds by remember { mutableStateOf(setOf<String>()) }
    
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var showAllRoles by remember { mutableStateOf(false) }
    
    // Load settings and roles
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load roles
                val rolesResponse = apiClient.guildService.getRoles(guildId)
                if (rolesResponse.isSuccessful && rolesResponse.body() != null) {
                    roles = rolesResponse.body()!!.sortedByDescending { it.position }
                }
                
                // Load current settings
                val settingsResponse = apiClient.configService.getRestoreSettings(guildId)
                if (settingsResponse.isSuccessful && settingsResponse.body() != null) {
                    val settings = settingsResponse.body()!!
                    restoreRoles = settings.restoreRoles
                    restoreXp = settings.restoreXp
                    restoreNickname = settings.restoreNickname
                    
                    // Parse excluded roles from JSON array string
                    try {
                        val jsonArray = JSONArray(settings.excludedRoles)
                        val excludedSet = mutableSetOf<String>()
                        for (i in 0 until jsonArray.length()) {
                            excludedSet.add(jsonArray.getString(i))
                        }
                        excludedRoleIds = excludedSet
                    } catch (e: Exception) {
                        excludedRoleIds = emptySet()
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
                title = { Text("Restore Settings") },
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
                                    Icons.Default.Restore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Automatic Data Restoration",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "When members leave your server, their roles, XP/level, and nickname are saved. " +
                                "If they rejoin, this data can be automatically restored.",
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
                                    "Settings saved successfully!",
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
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Restoration Options",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            // Restore Roles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Restore Roles",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Automatically reassign roles the member had before leaving",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = restoreRoles,
                                    onCheckedChange = { restoreRoles = it }
                                )
                            }
                            
                            Divider()
                            
                            // Restore XP & Level
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Restore XP & Level",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Restore the member's experience points and level",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = restoreXp,
                                    onCheckedChange = { restoreXp = it }
                                )
                            }
                            
                            Divider()
                            
                            // Restore Nickname
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Restore Nickname",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Restore the member's server nickname",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = restoreNickname,
                                    onCheckedChange = { restoreNickname = it }
                                )
                            }
                        }
                    }
                }
                
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Excluded Roles",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        "Select roles that should never be restored",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Row {
                                    if (excludedRoleIds.isNotEmpty()) {
                                        TextButton(
                                            onClick = { excludedRoleIds = emptySet() }
                                        ) {
                                            Text("Clear All")
                                        }
                                    }
                                    TextButton(
                                        onClick = { excludedRoleIds = roles.map { it.id }.toSet() }
                                    ) {
                                        Text("Select All")
                                    }
                                }
                            }
                            
                            Text(
                                "${excludedRoleIds.size} role(s) excluded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                // Show roles (limit to 5 unless expanded)
                val displayRoles = if (showAllRoles) roles else roles.take(5)
                items(displayRoles) { role ->
                    Card(
                        onClick = {
                            excludedRoleIds = if (excludedRoleIds.contains(role.id)) {
                                excludedRoleIds - role.id
                            } else {
                                excludedRoleIds + role.id
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                role.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = excludedRoleIds.contains(role.id),
                                onCheckedChange = null
                            )
                        }
                    }
                }
                
                if (roles.size > 5 && !showAllRoles) {
                    item {
                        TextButton(
                            onClick = { showAllRoles = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show ${roles.size - 5} more roles")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                }
                
                if (showAllRoles && roles.size > 5) {
                    item {
                        TextButton(
                            onClick = { showAllRoles = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Show less")
                            Icon(Icons.Default.ArrowDropUp, contentDescription = null)
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
                                    
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    
                                    // Convert excluded role IDs to JSON array string
                                    val excludedRolesJson = JSONArray(excludedRoleIds.toList()).toString()
                                    
                                    val settings = RestoreSettings(
                                        guildId = guildId,
                                        restoreRoles = restoreRoles,
                                        restoreXp = restoreXp,
                                        restoreNickname = restoreNickname,
                                        excludedRoles = excludedRolesJson
                                    )
                                    
                                    apiClient.configService.updateRestoreSettings(guildId, settings)
                                    showSuccess = true
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Failed to save settings"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Settings")
                    }
                }
            }
        }
    }
}
