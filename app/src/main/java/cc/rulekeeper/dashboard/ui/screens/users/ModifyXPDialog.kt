package cc.rulekeeper.dashboard.ui.screens.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ModifyXPDialog(
    currentXP: Int,
    currentLevel: Int,
    onConfirm: (amount: Int, operation: String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var operation by remember { mutableStateOf("add") } // add, remove, set
    
    val newXP = when (operation) {
        "add" -> currentXP + (amount.toIntOrNull() ?: 0)
        "remove" -> (currentXP - (amount.toIntOrNull() ?: 0)).coerceAtLeast(0)
        "set" -> amount.toIntOrNull() ?: currentXP
        else -> currentXP
    }
    
    // Rough XP to level calculation (adjust based on your leveling formula)
    val newLevel = kotlin.math.sqrt(newXP / 100.0).toInt()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Modify XP")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current stats
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Current",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$currentXP XP",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Level $currentLevel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "New",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$newXP XP",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Level $newLevel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Operation selection
                Text(
                    "Operation",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = operation == "add",
                        onClick = { operation = "add" },
                        label = { Text("Add") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = operation == "remove",
                        onClick = { operation = "remove" },
                        label = { Text("Remove") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = operation == "set",
                        onClick = { operation = "set" },
                        label = { Text("Set") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() } },
                    label = { Text(if (operation == "set") "New XP" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    if (amt > 0 || operation == "set") {
                        onConfirm(amt, operation)
                    }
                },
                enabled = amount.toIntOrNull() != null
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
