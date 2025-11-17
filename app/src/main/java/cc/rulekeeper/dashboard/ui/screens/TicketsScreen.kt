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
import java.text.SimpleDateFormat
import java.util.*

data class Ticket(
    val id: String,
    val userId: String,
    val username: String,
    val subject: String,
    val status: TicketStatus,
    val createdAt: Long,
    val channelId: String?,
    val category: String
)

enum class TicketStatus {
    OPEN, IN_PROGRESS, CLOSED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tickets by remember { 
        mutableStateOf<List<Ticket>>(emptyList())
    }
    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf<TicketStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load tickets
    LaunchedEffect(guildId) {
        scope.launch {
            try {
                isLoading = true
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                val response = apiClient.ticketsService.getTickets(guildId)
                if (response.isSuccessful && response.body() != null) {
                    // Convert API response to Ticket objects
                    tickets = response.body()!!.mapNotNull { map ->
                        try {
                            Ticket(
                                id = map["id"]?.toString() ?: "",
                                userId = map["user_id"]?.toString() ?: "",
                                username = map["username"]?.toString() ?: "Unknown",
                                subject = map["subject"]?.toString() ?: "No subject",
                                status = when (map["status"]?.toString()) {
                                    "IN_PROGRESS" -> TicketStatus.IN_PROGRESS
                                    "CLOSED" -> TicketStatus.CLOSED
                                    else -> TicketStatus.OPEN
                                },
                                createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L,
                                channelId = map["channel_id"]?.toString(),
                                category = map["category"]?.toString() ?: "General"
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tickets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showCreateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Create ticket")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Tickets") },
                    icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Configuration") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }
            
            when (selectedTab) {
                0 -> TicketsList(
                    tickets = tickets,
                    filterStatus = filterStatus,
                    onFilterChange = { filterStatus = it },
                    onTicketClick = { ticket -> /* Open ticket details */ },
                    onCloseTicket = { ticket ->
                        tickets = tickets.map {
                            if (it.id == ticket.id) it.copy(status = TicketStatus.CLOSED) else it
                        }
                    }
                )
                1 -> TicketsConfiguration(guildId = guildId)
            }
        }
    }
    
    if (showCreateDialog) {
        CreateTicketDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { subject, category ->
                // Create ticket logic
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsList(
    tickets: List<Ticket>,
    filterStatus: TicketStatus?,
    onFilterChange: (TicketStatus?) -> Unit,
    onTicketClick: (Ticket) -> Unit,
    onCloseTicket: (Ticket) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { onFilterChange(null) },
                label = { Text("All") }
            )
            FilterChip(
                selected = filterStatus == TicketStatus.OPEN,
                onClick = { onFilterChange(TicketStatus.OPEN) },
                label = { Text("Open") },
                leadingIcon = {
                    Icon(Icons.Default.Circle, contentDescription = null, 
                         modifier = Modifier.size(8.dp),
                         tint = MaterialTheme.colorScheme.error)
                }
            )
            FilterChip(
                selected = filterStatus == TicketStatus.IN_PROGRESS,
                onClick = { onFilterChange(TicketStatus.IN_PROGRESS) },
                label = { Text("In Progress") },
                leadingIcon = {
                    Icon(Icons.Default.Circle, contentDescription = null,
                         modifier = Modifier.size(8.dp),
                         tint = MaterialTheme.colorScheme.tertiary)
                }
            )
            FilterChip(
                selected = filterStatus == TicketStatus.CLOSED,
                onClick = { onFilterChange(TicketStatus.CLOSED) },
                label = { Text("Closed") },
                leadingIcon = {
                    Icon(Icons.Default.Circle, contentDescription = null,
                         modifier = Modifier.size(8.dp),
                         tint = MaterialTheme.colorScheme.surfaceVariant)
                }
            )
        }
        
        val filteredTickets = if (filterStatus != null) {
            tickets.filter { it.status == filterStatus }
        } else {
            tickets
        }
        
        if (filteredTickets.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (filterStatus != null) "No ${filterStatus.name.lowercase().replace('_', ' ')} tickets"
                        else "No tickets yet"
                    )
                    Text(
                        "Tap + to create a ticket",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTickets) { ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket) },
                        onClose = { onCloseTicket(ticket) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketCard(
    ticket: Ticket,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    var showCloseDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = when (ticket.status) {
                            TicketStatus.OPEN -> MaterialTheme.colorScheme.error
                            TicketStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                            TicketStatus.CLOSED -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                    Column {
                        Text(
                            ticket.subject,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "by ${ticket.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (ticket.status != TicketStatus.CLOSED) {
                            DropdownMenuItem(
                                text = { Text("Mark In Progress") },
                                onClick = {
                                    // Update status
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Close Ticket") },
                                onClick = {
                                    showCloseDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Reopen Ticket") },
                                onClick = {
                                    // Reopen ticket
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete Ticket") },
                            onClick = {
                                // Delete ticket
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(ticket.category) },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    }
                )
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date(ticket.createdAt))) 
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                    }
                )
            }
        }
    }
    
    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Close Ticket?") },
            text = { Text("Are you sure you want to close this ticket?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClose()
                        showCloseDialog = false
                    }
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TicketsConfiguration(guildId: String) {
    val scope = rememberCoroutineScope()
    val settingsRepository = SettingsRepository(LocalContext.current)
    val apiClient = ApiClient.getInstance() { settingsRepository.getCachedAccessToken() }
    
    var enabled by remember { mutableStateOf(true) }
    var ticketCategoryId by remember { mutableStateOf("") }
    var supportRoleId by remember { mutableStateOf("") }
    var maxTicketsPerUser by remember { mutableStateOf("3") }
    var ticketCategories by remember { mutableStateOf(listOf("General", "Bug Report", "Appeal", "Other")) }
    var welcomeMessage by remember { mutableStateOf("Thank you for creating a ticket! A staff member will be with you shortly.") }
    var autoCloseInactive by remember { mutableStateOf(false) }
    var inactiveDays by remember { mutableStateOf("7") }
    
    // Ticket menus state
    var ticketMenus by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var menuToEdit by remember { mutableStateOf<Map<String, Any>?>(null) }
    var menuToDelete by remember { mutableStateOf<Map<String, Any>?>(null) }
    var showAddMenuDialog by remember { mutableStateOf(false) }
    var isLoadingMenus by remember { mutableStateOf(false) }
    var menuErrorMessage by remember { mutableStateOf<String?>(null) }
    
    // Load ticket menus
    LaunchedEffect(guildId) {
        isLoadingMenus = true
        try {
            val response = apiClient.ticketsService.getTicketMenus(guildId)
            if (response.isSuccessful) {
                ticketMenus = response.body() ?: emptyList()
            } else {
                menuErrorMessage = "Failed to load ticket menus: ${response.code()}"
            }
        } catch (e: Exception) {
            menuErrorMessage = "Error loading ticket menus: ${e.message}"
        } finally {
            isLoadingMenus = false
        }
    }
    
    // Confirmation dialog for menu deletion
    if (menuToDelete != null) {
        ConfirmationDialog(
            title = "Delete Ticket Menu",
            message = "Are you sure you want to delete the menu \"${menuToDelete!!["label"] ?: "Unknown"}\"? This cannot be undone.",
            onConfirm = {
                scope.launch {
                    try {
                        val menuId = (menuToDelete!!["id"] as? Number)?.toInt() ?: return@launch
                        val response = apiClient.ticketsService.deleteTicketMenu(guildId, menuId)
                        if (response.isSuccessful) {
                            ticketMenus = ticketMenus.filterNot { 
                                (it["id"] as? Number)?.toInt() == menuId 
                            }
                            menuToDelete = null
                        } else {
                            menuErrorMessage = "Failed to delete menu: ${response.code()}"
                            menuToDelete = null
                        }
                    } catch (e: Exception) {
                        menuErrorMessage = "Error deleting menu: ${e.message}"
                        menuToDelete = null
                    }
                }
            },
            onDismiss = { menuToDelete = null },
            isDestructive = true
        )
    }
    
    // Add ticket menu dialog
    if (showAddMenuDialog) {
        AddTicketMenuDialog(
            onDismiss = { showAddMenuDialog = false },
            onCreate = { menuData ->
                scope.launch {
                    try {
                        val response = apiClient.ticketsService.createTicketMenu(guildId, menuData)
                        if (response.isSuccessful) {
                            response.body()?.let { newMenu ->
                                ticketMenus = ticketMenus + newMenu
                            }
                            showAddMenuDialog = false
                        } else {
                            menuErrorMessage = "Failed to create menu: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        menuErrorMessage = "Error creating menu: ${e.message}"
                    }
                }
            }
        )
    }
    
    // Edit ticket menu dialog
    if (menuToEdit != null) {
        EditTicketMenuDialog(
            menu = menuToEdit!!,
            onDismiss = { menuToEdit = null },
            onSave = { menuId, updatedData ->
                scope.launch {
                    try {
                        val response = apiClient.ticketsService.updateTicketMenu(guildId, menuId, updatedData)
                        if (response.isSuccessful) {
                            response.body()?.let { updatedMenu ->
                                ticketMenus = ticketMenus.map { 
                                    if ((it["id"] as? Number)?.toInt() == menuId) updatedMenu else it 
                                }
                            }
                            menuToEdit = null
                        } else {
                            menuErrorMessage = "Failed to update menu: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        menuErrorMessage = "Error updating menu: ${e.message}"
                    }
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Ticket System", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it }
                        )
                    }
                }
            }
        }
        
        if (enabled) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Channel Settings", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = ticketCategoryId,
                            onValueChange = { ticketCategoryId = it },
                            label = { Text("Ticket Category ID") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Discord category where tickets will be created") }
                        )
                        
                        OutlinedTextField(
                            value = supportRoleId,
                            onValueChange = { supportRoleId = it },
                            label = { Text("Support Role ID") },
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("Role that can view and manage tickets") }
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Ticket Limits", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = maxTicketsPerUser,
                            onValueChange = { maxTicketsPerUser = it },
                            label = { Text("Max tickets per user") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Welcome Message", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = welcomeMessage,
                            onValueChange = { welcomeMessage = it },
                            label = { Text("Ticket welcome message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Ticket Categories", style = MaterialTheme.typography.titleMedium)
                        
                        ticketCategories.forEach { category ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category)
                                IconButton(onClick = { 
                                    ticketCategories = ticketCategories - category 
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        
                        OutlinedButton(
                            onClick = { /* Add category dialog */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Category")
                        }
                    }
                }
            }
            
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-Close Inactive Tickets", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = autoCloseInactive,
                                onCheckedChange = { autoCloseInactive = it }
                            )
                        }
                        
                        if (autoCloseInactive) {
                            OutlinedTextField(
                                value = inactiveDays,
                                onValueChange = { inactiveDays = it },
                                label = { Text("Days before auto-close") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Ticket Menus Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ticket Menus", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(
                                onClick = { showAddMenuDialog = true }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Menu")
                            }
                        }
                        
                        if (isLoadingMenus) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (menuErrorMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        menuErrorMessage!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else if (ticketMenus.isEmpty()) {
                            Text(
                                "No ticket menus created yet. Click 'Add Menu' to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            ticketMenus.forEach { menu ->
                                TicketMenuCard(
                                    menu = menu,
                                    onEdit = { menuToEdit = menu },
                                    onDelete = { menuToDelete = menu }
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            "Users can create tickets using a button or command. Each ticket creates a private channel.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = { /* Save configuration */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Ticket Configuration")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var description by remember { mutableStateOf("") }
    val categories = listOf("General", "Bug Report", "Appeal", "Other")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Ticket") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Text("Category:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.take(2).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (subject.isNotBlank()) {
                        onCreate(subject, category)
                    }
                }
            ) {
                Text("Create")
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
fun TicketMenuCard(
    menu: Map<String, Any>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = (menu["label"] as? String) ?: "Ticket Menu",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "ID: ${menu["id"]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Menu type badge
                val menuType = menu["type"] as? String ?: "button"
                AssistChip(
                    onClick = { },
                    label = { Text(menuType.capitalize()) },
                    leadingIcon = {
                        Icon(
                            when (menuType) {
                                "dropdown" -> Icons.Default.List
                                "modal" -> Icons.Default.Chat
                                else -> Icons.Default.CheckBox
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            val description = (menu["description"] as? String) ?: "No description"
            Text(
                text = description.take(100) + if (description.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Channel info
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Tag,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Channel ID: ${menu["channel_id"]}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
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
fun AddTicketMenuDialog(
    onDismiss: () -> Unit,
    onCreate: (Map<String, Any>) -> Unit
) {
    var menuType by remember { mutableStateOf("button") }
    var label by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var channelId by remember { mutableStateOf("") }
    
    val menuTypes = listOf("button", "dropdown", "modal")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Ticket Menu") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Menu Type:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuTypes.forEach { type ->
                        FilterChip(
                            selected = menuType == type,
                            onClick = { menuType = type },
                            label = { Text(type.capitalize()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Menu Label") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Support Tickets") }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("Brief description of this ticket menu") }
                )
                
                OutlinedTextField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = { Text("Channel ID") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Discord channel ID") },
                    supportingText = { Text("Where the ticket menu will be sent") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank() && channelId.isNotBlank()) {
                        onCreate(mapOf(
                            "type" to menuType,
                            "label" to label,
                            "description" to description,
                            "channel_id" to channelId
                        ))
                    }
                },
                enabled = label.isNotBlank() && channelId.isNotBlank()
            ) {
                Text("Create")
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
fun EditTicketMenuDialog(
    menu: Map<String, Any>,
    onDismiss: () -> Unit,
    onSave: (menuId: Int, updatedData: Map<String, Any>) -> Unit
) {
    val menuId = (menu["id"] as? Number)?.toInt() ?: return
    var menuType by remember { mutableStateOf(menu["type"] as? String ?: "button") }
    var label by remember { mutableStateOf(menu["label"] as? String ?: "") }
    var description by remember { mutableStateOf(menu["description"] as? String ?: "") }
    var channelId by remember { mutableStateOf(menu["channel_id"]?.toString() ?: "") }
    
    val menuTypes = listOf("button", "dropdown", "modal")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Ticket Menu") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Menu Type:", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    menuTypes.forEach { type ->
                        FilterChip(
                            selected = menuType == type,
                            onClick = { menuType = type },
                            label = { Text(type.capitalize()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Menu Label") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                OutlinedTextField(
                    value = channelId,
                    onValueChange = { channelId = it },
                    label = { Text("Channel ID") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Where the ticket menu will be sent") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank() && channelId.isNotBlank()) {
                        onSave(menuId, mapOf(
                            "type" to menuType,
                            "label" to label,
                            "description" to description,
                            "channel_id" to channelId
                        ))
                    }
                },
                enabled = label.isNotBlank() && channelId.isNotBlank()
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
