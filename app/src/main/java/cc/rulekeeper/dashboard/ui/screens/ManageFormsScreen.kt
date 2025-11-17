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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFormsScreen(
    guildId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    onNavigateToEditForm: (String) -> Unit = {},
    onNavigateToFormSubmissions: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var forms by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var formToDelete by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val loadForms: suspend () -> Unit = {
        try {
            isLoading = true
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            val response = apiClient.formsService.getForms(guildId)
            if (response.isSuccessful && response.body() != null) {
                forms = response.body()!!["forms"] as? List<Map<String, Any>> ?: emptyList()
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId) {
        scope.launch {
            loadForms()
        }
    }
    
    if (formToDelete != null) {
        ConfirmationDialog(
            title = "Delete Form",
            message = "Are you sure you want to delete this form? All submissions will be lost. This cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.formsService.deleteForm(guildId, formToDelete!!)
                        if (response.isSuccessful) {
                            successMessage = "Form deleted successfully"
                            loadForms()
                        } else {
                            errorMessage = "Failed to delete form"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    }
                }
                formToDelete = null
            },
            onDismiss = { formToDelete = null }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Forms") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Form")
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
                                Icons.Default.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Custom Forms",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create custom forms for applications, surveys, and member submissions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            item {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    errorMessage != null -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    forms.isEmpty() -> {
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
                                    "No Forms Created",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Create custom forms for member applications, staff applications, surveys, and more.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { showCreateDialog = true }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Form")
                                }
                            }
                        }
                    }
                }
            }
            
            if (forms.isNotEmpty()) {
                items(forms) { form ->
                    FormListItem(
                        form = form,
                        onDelete = {
                            formToDelete = form["id"]?.toString()
                        },
                        onEdit = {
                            form["id"]?.toString()?.let { formId ->
                                onNavigateToEditForm(formId)
                            }
                        },
                        onViewSubmissions = {
                            form["id"]?.toString()?.let { formId ->
                                onNavigateToFormSubmissions(formId)
                            }
                        }
                    )
                }
            }
        }
        
        if (showCreateDialog) {
            CreateFormDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, description ->
                    scope.launch {
                        try {
                            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                settingsRepository.getCachedAccessToken()
                            }
                            val formData = mapOf(
                                "name" to name,
                                "description" to description,
                                "enabled" to true,
                                "fields" to emptyList<Any>()
                            )
                            val response = apiClient.formsService.createForm(guildId, formData)
                            if (response.isSuccessful) {
                                loadForms()
                                showCreateDialog = false
                            } else {
                                errorMessage = "Failed to create form"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CreateFormDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var formName by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Form") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formName,
                    onValueChange = { formName = it },
                    label = { Text("Form Name") },
                    placeholder = { Text("e.g., Staff Application") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = formDescription,
                    onValueChange = { formDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("Brief description of the form") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(formName, formDescription) },
                enabled = formName.isNotBlank()
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
fun FormListItem(
    form: Map<String, Any>,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onViewSubmissions: () -> Unit
) {
    Card {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        form["name"] as? String ?: "Unnamed Form",
                        style = MaterialTheme.typography.titleMedium
                    )
                    form["description"]?.let { desc ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            desc as String,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (form["enabled"] as? Boolean == true) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Divider()
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = onViewSubmissions) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Submissions")
                }
                TextButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}
