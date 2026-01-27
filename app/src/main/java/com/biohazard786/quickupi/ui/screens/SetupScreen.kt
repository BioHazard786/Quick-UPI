package com.biohazard786.quickupi.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.R


@SuppressLint("MutableCollectionMutableState")
@Composable
fun SetupScreen(
    upiIds: List<String>,
    onSaveUpiIds: (List<String>, String) -> Unit,
) {
    // Current list of IDs being edited - Fixed bug by using toMutableStateList
    val currentUpiIds = remember(upiIds) { upiIds.toMutableStateList() }
    var newUpiInput by remember { mutableStateOf("") }
    var newPayeeNameInput by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }

    Text("Setup your UPI ID", style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(16.dp))

    // Card functionality for listing IDs
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column {
            // Header - Collapsible Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${currentUpiIds.size}/3 UPI IDs added",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    painter = if (isExpanded) painterResource(R.drawable.ic_keyboard_arrow_up) else painterResource(
                        R.drawable.ic_keyboard_arrow_down
                    ), contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            // Collapsible Content
            if (isExpanded && currentUpiIds.isNotEmpty()) {
                 Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Display added UPI IDs
                    currentUpiIds.forEachIndexed { index, id ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                id,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (currentUpiIds.size == 1) {
                                        pendingDeleteIndex = index
                                        showDeleteDialog = true
                                    } else {
                                        currentUpiIds.removeAt(index)
                                    }
                                }, modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete UPI ID?") },
            text = { Text("Are you sure you want to delete the last UPI ID?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pendingDeleteIndex != -1 && pendingDeleteIndex < currentUpiIds.size) {
                            currentUpiIds.removeAt(pendingDeleteIndex)
                        }
                        showDeleteDialog = false
                        pendingDeleteIndex = -1
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (currentUpiIds.size < 3) {
        val isUpiValid = newUpiInput.matches(Regex("^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$"))
        val isDuplicate = currentUpiIds.contains(newUpiInput)

        // Function to attempt adding ID
        val addId = {
            if (isUpiValid && !isDuplicate && newUpiInput.isNotEmpty()) {
                currentUpiIds.add(newUpiInput)
                newUpiInput = ""
            }
        }

        OutlinedTextField(
            value = newUpiInput,
            onValueChange = { newUpiInput = it },
            label = { Text("Enter UPI ID") },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_upi_pay), contentDescription = "UPI ID"
                )
            },
            trailingIcon = {
                // Save Button instead of Clear
                if (newUpiInput.isNotEmpty() && isUpiValid && !isDuplicate) {
                    IconButton(onClick = addId) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add_upi_id), // Save/Check icon
                            contentDescription = "Save ID", tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            isError = (!isUpiValid && newUpiInput.isNotEmpty()) || isDuplicate,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { addId() })
        )
        if (!isUpiValid && newUpiInput.isNotEmpty()) {
            Text(
                "Invalid UPI ID Format (name@bank)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
        if (isDuplicate) {
            Text(
                "UPI ID already added",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    } else {
        Text(
            "Maximum 3 UPI IDs allowed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Payee Name
    OutlinedTextField(
        value = newPayeeNameInput,
        onValueChange = { newPayeeNameInput = it },
        label = { Text("Name (Optional)") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_person), contentDescription = "Payee Name"
            )
        },
        trailingIcon = {
            if (newPayeeNameInput.isNotEmpty()) {
                IconButton(onClick = { newPayeeNameInput = "" }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close), contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onSaveUpiIds(currentUpiIds, newPayeeNameInput) },
        enabled = currentUpiIds.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) { Text("Save & Continue") }
}
