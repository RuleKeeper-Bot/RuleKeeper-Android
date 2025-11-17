package cc.rulekeeper.dashboard.ui.screens

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
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.*
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleMenusScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    var menus by remember { mutableStateOf<List<RoleMenu>>(emptyList()) }
    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<RoleMenu?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load data
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                isLoading = true
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load role menus
                val menusResponse = apiClient.roleMenuService.getRoleMenus(guildId)
                if (menusResponse.isSuccessful) {
                    menus = menusResponse.body() ?: emptyList()
                }
                
                // Load channels
                val channelsResponse = apiClient.guildService.getChannels(guildId)
                if (channelsResponse.isSuccessful) {
                    // Filter out categories (type 4) - only show text channels (type 0)
                    channels = channelsResponse.body()?.filter { it.type == 0 } ?: emptyList()
                }
                
                // Load roles
                val rolesResponse = apiClient.guildService.getRoles(guildId)
                if (rolesResponse.isSuccessful) {
                    roles = rolesResponse.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    // Create dialog
    if (showCreateDialog) {
        CreateRoleMenuDialog(
            guildId = guildId,
            channels = channels,
            roles = roles,
            settingsRepository = settingsRepository,
            onDismiss = { showCreateDialog = false },
            onCreated = {
                showCreateDialog = false
                // Reload menus
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.roleMenuService.getRoleMenus(guildId)
                        if (response.isSuccessful) {
                            menus = response.body() ?: emptyList()
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            }
        )
    }
    
    // Edit dialog
    showEditDialog?.let { menu ->
        EditRoleMenuDialog(
            guildId = guildId,
            menu = menu,
            channels = channels,
            roles = roles,
            settingsRepository = settingsRepository,
            onDismiss = { showEditDialog = null },
            onUpdated = {
                showEditDialog = null
                // Reload menus
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.roleMenuService.getRoleMenus(guildId)
                        if (response.isSuccessful) {
                            menus = response.body() ?: emptyList()
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
            },
            onDeleted = {
                showEditDialog = null
                // Reload menus
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.roleMenuService.getRoleMenus(guildId)
                        if (response.isSuccessful) {
                            menus = response.body() ?: emptyList()
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
                title = { Text("Role Menus") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Menu")
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
                                Icons.Default.Menu,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Role Menus",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create interactive menus that allow users to self-assign roles by clicking buttons or using dropdown menus.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            "Error: $errorMessage",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (menus.isEmpty()) {
                item {
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
                                "No Role Menus Created",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Create your first role menu to let members self-assign roles.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Role Menu")
                            }
                        }
                    }
                }
            } else {
                items(menus) { menu ->
                    RoleMenuCard(
                        menu = menu,
                        channels = channels,
                        onClick = { showEditDialog = menu }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleMenuCard(
    menu: RoleMenu,
    channels: List<Channel>,
    onClick: () -> Unit
) {
    val channelName = channels.find { it.id == menu.channelId }?.name ?: "Unknown Channel"
    val menuTypeLabel = when (menu.type) {
        "dropdown" -> "Dropdown Menu"
        "button" -> "Button Menu"
        "reactionrole" -> "Reaction Roles"
        else -> menu.type
    }
    val roleCount = menu.config?.roles?.size ?: 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        menu.config?.title ?: "Untitled Menu",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        menuTypeLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!menu.config?.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    menu.config?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "#$channelName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    "$roleCount role${if (roleCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoleMenuRoleDialog(
    availableRoles: List<Role>,
    onDismiss: () -> Unit,
    onRoleAdded: (RoleMenuRole) -> Unit
) {
    var selectedRoleId by remember { mutableStateOf(availableRoles.firstOrNull()?.id ?: "") }
    var label by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    
    val selectedRole = availableRoles.find { it.id == selectedRoleId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Role") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Role selection
                Text("Discord Role", style = MaterialTheme.typography.labelMedium)
                var roleExpanded by remember { mutableStateOf(false) }
                
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
                        availableRoles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRoleId = role.id
                                    // Auto-fill label with role name if empty
                                    if (label.isBlank()) {
                                        label = role.name
                                    }
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Role display name") }
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Role description") },
                    minLines = 2
                )
                
                // Emoji
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("🎮") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRoleAdded(
                        RoleMenuRole(
                            roleId = selectedRoleId,
                            label = label.ifBlank { selectedRole?.name ?: "Role" },
                            description = description.ifBlank { null },
                            emoji = emoji.ifBlank { null }
                        )
                    )
                },
                enabled = selectedRoleId.isNotBlank() && (label.isNotBlank() || selectedRole != null)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoleMenuDialog(
    guildId: String,
    channels: List<Channel>,
    roles: List<Role>,
    settingsRepository: SettingsRepository,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var menuType by remember { mutableStateOf("dropdown") }
    var selectedChannelId by remember { mutableStateOf(channels.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var placeholder by remember { mutableStateOf("") }
    var minValues by remember { mutableStateOf("1") }
    var maxValues by remember { mutableStateOf("1") }
    var selectedRoles by remember { mutableStateOf<List<RoleMenuRole>>(emptyList()) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Role configuration dialog
    if (showRoleDialog) {
        AddRoleMenuRoleDialog(
            availableRoles = roles.filter { role -> 
                selectedRoles.none { it.roleId == role.id }
            },
            onDismiss = { showRoleDialog = false },
            onRoleAdded = { roleMenuRole ->
                selectedRoles = selectedRoles + roleMenuRole
                showRoleDialog = false
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Role Menu") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Menu Title") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Select your roles") }
                    )
                }
                
                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Choose one or more roles") },
                        minLines = 2
                    )
                }
                
                // Menu Type
                item {
                    Text("Menu Type", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { menuType = "dropdown" }
                        ) {
                            RadioButton(
                                selected = menuType == "dropdown",
                                onClick = { menuType = "dropdown" }
                            )
                            Text("Dropdown Menu", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { menuType = "button" }
                        ) {
                            RadioButton(
                                selected = menuType == "button",
                                onClick = { menuType = "button" }
                            )
                            Text("Button Menu", modifier = Modifier.padding(start = 8.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { menuType = "reactionrole" }
                        ) {
                            RadioButton(
                                selected = menuType == "reactionrole",
                                onClick = { menuType = "reactionrole" }
                            )
                            Text("Reaction Roles", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                
                // Channel Selection
                item {
                    Text("Channel", style = MaterialTheme.typography.labelMedium)
                    var channelExpanded by remember { mutableStateOf(false) }
                    val selectedChannel = channels.find { it.id == selectedChannelId }
                    
                    ExposedDropdownMenuBox(
                        expanded = channelExpanded,
                        onExpandedChange = { channelExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedChannel?.name ?: "Select Channel",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = channelExpanded,
                            onDismissRequest = { channelExpanded = false }
                        ) {
                            channels.forEach { channel ->
                                DropdownMenuItem(
                                    text = { Text("#${channel.name}") },
                                    onClick = {
                                        selectedChannelId = channel.id
                                        channelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Placeholder (for dropdown menus)
                if (menuType == "dropdown") {
                    item {
                        OutlinedTextField(
                            value = placeholder,
                            onValueChange = { placeholder = it },
                            label = { Text("Placeholder (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Select roles...") }
                        )
                    }
                }
                
                // Min/Max values (for dropdown and button menus)
                if (menuType in listOf("dropdown", "button")) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = minValues,
                                onValueChange = { if (it.all { c -> c.isDigit() }) minValues = it },
                                label = { Text("Min Selections") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("1") }
                            )
                            OutlinedTextField(
                                value = maxValues,
                                onValueChange = { if (it.all { c -> c.isDigit() }) maxValues = it },
                                label = { Text("Max Selections") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("1") }
                            )
                        }
                    }
                }
                
                // Roles section
                item {
                    Divider()
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Roles (${selectedRoles.size})",
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(onClick = { showRoleDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Role")
                        }
                    }
                }
                
                items(selectedRoles.size) { index ->
                    val menuRole = selectedRoles[index]
                    val role = roles.find { it.id == menuRole.roleId }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    menuRole.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (!menuRole.description.isNullOrBlank()) {
                                    Text(
                                        menuRole.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    role?.name ?: "Unknown Role",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                selectedRoles = selectedRoles.filterIndexed { i, _ -> i != index }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
                
                if (errorMessage != null) {
                    item {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isCreating = true
                            errorMessage = null
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            val menuCreate = RoleMenuCreate(
                                guildId = guildId,
                                type = menuType,
                                channelId = selectedChannelId,
                                config = RoleMenuConfig(
                                    title = title.ifBlank { "Select your roles" },
                                    description = description.ifBlank { null },
                                    roles = selectedRoles.ifEmpty { null },
                                    placeholder = placeholder.ifBlank { null },
                                    minValues = minValues.toIntOrNull() ?: 1,
                                    maxValues = maxValues.toIntOrNull() ?: 1
                                )
                            )
                            val response = apiClient.roleMenuService.createRoleMenu(guildId, menuCreate)
                            if (response.isSuccessful) {
                                onCreated()
                            } else {
                                errorMessage = "Failed to create menu: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        } finally {
                            isCreating = false
                        }
                    }
                },
                enabled = !isCreating && selectedChannelId.isNotBlank()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoleMenuDialog(
    guildId: String,
    menu: RoleMenu,
    channels: List<Channel>,
    roles: List<Role>,
    settingsRepository: SettingsRepository,
    onDismiss: () -> Unit,
    onUpdated: () -> Unit,
    onDeleted: () -> Unit
) {
    var title by remember { mutableStateOf(menu.config?.title ?: "") }
    var description by remember { mutableStateOf(menu.config?.description ?: "") }
    var placeholder by remember { mutableStateOf(menu.config?.placeholder ?: "") }
    var minValues by remember { mutableStateOf((menu.config?.minValues ?: 1).toString()) }
    var maxValues by remember { mutableStateOf((menu.config?.maxValues ?: 1).toString()) }
    var selectedChannelId by remember { mutableStateOf(menu.channelId) }
    var menuRoles by remember { mutableStateOf(menu.config?.roles ?: emptyList()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var editingRole by remember { mutableStateOf<Pair<Int, RoleMenuRole>?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        ConfirmationDialog(
            title = "Delete Role Menu",
            message = "Are you sure you want to delete this role menu? This action cannot be undone.",
            onConfirm = {
                scope.launch {
                    try {
                        isDeleting = true
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.roleMenuService.deleteRoleMenu(guildId, menu.id)
                        if (response.isSuccessful) {
                            onDeleted()
                        } else {
                            errorMessage = "Failed to delete menu"
                            showDeleteConfirm = false
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                        showDeleteConfirm = false
                    } finally {
                        isDeleting = false
                    }
                }
            },
            onDismiss = { showDeleteConfirm = false },
            isDestructive = true
        )
    }
    
    // Add/Edit role dialog
    if (showRoleDialog) {
        val availableRoles = if (editingRole != null) {
            roles
        } else {
            roles.filter { role -> menuRoles.none { it.roleId == role.id } }
        }
        
        EditRoleMenuRoleDialog(
            availableRoles = availableRoles,
            existingRole = editingRole?.second,
            onDismiss = {
                showRoleDialog = false
                editingRole = null
            },
            onRoleSaved = { roleMenuRole ->
                if (editingRole != null) {
                    menuRoles = menuRoles.mapIndexed { index, role ->
                        if (index == editingRole!!.first) roleMenuRole else role
                    }
                } else {
                    menuRoles = menuRoles + roleMenuRole
                }
                showRoleDialog = false
                editingRole = null
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Role Menu") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 500.dp)
            ) {
                // Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Menu Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Description
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                
                // Menu Type (read-only)
                item {
                    Text("Menu Type: ${menu.type.replaceFirstChar { it.uppercase() }}", 
                        style = MaterialTheme.typography.bodyMedium)
                }
                
                // Channel
                item {
                    Text("Channel", style = MaterialTheme.typography.labelMedium)
                    var channelExpanded by remember { mutableStateOf(false) }
                    val selectedChannel = channels.find { it.id == selectedChannelId }
                    
                    ExposedDropdownMenuBox(
                        expanded = channelExpanded,
                        onExpandedChange = { channelExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedChannel?.name ?: "Select Channel",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelExpanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = channelExpanded,
                            onDismissRequest = { channelExpanded = false }
                        ) {
                            channels.forEach { channel ->
                                DropdownMenuItem(
                                    text = { Text("#${channel.name}") },
                                    onClick = {
                                        selectedChannelId = channel.id
                                        channelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Placeholder (for dropdown menus)
                if (menu.type == "dropdown") {
                    item {
                        OutlinedTextField(
                            value = placeholder,
                            onValueChange = { placeholder = it },
                            label = { Text("Placeholder (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Select roles...") }
                        )
                    }
                }
                
                // Min/Max values (for dropdown and button menus)
                if (menu.type in listOf("dropdown", "button")) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = minValues,
                                onValueChange = { if (it.all { c -> c.isDigit() }) minValues = it },
                                label = { Text("Min Selections") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("1") }
                            )
                            OutlinedTextField(
                                value = maxValues,
                                onValueChange = { if (it.all { c -> c.isDigit() }) maxValues = it },
                                label = { Text("Max Selections") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("1") }
                            )
                        }
                    }
                }
                
                // Roles section
                item {
                    Divider()
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Roles (${menuRoles.size})",
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(onClick = { showRoleDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Role")
                        }
                    }
                }
                
                items(menuRoles.size) { index ->
                    val menuRole = menuRoles[index]
                    val role = roles.find { it.id == menuRole.roleId }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.clickable {
                            editingRole = Pair(index, menuRole)
                            showRoleDialog = true
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!menuRole.emoji.isNullOrBlank()) {
                                Text(
                                    menuRole.emoji,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    menuRole.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (!menuRole.description.isNullOrBlank()) {
                                    Text(
                                        menuRole.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    role?.name ?: "Unknown Role",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = {
                                menuRoles = menuRoles.filterIndexed { i, _ -> i != index }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
                
                if (errorMessage != null) {
                    item {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                isSaving = true
                                errorMessage = null
                                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                    settingsRepository.getCachedAccessToken()
                                }
                                val update = RoleMenuUpdate(
                                    channelId = selectedChannelId,
                                    config = RoleMenuConfig(
                                        title = title.ifBlank { "Select your roles" },
                                        description = description.ifBlank { null },
                                        roles = menuRoles.ifEmpty { null },
                                        placeholder = placeholder.ifBlank { null },
                                        minValues = minValues.toIntOrNull() ?: 1,
                                        maxValues = maxValues.toIntOrNull() ?: 1
                                    )
                                )
                                val response = apiClient.roleMenuService.updateRoleMenu(guildId, menu.id, update)
                                if (response.isSuccessful) {
                                    onUpdated()
                                } else {
                                    errorMessage = "Failed to update menu: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                errorMessage = e.message
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoleMenuRoleDialog(
    availableRoles: List<Role>,
    existingRole: RoleMenuRole? = null,
    onDismiss: () -> Unit,
    onRoleSaved: (RoleMenuRole) -> Unit
) {
    var selectedRoleId by remember { mutableStateOf(existingRole?.roleId ?: availableRoles.firstOrNull()?.id ?: "") }
    var label by remember { mutableStateOf(existingRole?.label ?: "") }
    var description by remember { mutableStateOf(existingRole?.description ?: "") }
    var emoji by remember { mutableStateOf(existingRole?.emoji ?: "") }
    
    val selectedRole = availableRoles.find { it.id == selectedRoleId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingRole != null) "Edit Role" else "Add Role") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Role selection
                Text("Discord Role", style = MaterialTheme.typography.labelMedium)
                var roleExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRole?.name ?: "Select Role",
                        onValueChange = {},
                        readOnly = true,
                        enabled = existingRole == null, // Can't change role when editing
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            if (existingRole == null) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded)
                            }
                        }
                    )
                    if (existingRole == null) {
                        ExposedDropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            availableRoles.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role.name) },
                                    onClick = {
                                        selectedRoleId = role.id
                                        if (label.isBlank()) {
                                            label = role.name
                                        }
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Role display name") }
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Role description") },
                    minLines = 2
                )
                
                // Emoji
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("🎮") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRoleSaved(
                        RoleMenuRole(
                            roleId = selectedRoleId,
                            label = label.ifBlank { selectedRole?.name ?: "Role" },
                            description = description.ifBlank { null },
                            emoji = emoji.ifBlank { null }
                        )
                    )
                },
                enabled = selectedRoleId.isNotBlank() && (label.isNotBlank() || selectedRole != null)
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
