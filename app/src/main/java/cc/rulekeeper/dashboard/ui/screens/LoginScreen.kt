package cc.rulekeeper.dashboard.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.R
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.AuthRepository
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    settingsRepository: SettingsRepository,
    oauthCode: String? = null,
    onOauthCodeProcessed: () -> Unit = {},
    onLoginSuccess: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var useDiscordLogin by remember { mutableStateOf(true) }
    var processedOauthCode by remember { mutableStateOf<String?>(null) }
    
    // Handle OAuth callback
    LaunchedEffect(oauthCode) {
        if (oauthCode != null && !isLoading && processedOauthCode != oauthCode) {
            processedOauthCode = oauthCode  // Mark this code as processed
            isLoading = true
            errorMessage = null
            
            scope.launch {
                try {
                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                    val apiClient = ApiClient.getInstance(apiBaseUrl) { null }
                    val authRepository = AuthRepository(apiClient, settingsRepository)
                    
                    val result = authRepository.handleDiscordCallback(
                        code = oauthCode,
                        redirectUri = null  // Not needed for token exchange, server handled the redirect
                    )
                    
                    result.onSuccess {
                        onOauthCodeProcessed()  // Clear the code from MainActivity
                        onLoginSuccess()
                    }.onFailure { error ->
                        onOauthCodeProcessed()  // Clear the code even on failure
                        errorMessage = error.message ?: "Discord login failed"
                    }
                } catch (e: Exception) {
                    onOauthCodeProcessed()  // Clear the code on exception
                    errorMessage = "Error: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login to RuleKeeper") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // RuleKeeper Logo
            Image(
                painter = painterResource(id = R.drawable.ic_rulekeeper_logo),
                contentDescription = "RuleKeeper Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Text(
                "RuleKeeper Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        if (useDiscordLogin) "Discord Login" else "Bot Admin Login",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (!useDiscordLogin) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            visualTransformation = if (passwordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val authRepository = AuthRepository(apiClient, settingsRepository)
                                        
                                        val result = authRepository.login(username, password)
                                        
                                        result.onSuccess { response ->
                                            if (response.requiresMfa == true) {
                                                errorMessage = "MFA is required. Please use the web dashboard for MFA login."
                                            } else if (response.accessToken != null) {
                                                onLoginSuccess()
                                            } else {
                                                errorMessage = response.message ?: "Login failed"
                                            }
                                        }.onFailure { error ->
                                            errorMessage = error.message ?: "Login failed"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Network error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login")
                            }
                        }
                    } else {
                        Text(
                            "Login with your Discord account to access servers you manage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    
                                    try {
                                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                        
                                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                            settingsRepository.getCachedAccessToken()
                                        }
                                        val authRepository = AuthRepository(apiClient, settingsRepository)
                                        
                        // Request Discord OAuth URL with server redirect URI
                        // The server will redirect back to the mobile app using the state parameter
                        val result = authRepository.login(
                            username = "",
                            password = "",
                            useDiscord = true,
                            redirectUri = null,  // Use default server callback
                            isMobile = true  // Tell server this is a mobile request
                        )
                                        
                                        result.onSuccess { response ->
                                            // Open OAuth URL in browser
                                            response.oauthUrl?.let { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } ?: run {
                                                errorMessage = "Failed to get Discord login URL"
                                            }
                                        }.onFailure { error ->
                                            errorMessage = error.message ?: "Failed to initiate Discord login"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Network error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Login with Discord")
                            }
                        }
                    }
                }
            }
            
            TextButton(
                onClick = { useDiscordLogin = !useDiscordLogin },
                enabled = !isLoading
            ) {
                Text(
                    if (useDiscordLogin) {
                        "Use Bot Admin Login Instead"
                    } else {
                        "Use Discord Login Instead"
                    }
                )
            }
        }
    }
}
