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
import cc.rulekeeper.dashboard.data.model.SpamConfig
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpamConfigScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    // All fields matching database schema
    var enabled by remember { mutableStateOf(true) }
    var spamThreshold by remember { mutableStateOf(5) }
    var spamTimeWindow by remember { mutableStateOf(10) }
    var mentionThreshold by remember { mutableStateOf(3) }
    var mentionTimeWindow by remember { mutableStateOf(30) }
    var excludedChannels by remember { mutableStateOf("[]") }
    var excludedRoles by remember { mutableStateOf("[]") }
    var spamStrikesBeforeWarning by remember { mutableStateOf(1) }
    var noXpDuration by remember { mutableStateOf(60) }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                errorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load current config from API
                val configResponse = apiClient.configService.getSpamConfig(guildId)
                if (configResponse.isSuccessful) {
                    configResponse.body()?.let { config ->
                        enabled = config.enabled
                        spamThreshold = config.spamThreshold
                        spamTimeWindow = config.spamTimeWindow
                        mentionThreshold = config.mentionThreshold
                        mentionTimeWindow = config.mentionTimeWindow
                        excludedChannels = config.excludedChannels
                        excludedRoles = config.excludedRoles
                        spamStrikesBeforeWarning = config.spamStrikesBeforeWarning
                        noXpDuration = config.noXpDuration
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
                title = { Text("Spam Protection") },
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
                        Text("Enable Spam Protection", style = MaterialTheme.typography.titleMedium)
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    
                    Text(
                        "Automatically detect and prevent spam messages",
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
                        Text("Message Spam Detection", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = spamThreshold.toString(),
                            onValueChange = { spamThreshold = it.toIntOrNull() ?: spamThreshold },
                            label = { Text("Spam Threshold") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Max messages allowed") }
                        )
                        
                        OutlinedTextField(
                            value = spamTimeWindow.toString(),
                            onValueChange = { spamTimeWindow = it.toIntOrNull() ?: spamTimeWindow },
                            label = { Text("Time Window (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Within this time period") }
                        )
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Mention Spam Detection", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = mentionThreshold.toString(),
                            onValueChange = { mentionThreshold = it.toIntOrNull() ?: mentionThreshold },
                            label = { Text("Mention Threshold") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Max mentions allowed") }
                        )
                        
                        OutlinedTextField(
                            value = mentionTimeWindow.toString(),
                            onValueChange = { mentionTimeWindow = it.toIntOrNull() ?: mentionTimeWindow },
                            label = { Text("Mention Time Window (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Within this time period") }
                        )
                    }
                }
                
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Punishment Settings", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = spamStrikesBeforeWarning.toString(),
                            onValueChange = { spamStrikesBeforeWarning = it.toIntOrNull() ?: spamStrikesBeforeWarning },
                            label = { Text("Strikes Before Warning") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Number of spam strikes before issuing a warning") }
                        )
                        
                        OutlinedTextField(
                            value = noXpDuration.toString(),
                            onValueChange = { noXpDuration = it.toIntOrNull() ?: noXpDuration },
                            label = { Text("No XP Duration (seconds)") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("How long user won't gain XP after spam detection") }
                        )
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
                            
                            val config = SpamConfig(
                                enabled = enabled,
                                spamThreshold = spamThreshold,
                                spamTimeWindow = spamTimeWindow,
                                mentionThreshold = mentionThreshold,
                                mentionTimeWindow = mentionTimeWindow,
                                excludedChannels = excludedChannels,
                                excludedRoles = excludedRoles,
                                spamStrikesBeforeWarning = spamStrikesBeforeWarning,
                                noXpDuration = noXpDuration
                            )
                            
                            apiClient.configService.updateSpamConfig(guildId, config)
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
