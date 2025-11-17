package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.AddLevelRewardRequest
import cc.rulekeeper.dashboard.data.model.Channel
import cc.rulekeeper.dashboard.data.model.LevelConfig
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelingScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // All fields matching database schema
    var cooldown by remember { mutableStateOf(60) }
    var xpMin by remember { mutableStateOf(15) }
    var xpMax by remember { mutableStateOf(25) }
    var levelChannel by remember { mutableStateOf<String?>(null) }
    var announceLevelUp by remember { mutableStateOf(true) }
    var excludedChannels by remember { mutableStateOf("[]") }
    var xpBoostRoles by remember { mutableStateOf("{}") }
    var embedTitle by remember { mutableStateOf("🎉 Level Up!") }
    var embedDescription by remember { mutableStateOf("{user} has reached level **{level}**!") }
    var embedColor by remember { mutableStateOf(16766720) }
    var giveXpToBots by remember { mutableStateOf(false) }
    var giveXpToSelf by remember { mutableStateOf(false) }
    var cooldownBypassUsers by remember { mutableStateOf("[]") }
    var cooldownBypassRoles by remember { mutableStateOf("[]") }
    
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var roles by remember { mutableStateOf<List<cc.rulekeeper.dashboard.data.model.Role>>(emptyList()) }
    var levelRewards by remember { mutableStateOf<Map<Int, String>>(emptyMap()) } // level -> roleId
    var showChannelDropdown by remember { mutableStateOf(false) }
    var showAddRewardDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                errorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load channels
                val channelsResponse = apiClient.guildService.getChannels(guildId)
                if (channelsResponse.isSuccessful) {
                    channels = channelsResponse.body()?.filter { it.type == 0 } ?: emptyList()
                }
                
                // Load roles
                val rolesResponse = apiClient.guildService.getRoles(guildId)
                if (rolesResponse.isSuccessful) {
                    roles = rolesResponse.body() ?: emptyList()
                }
                
                // Load current config from API
                val configResponse = apiClient.configService.getLevelConfig(guildId)
                if (configResponse.isSuccessful) {
                    configResponse.body()?.let { config ->
                        cooldown = config.cooldown
                        xpMin = config.xpMin
                        xpMax = config.xpMax
                        levelChannel = config.levelChannel
                        announceLevelUp = config.announceLevelUp
                        excludedChannels = config.excludedChannels
                        xpBoostRoles = config.xpBoostRoles
                        embedTitle = config.embedTitle
                        embedDescription = config.embedDescription
                        embedColor = config.embedColor
                        giveXpToBots = config.giveXpToBots
                        giveXpToSelf = config.giveXpToSelf
                        cooldownBypassUsers = config.cooldownBypassUsers
                        cooldownBypassRoles = config.cooldownBypassRoles
                    }
                }
                
                // Load level rewards
                val rewardsResponse = apiClient.configService.getLevelRewards(guildId)
                if (rewardsResponse.isSuccessful) {
                    val rewardsMap = rewardsResponse.body()?.rewards ?: emptyMap()
                    // Convert string keys to int
                    levelRewards = rewardsMap.mapKeys { it.key.toIntOrNull() ?: 0 }.filterKeys { it > 0 }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load configuration"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Add reward dialog
    if (showAddRewardDialog) {
        AddLevelRewardDialog(
            roles = roles,
            existingLevels = levelRewards.keys.toList(),
            onDismiss = { showAddRewardDialog = false },
            onAdd = { level, roleId ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.configService.addLevelReward(
                            guildId,
                            AddLevelRewardRequest(level, roleId)
                        )
                        if (response.isSuccessful) {
                            levelRewards = levelRewards + (level to roleId)
                            showAddRewardDialog = false
                        } else {
                            errorMessage = "Failed to add reward"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leveling System") },
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
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Text(
                            "Configuration saved successfully!",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            errorMessage ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("XP Configuration", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = xpMin.toString(),
                            onValueChange = { xpMin = it.toIntOrNull() ?: xpMin },
                            label = { Text("Minimum XP per Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = xpMax.toString(),
                            onValueChange = { xpMax = it.toIntOrNull() ?: xpMax },
                            label = { Text("Maximum XP per Message") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = cooldown.toString(),
                            onValueChange = { cooldown = it.toIntOrNull() ?: cooldown },
                            label = { Text("Cooldown (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Time between XP gains") }
                        )
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Level-Up Announcement", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Announce Level-Ups")
                            Switch(checked = announceLevelUp, onCheckedChange = { announceLevelUp = it })
                        }
                        
                        if (announceLevelUp) {
                            Text("Channel", style = MaterialTheme.typography.labelMedium)
                            
                            ExposedDropdownMenuBox(
                                expanded = showChannelDropdown,
                                onExpandedChange = { showChannelDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = channels.find { it.id == levelChannel }?.name ?: "Same channel as message",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showChannelDropdown) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = showChannelDropdown,
                                    onDismissRequest = { showChannelDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Same channel as message") },
                                        onClick = {
                                            levelChannel = null
                                            showChannelDropdown = false
                                        }
                                    )
                                    channels.forEach { channel ->
                                        DropdownMenuItem(
                                            text = { Text("# ${channel.name}") },
                                            onClick = {
                                                levelChannel = channel.id
                                                showChannelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = embedTitle,
                                onValueChange = { embedTitle = it },
                                label = { Text("Embed Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = embedDescription,
                                onValueChange = { embedDescription = it },
                                label = { Text("Embed Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                supportingText = {
                                    Text("Use {user} for mention, {level} for level number")
                                }
                            )
                            
                            OutlinedTextField(
                                value = "0x" + embedColor.toString(16).uppercase().padStart(6, '0'),
                                onValueChange = { 
                                    val hex = it.removePrefix("0x").removePrefix("#")
                                    embedColor = hex.toIntOrNull(16) ?: embedColor
                                },
                                label = { Text("Embed Color (Hex)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("XP Settings", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Give XP to Bots")
                            Switch(checked = giveXpToBots, onCheckedChange = { giveXpToBots = it })
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Give XP to Self (Bot)")
                            Switch(checked = giveXpToSelf, onCheckedChange = { giveXpToSelf = it })
                        }
                    }
                }
                
                // Level Rewards Section
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
                            Text("Level Rewards", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { showAddRewardDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Reward")
                            }
                        }
                        
                        if (levelRewards.isEmpty()) {
                            Text(
                                "No level rewards configured",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            levelRewards.toSortedMap().forEach { (level, roleId) ->
                                val role = roles.find { it.id == roleId }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "Level $level",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                                Text(
                                                    role?.name ?: "Unknown Role",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    try {
                                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                            settingsRepository.getCachedAccessToken()
                                                        }
                                                        val response = apiClient.configService.deleteLevelReward(guildId, level)
                                                        if (response.isSuccessful) {
                                                            levelRewards = levelRewards - level
                                                        } else {
                                                            errorMessage = "Failed to delete reward"
                                                        }
                                                    } catch (e: Exception) {
                                                        errorMessage = e.message
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Reward")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                showSuccess = false
                                errorMessage = null
                                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                    settingsRepository.getCachedAccessToken()
                                }
                                
                                val config = LevelConfig(
                                    cooldown = cooldown,
                                    xpMin = xpMin,
                                    xpMax = xpMax,
                                    levelChannel = levelChannel,
                                    announceLevelUp = announceLevelUp,
                                    excludedChannels = excludedChannels,
                                    xpBoostRoles = xpBoostRoles,
                                    embedTitle = embedTitle,
                                    embedDescription = embedDescription,
                                    embedColor = embedColor,
                                    giveXpToBots = giveXpToBots,
                                    giveXpToSelf = giveXpToSelf,
                                    cooldownBypassUsers = cooldownBypassUsers,
                                    cooldownBypassRoles = cooldownBypassRoles
                                )
                                
                                apiClient.configService.updateLevelConfig(guildId, config)
                                showSuccess = true
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Failed to save configuration"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Configuration")
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLevelRewardDialog(
    roles: List<cc.rulekeeper.dashboard.data.model.Role>,
    existingLevels: List<Int>,
    onDismiss: () -> Unit,
    onAdd: (level: Int, roleId: String) -> Unit
) {
    var level by remember { mutableStateOf("") }
    var selectedRoleId by remember { mutableStateOf(roles.firstOrNull()?.id ?: "") }
    var showError by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Level Reward") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = level,
                    onValueChange = { 
                        if (it.all { c -> c.isDigit() }) {
                            level = it
                            showError = false
                        }
                    },
                    label = { Text("Level") },
                    placeholder = { Text("e.g. 10") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text(errorText, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                Text("Role Reward", style = MaterialTheme.typography.labelMedium)
                var roleExpanded by remember { mutableStateOf(false) }
                val selectedRole = roles.find { it.id == selectedRoleId }
                
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole?.name ?: "Select Role",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRoleId = role.id
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val levelInt = level.toIntOrNull()
                    when {
                        levelInt == null || levelInt <= 0 -> {
                            showError = true
                            errorText = "Please enter a valid level"
                        }
                        levelInt in existingLevels -> {
                            showError = true
                            errorText = "A reward already exists for level $levelInt"
                        }
                        selectedRoleId.isEmpty() -> {
                            showError = true
                            errorText = "Please select a role"
                        }
                        else -> {
                            onAdd(levelInt, selectedRoleId)
                        }
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
