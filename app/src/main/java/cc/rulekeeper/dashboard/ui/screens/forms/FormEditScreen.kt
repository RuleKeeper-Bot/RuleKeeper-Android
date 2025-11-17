package cc.rulekeeper.dashboard.ui.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.repository.SettingsRepository
import cc.rulekeeper.dashboard.ui.components.ErrorCard
import cc.rulekeeper.dashboard.ui.components.LoadingCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class FormField(
    val id: String = "",
    val label: String = "",
    val type: String = "text",
    val required: Boolean = false,
    val options: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEditScreen(
    guildId: String,
    formId: String,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    // Form data
    var formName by remember { mutableStateOf("") }
    var formDescription by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(true) }
    var fields by remember { mutableStateOf<List<FormField>>(emptyList()) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    
    val loadForm: suspend () -> Unit = {
        try {
            isLoading = true
            errorMessage = null
            val apiBaseUrl = settingsRepository.apiBaseUrl.first()
            val apiClient = ApiClient.getInstance(apiBaseUrl) {
                settingsRepository.getCachedAccessToken()
            }
            
            val response = apiClient.formsService.getForm(guildId, formId)
            if (response.isSuccessful && response.body() != null) {
                val form = response.body()!!
                formName = form["name"]?.toString() ?: ""
                formDescription = form["description"]?.toString() ?: ""
                isEnabled = form["enabled"] as? Boolean ?: true
                
                val fieldsList = form["fields"] as? List<Map<String, Any>> ?: emptyList()
                fields = fieldsList.mapNotNull { field ->
                    try {
                        FormField(
                            id = field["id"]?.toString() ?: "",
                            label = field["label"]?.toString() ?: "",
                            type = field["type"]?.toString() ?: "text",
                            required = field["required"] as? Boolean ?: false,
                            options = field["options"] as? List<String> ?: emptyList()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            } else {
                errorMessage = "Failed to load form"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error occurred"
        } finally {
            isLoading = false
        }
    }
    
    LaunchedEffect(guildId, formId) {
        loadForm()
    }
    
    val saveForm: () -> Unit = {
        scope.launch {
            try {
                val apiBaseUrl = settingsRepository.apiBaseUrl.first()
                val apiClient = ApiClient.getInstance(apiBaseUrl) {
                    settingsRepository.getCachedAccessToken()
                }
                
                val formData = mapOf(
                    "name" to formName,
                    "description" to formDescription,
                    "enabled" to isEnabled,
                    "fields" to fields.map { field ->
                        mapOf(
                            "id" to field.id,
                            "label" to field.label,
                            "type" to field.type,
                            "required" to field.required,
                            "options" to field.options
                        )
                    }
                )
                
                val response = apiClient.formsService.updateForm(guildId, formId, formData)
                if (response.isSuccessful) {
                    successMessage = "Form updated successfully"
                } else {
                    errorMessage = "Failed to update form"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error occurred"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Form") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = saveForm,
                        enabled = formName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFieldDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Field")
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingCard(message = "Loading form...")
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    ErrorCard(
                        message = errorMessage!!,
                        onRetry = { scope.launch { loadForm() } }
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Success message
                    if (successMessage != null) {
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
                        }
                    }
                    
                    // Basic info section
                    item {
                        Card {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Basic Information",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = formName,
                                    onValueChange = { formName = it },
                                    label = { Text("Form Name *") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = formDescription,
                                    onValueChange = { formDescription = it },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    maxLines = 5
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Form Enabled")
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { isEnabled = it }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Fields section header
                    item {
                        Text(
                            "Form Fields",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    // Form fields
                    if (fields.isEmpty()) {
                        item {
                            Card {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No Fields Added",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Add fields to your form using the + button",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(fields) { index, field ->
                            FormFieldItem(
                                field = field,
                                onDelete = {
                                    fields = fields.filterIndexed { i, _ -> i != index }
                                },
                                onMoveUp = if (index > 0) {
                                    {
                                        fields = fields.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index - 1]
                                            this[index - 1] = temp
                                        }
                                    }
                                } else null,
                                onMoveDown = if (index < fields.size - 1) {
                                    {
                                        fields = fields.toMutableList().apply {
                                            val temp = this[index]
                                            this[index] = this[index + 1]
                                            this[index + 1] = temp
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddFieldDialog) {
        AddFieldDialog(
            onDismiss = { showAddFieldDialog = false },
            onConfirm = { field ->
                fields = fields + field
                showAddFieldDialog = false
            }
        )
    }
}

@Composable
fun FormFieldItem(
    field: FormField,
    onDelete: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        field.label,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Type: ${field.type}${if (field.required) " • Required" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (onMoveUp != null) {
                        IconButton(onClick = onMoveUp) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up")
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(onClick = onMoveDown) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down")
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (field.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Options: ${field.options.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (FormField) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf("text") }
    var isRequired by remember { mutableStateOf(false) }
    var optionsText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Field") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Field Label *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Field type dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = fieldType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Field Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("text", "textarea", "number", "select", "checkbox").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    fieldType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                if (fieldType == "select") {
                    OutlinedTextField(
                        value = optionsText,
                        onValueChange = { optionsText = it },
                        label = { Text("Options (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Option 1, Option 2, Option 3") }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Required Field")
                    Switch(
                        checked = isRequired,
                        onCheckedChange = { isRequired = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val options = if (fieldType == "select") {
                        optionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    } else emptyList()
                    
                    onConfirm(
                        FormField(
                            id = java.util.UUID.randomUUID().toString(),
                            label = label,
                            type = fieldType,
                            required = isRequired,
                            options = options
                        )
                    )
                },
                enabled = label.isNotBlank()
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
