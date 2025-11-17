package cc.rulekeeper.dashboard.ui.screens.forms

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
import cc.rulekeeper.dashboard.ui.components.ErrorCard
import cc.rulekeeper.dashboard.ui.components.LoadingCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class FormSubmission(
    val id: String,
    val userId: String,
    val username: String,
    val submittedAt: String,
    val status: String,
    val responses: Map<String, Any>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSubmissionsScreen(
    guildId: String,
    formId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    onNavigateToSubmissionDetail: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var formName by remember { mutableStateOf("Loading...") }
    var submissions by remember { mutableStateOf<List<FormSubmission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submissionToDelete by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") }
    
    val loadSubmissions: suspend () -> Unit = {
        try {
            isLoading = true
            errorMessage = null
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            // Load form details
            val formResponse = apiClient.formsService.getForm(guildId, formId)
            if (formResponse.isSuccessful && formResponse.body() != null) {
                formName = formResponse.body()!!["name"]?.toString() ?: "Form"
            }
            
            // Load submissions
            val response = apiClient.formsService.getFormSubmissions(guildId, formId)
            if (response.isSuccessful && response.body() != null) {
                submissions = response.body()!!.mapNotNull { sub ->
                    try {
                        FormSubmission(
                            id = sub["id"]?.toString() ?: return@mapNotNull null,
                            userId = sub["user_id"]?.toString() ?: "",
                            username = sub["username"]?.toString() ?: "Unknown User",
                            submittedAt = sub["submitted_at"]?.toString() ?: "",
                            status = sub["status"]?.toString() ?: "pending",
                            responses = sub["responses"] as? Map<String, Any> ?: emptyMap()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                errorMessage = "Failed to load submissions"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId, formId) {
        loadSubmissions()
    }
    
    // Filter submissions
    val filteredSubmissions = submissions.filter { submission ->
        val matchesSearch = searchQuery.isEmpty() || 
            submission.username.contains(searchQuery, ignoreCase = true) ||
            submission.userId.contains(searchQuery, ignoreCase = true)
        val matchesStatus = filterStatus == "all" || submission.status == filterStatus
        matchesSearch && matchesStatus
    }
    
    if (submissionToDelete != null) {
        ConfirmationDialog(
            title = "Delete Submission",
            message = "Are you sure you want to delete this submission? This cannot be undone.",
            confirmButtonText = "Delete",
            onConfirm = {
                scope.launch {
                    try {
                        val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                        val apiClient = ApiClient.getInstance(apiBaseUrl) {
                            settingsRepository.getCachedAccessToken()
                        }
                        val response = apiClient.formsService.deleteSubmission(guildId, formId, submissionToDelete!!)
                        if (response.isSuccessful) {
                            successMessage = "Submission deleted successfully"
                            loadSubmissions()
                        } else {
                            errorMessage = "Failed to delete submission"
                        }
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        submissionToDelete = null
                    }
                }
            },
            onDismiss = {
                submissionToDelete = null
            },
            isDestructive = true
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$formName - Submissions") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                                    val apiClient = ApiClient.getInstance(apiBaseUrl) {
                                        settingsRepository.getCachedAccessToken()
                                    }
                                    // Export submissions
                                    val response = apiClient.formsService.exportSubmissions(guildId, formId)
                                    if (response.isSuccessful) {
                                        successMessage = "Submissions exported successfully"
                                    } else {
                                        errorMessage = "Failed to export submissions"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export Submissions")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Success message
            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            successMessage!!,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by username or ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterStatus == "all",
                    onClick = { filterStatus = "all" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterStatus == "pending",
                    onClick = { filterStatus = "pending" },
                    label = { Text("Pending") }
                )
                FilterChip(
                    selected = filterStatus == "approved",
                    onClick = { filterStatus = "approved" },
                    label = { Text("Approved") }
                )
                FilterChip(
                    selected = filterStatus == "rejected",
                    onClick = { filterStatus = "rejected" },
                    label = { Text("Rejected") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Submissions list
            when {
                isLoading -> LoadingCard(message = "Loading submissions...")
                errorMessage != null -> ErrorCard(
                    message = errorMessage!!,
                    onRetry = { scope.launch { loadSubmissions() } }
                )
                filteredSubmissions.isEmpty() -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Assignment,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Submissions Found",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "This form has no submissions yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredSubmissions) { submission ->
                            SubmissionListItem(
                                submission = submission,
                                onDelete = { submissionToDelete = submission.id },
                                onView = { onNavigateToSubmissionDetail(submission.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubmissionListItem(
    submission: FormSubmission,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with username and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        submission.username,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "ID: ${submission.userId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status badge
                AssistChip(
                    onClick = {},
                    label = { Text(submission.status.uppercase()) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (submission.status) {
                            "approved" -> MaterialTheme.colorScheme.primaryContainer
                            "rejected" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        labelColor = when (submission.status) {
                            "approved" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "rejected" -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Submission date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    formatTimestamp(submission.submittedAt),
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
                TextButton(
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Details")
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
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

fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp
    }
}
