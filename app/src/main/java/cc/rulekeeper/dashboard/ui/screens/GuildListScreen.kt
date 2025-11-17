package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.R
import coil.compose.AsyncImage
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.Guild
import cc.rulekeeper.dashboard.data.repository.AuthRepository
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuildListScreen(
    settingsRepository: SettingsRepository,
    onGuildSelected: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var guilds by remember { mutableStateOf<List<Guild>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    
    val username by settingsRepository.username.collectAsState(initial = null)
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                
                // Create API client with synchronous cached token provider
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.guildService.getGuilds()
                
                println("[DEBUG] Guild response code: ${response.code()}")
                println("[DEBUG] Guild response body: ${response.body()}")
                println("[DEBUG] Guild response raw: ${response.raw()}")
                
                if (response.isSuccessful && response.body() != null) {
                    val guildListResponse = response.body()!!
                    println("[DEBUG] Guilds count: ${guildListResponse.guilds.size}, Total: ${guildListResponse.total}")
                    guilds = guildListResponse.guilds
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = "Failed to load guilds: ${response.code()} - ${errorBody ?: response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_rulekeeper_logo),
                            contentDescription = "RuleKeeper Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text("Your Servers")
                            username?.let {
                                Text(
                                    "Logged in as $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings") },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                scope.launch {
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    val authRepository = AuthRepository(apiClient, settingsRepository)
                                    authRepository.logout()
                                    onLogout()
                                }
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
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
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                errorMessage ?: "An error occurred",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                guilds.isEmpty() -> {
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
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("No servers found")
                            Text(
                                "Make sure the bot is added to your servers",
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
                        items(guilds) { guild ->
                            GuildCard(
                                guild = guild,
                                onClick = { onGuildSelected(guild.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuildCard(
    guild: Guild,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Server icon
            if (guild.icon != null) {
                AsyncImage(
                    model = guild.icon,
                    contentDescription = "${guild.name} icon",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    Icons.Default.GroupWork,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    guild.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open server",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
