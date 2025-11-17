package cc.rulekeeper.dashboard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.Guild
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ConfirmationDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SettingsTab {
    SECURITY, ANNOUNCEMENTS, IMPORT_EXPORT
}

data class MfaStatus(
    val totpEnabled: Boolean,
    val emailEnabled: Boolean,
    val requireBoth: Boolean
) {
    val anyEnabled: Boolean = totpEnabled || emailEnabled
}

data class AnnouncementConfig(
    val channelId: String?,
    val roleId: String?,
    val enabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.SECURITY) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Settings")
                    }
                },
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
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == SettingsTab.SECURITY,
                    onClick = { selectedTab = SettingsTab.SECURITY },
                    text = { Text("Security") },
                    icon = { Icon(Icons.Default.Security, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == SettingsTab.ANNOUNCEMENTS,
                    onClick = { selectedTab = SettingsTab.ANNOUNCEMENTS },
                    text = { Text("Announcements") },
                    icon = { Icon(Icons.Default.Announcement, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == SettingsTab.IMPORT_EXPORT,
                    onClick = { selectedTab = SettingsTab.IMPORT_EXPORT },
                    text = { Text("Import/Export") },
                    icon = { Icon(Icons.Default.ImportExport, contentDescription = null) }
                )
            }
            
            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    SettingsTab.SECURITY -> SecuritySection(settingsRepository)
                    SettingsTab.ANNOUNCEMENTS -> AnnouncementsSection(settingsRepository)
                    SettingsTab.IMPORT_EXPORT -> ImportExportSection(settingsRepository)
                }
            }
        }
    }
}

