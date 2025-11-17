package cc.rulekeeper.dashboard.ui.screens.users

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ErrorCard
import cc.rulekeeper.dashboard.ui.components.LoadingCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class GuildUser(
    val id: String,
    val username: String,
    val discriminator: String?,
    val avatar: String?,
    val xp: Int = 0,
    val level: Int = 0,
    val joinedAt: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var users by remember { mutableStateOf<List<GuildUser>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<GuildUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("username") } // username, xp, level, joined
    
    // Load users
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                isLoading = true
                errorMessage = null
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.usersService.getUsers(guildId)
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!
                    val usersList = (responseBody["users"] as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
                    users = usersList.mapNotNull { map ->
                        try {
                            GuildUser(
                                id = map["user_id"]?.toString() ?: "",
                                username = map["username"]?.toString() ?: "Unknown",
                                discriminator = null,
                                avatar = null,
                                xp = (map["xp"] as? Number)?.toInt() ?: 0,
                                level = (map["level"] as? Number)?.toInt() ?: 0,
                                joinedAt = (map["last_message"] as? Number)?.toLong()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    filteredUsers = users
                } else {
                    errorMessage = "Failed to load users"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Filter and sort users
    LaunchedEffect(searchQuery, sortBy, users) {
        var result = users
        
        // Filter by search
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Sort
        result = when (sortBy) {
            "xp" -> result.sortedByDescending { it.xp }
            "level" -> result.sortedByDescending { it.level }
            "joined" -> result.sortedByDescending { it.joinedAt ?: 0 }
            else -> result.sortedBy { it.username }
        }
        
        filteredUsers = result
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guild Users") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info card
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
                            Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "User Management",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "${users.size} total users",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // Search and sort
            item {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search users") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Sort by:",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            FilterChip(
                                selected = sortBy == "username",
                                onClick = { sortBy = "username" },
                                label = { Text("Name") }
                            )
                            FilterChip(
                                selected = sortBy == "xp",
                                onClick = { sortBy = "xp" },
                                label = { Text("XP") }
                            )
                            FilterChip(
                                selected = sortBy == "level",
                                onClick = { sortBy = "level" },
                                label = { Text("Level") }
                            )
                        }
                    }
                }
            }
            
            // Loading state
            if (isLoading) {
                item {
                    LoadingCard(message = "Loading users...")
                }
            }
            
            // Error state
            if (errorMessage != null) {
                item {
                    ErrorCard(
                        message = errorMessage ?: "Unknown error",
                        onRetry = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                // Retry logic here
                            }
                        }
                    )
                }
            }
            
            // Users list
            if (!isLoading && errorMessage == null) {
                if (filteredUsers.isEmpty()) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No users found",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                } else {
                    items(filteredUsers) { user ->
                        UserListItem(user = user, onClick = { onUserClick(user.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItem(
    user: GuildUser,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Level ${user.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${user.xp} XP",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
