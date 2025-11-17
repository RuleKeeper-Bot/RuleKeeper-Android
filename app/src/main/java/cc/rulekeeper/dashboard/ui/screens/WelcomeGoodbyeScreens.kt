package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import cc.rulekeeper.dashboard.data.model.Channel
import cc.rulekeeper.dashboard.data.model.WelcomeConfig
import cc.rulekeeper.dashboard.data.model.GoodbyeConfig
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeConfigScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // All fields matching database schema
    var enabled by remember { mutableStateOf(false) }
    var channelId by remember { mutableStateOf<String?>(null) }
    var messageType by remember { mutableStateOf("text") }
    var messageContent by remember { mutableStateOf<String?>("Welcome {user} to {server}!") }
    var embedTitle by remember { mutableStateOf<String?>("Welcome!") }
    var embedDescription by remember { mutableStateOf<String?>("{user} has joined {server}") }
    var embedColor by remember { mutableStateOf(0x00FF00) }
    var embedThumbnail by remember { mutableStateOf(true) }
    var showServerIcon by remember { mutableStateOf(false) }
    
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var showChannelDropdown by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
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
                
                // Load current config from API
                val configResponse = apiClient.configService.getWelcomeConfig(guildId)
                if (configResponse.isSuccessful) {
                    configResponse.body()?.let { config ->
                        enabled = config.enabled
                        channelId = config.channelId
                        messageType = config.messageType
                        messageContent = config.messageContent
                        embedTitle = config.embedTitle
                        embedDescription = config.embedDescription
                        embedColor = config.embedColor
                        embedThumbnail = config.embedThumbnail
                        showServerIcon = config.showServerIcon
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
                title = { Text("Welcome Messages") },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Welcome Messages", style = MaterialTheme.typography.titleMedium)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    
                    Text(
                        "Send a message when new members join",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (enabled) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Channel", style = MaterialTheme.typography.titleMedium)
                        
                        ExposedDropdownMenuBox(
                            expanded = showChannelDropdown,
                            onExpandedChange = { showChannelDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = channels.find { it.id == channelId }?.name ?: "Select Channel",
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
                                channels.forEach { channel ->
                                    DropdownMenuItem(
                                        text = { Text("# ${channel.name}") },
                                        onClick = {
                                            channelId = channel.id
                                            showChannelDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Message Type", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = messageType == "text",
                                onClick = { messageType = "text" },
                                label = { Text("Text") }
                            )
                            FilterChip(
                                selected = messageType == "embed",
                                onClick = { messageType = "embed" },
                                label = { Text("Embed") }
                            )
                        }
                    }
                }
                
                if (messageType == "text") {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Message Content", style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = messageContent ?: "",
                                onValueChange = { messageContent = it },
                                label = { Text("Message") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                supportingText = {
                                    Text("Use {user} for mention, {server} for server name")
                                }
                            )
                        }
                    }
                } else {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Embed Settings", style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = embedTitle ?: "",
                                onValueChange = { embedTitle = it },
                                label = { Text("Embed Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = embedDescription ?: "",
                                onValueChange = { embedDescription = it },
                                label = { Text("Embed Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                supportingText = {
                                    Text("Use {user} for mention, {server} for server name")
                                }
                            )
                            
                            OutlinedTextField(
                                value = "0x" + embedColor.toString(16).uppercase().padStart(6, '0'),
                                onValueChange = { 
                                    val hex = it.removePrefix("0x").removePrefix("#")
                                    embedColor = hex.toIntOrNull(16) ?: embedColor
                                },
                                label = { Text("Embed Color (Hex)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("0x00FF00") }
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Embed Thumbnail")
                                Switch(checked = embedThumbnail, onCheckedChange = { embedThumbnail = it })
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Server Icon")
                                Switch(checked = showServerIcon, onCheckedChange = { showServerIcon = it })
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
                            
                            val config = WelcomeConfig(
                                enabled = enabled,
                                channelId = channelId,
                                messageType = messageType,
                                messageContent = messageContent,
                                embedTitle = embedTitle,
                                embedDescription = embedDescription,
                                embedColor = embedColor,
                                embedThumbnail = embedThumbnail,
                                showServerIcon = showServerIcon
                            )
                            
                            apiClient.configService.updateWelcomeConfig(guildId, config)
                            showSuccess = true
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to save configuration"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && (!enabled || channelId != null)
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
fun GoodbyeConfigScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // All fields matching database schema
    var enabled by remember { mutableStateOf(false) }
    var channelId by remember { mutableStateOf<String?>(null) }
    var messageType by remember { mutableStateOf("text") }
    var messageContent by remember { mutableStateOf<String?>("Goodbye {user}! We'll miss you.") }
    var embedTitle by remember { mutableStateOf<String?>("Goodbye!") }
    var embedDescription by remember { mutableStateOf<String?>("{user} has left the server.") }
    var embedColor by remember { mutableStateOf(0xFF0000) }
    var embedThumbnail by remember { mutableStateOf(true) }
    var showServerIcon by remember { mutableStateOf(false) }
    
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var showChannelDropdown by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
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
                
                // Load current config from API
                val configResponse = apiClient.configService.getGoodbyeConfig(guildId)
                if (configResponse.isSuccessful) {
                    configResponse.body()?.let { config ->
                        enabled = config.enabled
                        channelId = config.channelId
                        messageType = config.messageType
                        messageContent = config.messageContent
                        embedTitle = config.embedTitle
                        embedDescription = config.embedDescription
                        embedColor = config.embedColor
                        embedThumbnail = config.embedThumbnail
                        showServerIcon = config.showServerIcon
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
                title = { Text("Goodbye Messages") },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Goodbye Messages", style = MaterialTheme.typography.titleMedium)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    
                    Text(
                        "Send a message when members leave",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (enabled) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Channel", style = MaterialTheme.typography.titleMedium)
                        
                        ExposedDropdownMenuBox(
                            expanded = showChannelDropdown,
                            onExpandedChange = { showChannelDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = channels.find { it.id == channelId }?.name ?: "Select Channel",
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
                                channels.forEach { channel ->
                                    DropdownMenuItem(
                                        text = { Text("# ${channel.name}") },
                                        onClick = {
                                            channelId = channel.id
                                            showChannelDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Message Type", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = messageType == "text",
                                onClick = { messageType = "text" },
                                label = { Text("Text") }
                            )
                            FilterChip(
                                selected = messageType == "embed",
                                onClick = { messageType = "embed" },
                                label = { Text("Embed") }
                            )
                        }
                    }
                }
                
                if (messageType == "text") {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Message Content", style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = messageContent ?: "",
                                onValueChange = { messageContent = it },
                                label = { Text("Message") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                supportingText = {
                                    Text("Use {user} for mention, {server} for server name")
                                }
                            )
                        }
                    }
                } else {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Embed Settings", style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = embedTitle ?: "",
                                onValueChange = { embedTitle = it },
                                label = { Text("Embed Title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            OutlinedTextField(
                                value = embedDescription ?: "",
                                onValueChange = { embedDescription = it },
                                label = { Text("Embed Description") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                supportingText = {
                                    Text("Use {user} for mention, {server} for server name")
                                }
                            )
                            
                            OutlinedTextField(
                                value = "0x" + embedColor.toString(16).uppercase().padStart(6, '0'),
                                onValueChange = { 
                                    val hex = it.removePrefix("0x").removePrefix("#")
                                    embedColor = hex.toIntOrNull(16) ?: embedColor
                                },
                                label = { Text("Embed Color (Hex)") },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("0xFF0000") }
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Embed Thumbnail")
                                Switch(checked = embedThumbnail, onCheckedChange = { embedThumbnail = it })
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Show Server Icon")
                                Switch(checked = showServerIcon, onCheckedChange = { showServerIcon = it })
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
                            
                            val config = GoodbyeConfig(
                                enabled = enabled,
                                channelId = channelId,
                                messageType = messageType,
                                messageContent = messageContent,
                                embedTitle = embedTitle,
                                embedDescription = embedDescription,
                                embedColor = embedColor,
                                embedThumbnail = embedThumbnail,
                                showServerIcon = showServerIcon
                            )
                            
                            apiClient.configService.updateGoodbyeConfig(guildId, config)
                            showSuccess = true
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Failed to save configuration"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && (!enabled || channelId != null)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Configuration")
            }
        }
    }
}