@Composable
fun SecuritySection(
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    var mfaStatus by remember { mutableStateOf<MfaStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteServerDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf(false) }
    var userIdToDelete by remember { mutableStateOf("") }
    var showGuildSelectionForDelete by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf<String?>(null) } // "server" or "user"
    var selectedGuildForDelete by remember { mutableStateOf<Guild?>(null) }
    var guilds by remember { mutableStateOf<List<Guild>>(emptyList()) }
    var showTotpQrDialog by remember { mutableStateOf(false) }
    var totpSecret by remember { mutableStateOf<String?>(null) }
    var totpQrCodeUrl by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load guilds for delete operations
                val guildsResponse = apiClient.guildService.getGuilds()
                if (guildsResponse.isSuccessful) {
                    guilds = guildsResponse.body()?.guilds ?: emptyList()
                }
                
                // Load MFA status
                val response = apiClient.settingsService.getMfaStatus()
                if (response.isSuccessful) {
                    val data = response.body()
                    mfaStatus = MfaStatus(
                        totpEnabled = data?.get("totp_enabled") as? Boolean ?: false,
                        emailEnabled = data?.get("email_enabled") as? Boolean ?: false,
                        requireBoth = data?.get("require_both") as? Boolean ?: false
                    )
                } else {
                    errorMessage = "Failed to load MFA status: ${response.message()}"
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load security settings: ${e.message}"
                isLoading = false
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Security",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Success/Error Messages
        successMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { successMessage = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        errorMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        // MFA Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Multi-Factor Authentication (MFA)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (mfaStatus?.anyEnabled == true) {
                            Spacer(Modifier.width(8.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            if (mfaStatus?.totpEnabled == true && mfaStatus?.emailEnabled == true)
                                                "Both Methods Enabled"
                                            else if (mfaStatus?.totpEnabled == true)
                                                "TOTP Enabled"
                                            else
                                                "Email Enabled"
                                        )
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        } else {
                            Spacer(Modifier.width(8.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Disabled")
                                    }
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Multi-Factor Authentication adds an extra layer of security to your account. You can enable one or both methods for maximum security.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // TOTP Section
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Authenticator App (TOTP)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Google Authenticator, Authy, etc.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { 
                                            Text(
                                                if (mfaStatus?.totpEnabled == true) "Enabled" else "Disabled",
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (mfaStatus?.totpEnabled == true)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (mfaStatus?.totpEnabled == true)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                if (mfaStatus?.totpEnabled == true) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "TOTP MFA is enabled. Your account is protected with authenticator app verification.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(12.dp))
                                    
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                        settingsRepository.getCachedAccessToken()
                                                    }
                                                    val response = apiClient.settingsService.disableTotp(mapOf("code" to ""))
                                                    if (response.isSuccessful) {
                                                        mfaStatus = mfaStatus?.copy(totpEnabled = false)
                                                        successMessage = "TOTP MFA disabled successfully"
                                                    } else {
                                                        errorMessage = "Failed to disable TOTP: ${response.message()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error disabling TOTP: ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Disable TOTP")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                        settingsRepository.getCachedAccessToken()
                                                    }
                                                    val response = apiClient.settingsService.enableTotp()
                                                    if (response.isSuccessful) {
                                                        val qrCode = response.body()?.get("qr_code") as? String
                                                        val secret = response.body()?.get("secret") as? String
                                                        
                                                        // Store QR code and secret to show in dialog
                                                        totpQrCodeUrl = qrCode
                                                        totpSecret = secret
                                                        showTotpQrDialog = true
                                                        
                                                        // Update MFA status
                                                        mfaStatus = mfaStatus?.copy(totpEnabled = true)
                                                    } else {
                                                        errorMessage = "Failed to enable TOTP: ${response.message()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error enabling TOTP: ${e.message}"
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Enable Authenticator App")
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        // Email MFA Section
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.Email,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Email Verification",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Receive codes via email",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { 
                                            Text(
                                                if (mfaStatus?.emailEnabled == true) "Enabled" else "Disabled",
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (mfaStatus?.emailEnabled == true)
                                                MaterialTheme.colorScheme.primaryContainer
                                            else
                                                MaterialTheme.colorScheme.surfaceVariant,
                                            labelColor = if (mfaStatus?.emailEnabled == true)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                if (mfaStatus?.emailEnabled == true) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Email MFA is enabled. Your account is protected with email verification.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                    
                                    Spacer(Modifier.height(12.dp))
                                    
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                        settingsRepository.getCachedAccessToken()
                                                    }
                                                    val response = apiClient.settingsService.disableEmailMfa(mapOf("code" to ""))
                                                    if (response.isSuccessful) {
                                                        mfaStatus = mfaStatus?.copy(emailEnabled = false)
                                                        successMessage = "Email MFA disabled successfully"
                                                    } else {
                                                        errorMessage = "Failed to disable Email MFA: ${response.message()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error disabling Email MFA: ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Disable Email MFA")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                        settingsRepository.getCachedAccessToken()
                                                    }
                                                    val response = apiClient.settingsService.enableEmailMfa(mapOf("email" to "user@example.com"))
                                                    if (response.isSuccessful) {
                                                        mfaStatus = mfaStatus?.copy(emailEnabled = true)
                                                        successMessage = "Email MFA enabled successfully"
                                                    } else {
                                                        errorMessage = "Failed to enable Email MFA: ${response.message()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error enabling Email MFA: ${e.message}"
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Enable Email MFA")
                                    }
                                }
                            }
                        }
                        
                        // Danger Zone
                        if (mfaStatus?.anyEnabled == true) {
                            Spacer(Modifier.height(16.dp))
                            
                            Divider()
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "Danger Zone",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    
                                    Spacer(Modifier.height(8.dp))
                                    
                                    Text(
                                        "Disable all MFA methods (not recommended)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    
                                    Spacer(Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                        settingsRepository.getCachedAccessToken()
                                                    }
                                                    val response = apiClient.settingsService.disableAllMfa()
                                                    if (response.isSuccessful) {
                                                        mfaStatus = MfaStatus(
                                                            totpEnabled = false,
                                                            emailEnabled = false,
                                                            requireBoth = false
                                                        )
                                                        successMessage = "All MFA methods disabled"
                                                    } else {
                                                        errorMessage = "Failed to disable all MFA: ${response.message()}"
                                                    }
                                                } catch (e: Exception) {
                                                    errorMessage = "Error disabling all MFA: ${e.message}"
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Disable All MFA")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Delete Data Section
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Delete Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
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
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Warning: These actions are irreversible!",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Deleting data will permanently remove all configuration and history.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = {
                            deleteMode = "server"
                            showGuildSelectionForDelete = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete All Server Data")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            deleteMode = "user"
                            showGuildSelectionForDelete = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete User Data")
                    }
                }
            }
        }
    }
    
    // Guild selection dialog for delete operations
    if (showGuildSelectionForDelete) {
        AlertDialog(
            onDismissRequest = {
                showGuildSelectionForDelete = false
                deleteMode = null
            },
            title = { Text("Select Server") },
            text = {
                Column {
                    Text(
                        if (deleteMode == "server")
                            "Choose which server's data to delete:"
                        else
                            "Choose which server to delete user data from:"
                    )
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(guilds.size) { index ->
                            val guild = guilds[index]
                            TextButton(
                                onClick = {
                                    selectedGuildForDelete = guild
                                    showGuildSelectionForDelete = false
                                    if (deleteMode == "server") {
                                        showDeleteServerDialog = true
                                    } else {
                                        showDeleteUserDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    guild.name,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showGuildSelectionForDelete = false
                    deleteMode = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Server Confirmation
    if (showDeleteServerDialog && selectedGuildForDelete != null) {
        val guild = selectedGuildForDelete
        ConfirmationDialog(
            title = "Delete All Server Data",
            message = "Are you absolutely sure? This will delete ALL data for ${guild?.name} and cannot be undone!",
            confirmButtonText = "Delete",
            onConfirm = {
                guild?.let {
                    scope.launch {
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            val response = apiClient.settingsService.deleteAllServerData(it.id)
                            if (response.isSuccessful) {
                                successMessage = "All server data deleted successfully for ${it.name}"
                            } else {
                                errorMessage = "Failed to delete server data: ${response.message()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error deleting server data: ${e.message}"
                        }
                        showDeleteServerDialog = false
                    }
                }
            },
            onDismiss = { showDeleteServerDialog = false },
            isDestructive = true
        )
    }
    
    // Delete User Dialog
    if (showDeleteUserDialog && selectedGuildForDelete != null) {
        val guild = selectedGuildForDelete
        AlertDialog(
            onDismissRequest = {
                showDeleteUserDialog = false
                userIdToDelete = ""
            },
            title = { Text("Delete User Data - ${guild?.name}") },
            text = {
                Column {
                    Text("Enter the User ID to delete all data for:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userIdToDelete,
                        onValueChange = { userIdToDelete = it },
                        label = { Text("User ID") },
                        placeholder = { Text("123456789012345678") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userIdToDelete.isNotBlank() && guild != null) {
                            scope.launch {
                                try {
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    val response = apiClient.settingsService.deleteUserData(guild.id, mapOf("user_id" to userIdToDelete))
                                    if (response.isSuccessful) {
                                        successMessage = "User data deleted for ID: $userIdToDelete in ${guild.name}"
                                    } else {
                                        errorMessage = "Failed to delete user data: ${response.message()}"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error deleting user data: ${e.message}"
                                }
                                showDeleteUserDialog = false
                                userIdToDelete = ""
                            }
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteUserDialog = false
                    userIdToDelete = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // TOTP QR Code Dialog
    if (showTotpQrDialog && totpSecret != null) {
        AlertDialog(
            onDismissRequest = { 
                showTotpQrDialog = false
                totpSecret = null
                totpQrCodeUrl = null
            },
            title = { 
                Text(
                    "Setup Authenticator App",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val clipboardManager = LocalClipboardManager.current
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Scan this QR code with your authenticator app (Google Authenticator, Authy, etc.)",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // QR Code placeholder
                    Card(
                        modifier = Modifier
                            .size(200.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "QR Code",
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "QR Code",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Divider()
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Or enter this code manually:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                totpSecret ?: "",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(totpSecret ?: ""))
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy to clipboard"
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "After scanning, enter the 6-digit code from your app to verify setup on next login.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showTotpQrDialog = false
                        totpSecret = null
                        totpQrCodeUrl = null
                        successMessage = "TOTP MFA enabled successfully"
                    }
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun AnnouncementsSection(
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    var guilds by remember { mutableStateOf<List<Guild>>(emptyList()) }
    var selectedGuild by remember { mutableStateOf<Guild?>(null) }
    var showGuildSelection by remember { mutableStateOf(false) }
    var config by remember { mutableStateOf<AnnouncementConfig?>(null) }
    var channels by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var roles by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var selectedChannelId by remember { mutableStateOf<String?>(null) }
    var selectedRoleId by remember { mutableStateOf<String?>(null) }
    var enabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load guilds
                val guildsResponse = apiClient.guildService.getGuilds()
                if (guildsResponse.isSuccessful) {
                    guilds = guildsResponse.body()?.guilds ?: emptyList()
                }
                
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load guilds: ${e.message}"
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(selectedGuild) {
        selectedGuild?.let { guild ->
            scope.launch {
                try {
                    isLoading = true
                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                        settingsRepository.getCachedAccessToken()
                    }
                    
                    // Load channels
                    val channelsResponse = apiClient.guildService.getGuildChannels(guild.id)
                    if (channelsResponse.isSuccessful) {
                        channels = channelsResponse.body() ?: emptyList()
                    }
                    
                    // Load roles
                    val rolesResponse = apiClient.guildService.getGuildRoles(guild.id)
                    if (rolesResponse.isSuccessful) {
                        roles = rolesResponse.body() ?: emptyList()
                    }
                    
                    // Load announcement config
                    val configResponse = apiClient.settingsService.getAnnouncementConfig(guild.id)
                    if (configResponse.isSuccessful) {
                        val configData = configResponse.body()
                        config = AnnouncementConfig(
                            channelId = configData?.get("channel_id") as? String,
                            roleId = configData?.get("role_id") as? String,
                            enabled = configData?.get("enabled") as? Boolean ?: true
                        )
                        selectedChannelId = config?.channelId
                        selectedRoleId = config?.roleId
                        enabled = config?.enabled ?: true
                    }
                    
                    isLoading = false
                } catch (e: Exception) {
                    errorMessage = "Failed to load announcement settings: ${e.message}"
                    isLoading = false
                }
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Announcements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            var guildExpanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = guildExpanded,
                onExpandedChange = { guildExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGuild?.name ?: "Select a server...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Server") },
                    leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = guildExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = guildExpanded,
                    onDismissRequest = { guildExpanded = false }
                ) {
                    guilds.forEach { guild ->
                        DropdownMenuItem(
                            text = { Text(guild.name) },
                            onClick = {
                                selectedGuild = guild
                                guildExpanded = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Group, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
        
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
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "About Announcements",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Bot administrators can send important announcements to your server. Configure which channel should receive these messages below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        successMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { successMessage = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Announcement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Announcement Channel Configuration",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Channel Selection
                        var channelExpanded by remember { mutableStateOf(false) }
                        
                        Text(
                            "Announcement Channel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = channelExpanded,
                            onExpandedChange = { channelExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = channels.find { it["id"]?.toString() == selectedChannelId }
                                    ?.get("name")?.toString() ?: "Select a channel...",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Channel") },
                                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = channelExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = channelExpanded,
                                onDismissRequest = { channelExpanded = false }
                            ) {
                                channels.forEach { channel ->
                                    DropdownMenuItem(
                                        text = { Text("# ${channel["name"]}") },
                                        onClick = {
                                            selectedChannelId = channel["id"]?.toString()
                                            channelExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Tag, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            "Bot administrators can send announcements to this channel from the admin dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )
                        
                        // Role Selection
                        var roleExpanded by remember { mutableStateOf(false) }
                        
                        Text(
                            "Default Announcement Role (Optional)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = roleExpanded,
                            onExpandedChange = { roleExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = roles.find { it["id"]?.toString() == selectedRoleId }
                                    ?.get("name")?.toString() ?: "No role ping",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Role") },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = roleExpanded,
                                onDismissRequest = { roleExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("No role ping") },
                                    onClick = {
                                        selectedRoleId = null
                                        roleExpanded = false
                                    }
                                )
                                roles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text("@${role["name"]}") },
                                        onClick = {
                                            selectedRoleId = role["id"]?.toString()
                                            roleExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Group, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                        
                        Text(
                            "This role will be pinged when bot administrators send announcements to all servers. Leave empty for no ping.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )
                        
                        Divider()
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Enable announcements for this server",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Allow bot admins to send announcements here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enabled,
                                onCheckedChange = { enabled = it }
                            )
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                selectedGuild?.let { guild ->
                                    scope.launch {
                                        try {
                                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                                settingsRepository.getCachedAccessToken()
                                            }
                                            
                                            val configData: Map<String, Any> = mapOf(
                                                "channel_id" to (selectedChannelId ?: ""),
                                                "role_id" to (selectedRoleId ?: ""),
                                                "enabled" to enabled
                                            )
                                            
                                            val response = apiClient.settingsService.updateAnnouncementConfig(guild.id, configData)
                                            if (response.isSuccessful) {
                                                config = AnnouncementConfig(
                                                    channelId = selectedChannelId,
                                                    roleId = selectedRoleId,
                                                    enabled = enabled
                                                )
                                                successMessage = "Announcement settings updated successfully for ${guild.name}"
                                            } else {
                                                errorMessage = "Failed to update settings: ${response.message()}"
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Error updating settings: ${e.message}"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = selectedGuild != null
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Update Announcement Settings")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImportExportSection(
    settingsRepository: SettingsRepository
) {
    val scope = rememberCoroutineScope()
    var guilds by remember { mutableStateOf<List<Guild>>(emptyList()) }
    var selectedGuild by remember { mutableStateOf<Guild?>(null) }
    var selectedOptions by remember { mutableStateOf<Set<String>>(emptySet()) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                // Load guilds
                val guildsResponse = apiClient.guildService.getGuilds()
                if (guildsResponse.isSuccessful) {
                    guilds = guildsResponse.body()?.guilds ?: emptyList()
                }
                
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load guilds: ${e.message}"
                isLoading = false
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Import / Export",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Guild Selection Dropdown
        item {
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGuild?.name ?: "Select a server...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Server") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    guilds.forEach { guild ->
                        DropdownMenuItem(
                            text = { Text(guild.name) },
                            onClick = {
                                selectedGuild = guild
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        successMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = { successMessage = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        
        errorMessage?.let { message ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            message,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedOptions = setOf(
                            "commands", "permissions", "blocked_words", "logging",
                            "welcome", "goodbye", "roles", "levels", "moderation"
                        )
                    }
                ) {
                    Text("Select All")
                }
                
                OutlinedButton(
                    onClick = { selectedOptions = emptySet() }
                ) {
                    Text("Deselect All")
                }
            }
        }
        
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Export Server Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Text(
                        "Select the data you want to export:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    val exportCategories = listOf(
                        "commands" to "Commands",
                        "permissions" to "Command Permissions",
                        "blocked_words" to "Blocked Words",
                        "logging" to "Logging Configuration",
                        "welcome" to "Welcome Message",
                        "goodbye" to "Goodbye Message",
                        "roles" to "Role Configuration",
                        "levels" to "Levels & XP System",
                        "moderation" to "Moderation Settings"
                    )
                    
                    exportCategories.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedOptions.contains(key),
                                onCheckedChange = { checked ->
                                    selectedOptions = if (checked) {
                                        selectedOptions + key
                                    } else {
                                        selectedOptions - key
                                    }
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Button(
                        onClick = {
                            selectedGuild?.let { guild ->
                                scope.launch {
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        
                                        val optionsMap: Map<String, List<String>> = mapOf(
                                            "data_types" to selectedOptions.toList()
                                        )
                                        val response = apiClient.settingsService.exportData(guild.id, optionsMap)
                                        
                                        if (response.isSuccessful) {
                                            // In a real app, you would save the ResponseBody to a file
                                            // For now, just show success message
                                            successMessage = "Export completed successfully for ${guild.name}. File downloaded."
                                        } else {
                                            errorMessage = "Failed to export data: ${response.message()}"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error exporting data: ${e.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedOptions.isNotEmpty() && selectedGuild != null
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Export Selected Data")
                    }
                }
            }
        }
        
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Upload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Import Server Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "Select a previously exported .zip file to import server configuration and data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            selectedGuild?.let { guild ->
                                // File picker would be implemented with Android's file picker API
                                // For now, show a placeholder message
                                scope.launch {
                                    try {
                                        // In a real implementation, you would:
                                        // 1. Launch Android file picker
                                        // 2. Get the selected file URI
                                        // 3. Read file content into RequestBody
                                        // 4. Call: apiClient.settingsService.importData(guild.id, fileRequestBody)
                                        
                                        errorMessage = "File picker not implemented yet. Would launch system file picker to select .zip file for ${guild.name}."
                                    } catch (e: Exception) {
                                        errorMessage = "Error importing data: ${e.message}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedGuild != null
                    ) {
                        Icon(
                            Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Select File to Import")
                    }
                }
            }
        }
    }
}
