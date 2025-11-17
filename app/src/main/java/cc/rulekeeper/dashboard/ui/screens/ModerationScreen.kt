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
import cc.rulekeeper.dashboard.data.model.Warning
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var warnings by remember { mutableStateOf<List<Warning>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val loadWarnings: suspend () -> Unit = {
        try {
            isLoading = true
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            val response = apiClient.moderationService.getWarnings(guildId)
            if (response.isSuccessful && response.body() != null) {
                warnings = response.body()!!
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load moderation data"
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        scope.launch {
            loadWarnings()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Warned Users") },
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
        WarningsContent(
            warnings = warnings,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onDeleteWarning = { warning ->
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.moderationService.deleteWarning(
                            guildId,
                            warning.id
                        )
                        if (response.isSuccessful) {
                            loadWarnings()
                        } else {
                            errorMessage = "Failed to delete warning"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Failed to delete warning"
                    }
                }
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun WarningsContent(
    warnings: List<Warning>,
    isLoading: Boolean,
    errorMessage: String?,
    onDeleteWarning: (Warning) -> Unit,
    modifier: Modifier = Modifier
) {
    var warningToDelete by remember { mutableStateOf<Warning?>(null) }
    
    // Confirmation dialog for warning deletion
    if (warningToDelete != null) {
        ConfirmationDialog(
            title = "Delete Warning",
            message = "Are you sure you want to delete this warning for user ${warningToDelete!!.userId}?",
            onConfirm = {
                onDeleteWarning(warningToDelete!!)
                warningToDelete = null
            },
            onDismiss = { warningToDelete = null },
            isDestructive = true
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            warnings.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Warnings",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No members have been warned yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(warnings) { warning ->
                        WarningCard(
                            warning = warning,
                            onDelete = { warningToDelete = warning }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BansTab(
    guildId: String,
    settingsRepository: SettingsRepository
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Block,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Banned Users",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "View and manage banned users. This feature will display the server's ban list.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BansScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var bans by remember { mutableStateOf<List<cc.rulekeeper.dashboard.data.model.Ban>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.moderationService.getBans(guildId)
                if (response.isSuccessful && response.body() != null) {
                    bans = response.body()!!.bans
                } else {
                    errorMessage = "Failed to load bans"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load bans"
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banned Users") },
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
        BansContent(
            bans = bans,
            isLoading = isLoading,
            errorMessage = errorMessage,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun BansContent(
    bans: List<cc.rulekeeper.dashboard.data.model.Ban>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            bans.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No Bans",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No members have been banned.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bans) { ban ->
                        BanCard(ban = ban)
                    }
                }
            }
        }
    }
}

@Composable
fun BanCard(ban: cc.rulekeeper.dashboard.data.model.Ban) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            ban.username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            "User ID: ${ban.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (ban.reason != null) {
                Divider()
                Text(
                    ban.reason,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
