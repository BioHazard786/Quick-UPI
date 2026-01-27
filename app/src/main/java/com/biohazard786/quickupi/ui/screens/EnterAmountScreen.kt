package com.biohazard786.quickupi.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EnterAmountScreen(
    recentAmounts: List<String>,
    upiIds: List<String>,
    onGenerateQr: (String, String, String) -> Unit,
    onResetUpi: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    // Default to first ID
    var selectedUpiId by remember(upiIds) { mutableStateOf(upiIds.firstOrNull() ?: "") }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    // UPI ID Selection Dropdown (Only if > 1 ID)
    if (upiIds.size > 1) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            TextField(
                value = selectedUpiId,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        painter = if (expanded) painterResource(R.drawable.ic_keyboard_arrow_up) else painterResource(
                            R.drawable.ic_keyboard_arrow_down
                        ), contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_upi_pay),
                        contentDescription = "UPI ID"
                    )
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                upiIds.forEach { id ->
                    DropdownMenuItem(
                        text = {
                            Text(text = id)
                        },
                        onClick = {
                            selectedUpiId = id
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    val amountDouble = amountInput.toDoubleOrNull()
    // Amount is valid if it's empty OR if it's a valid number > 0
    val isAmountValid =
        amountInput.isEmpty() || (amountDouble != null && amountDouble > 0)
    // Error only if it's NOT empty and NOT valid (e.g. "0", "-5", "abc")
    val isAmountError = !isAmountValid && amountInput.isNotEmpty()

    OutlinedTextField(
        value = amountInput,
        onValueChange = { amountInput = it },
        label = { Text("Amount (Optional)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_currency_rupee),
                contentDescription = "Amount"
            )
        },
        trailingIcon = {
            if (amountInput.isNotEmpty()) {
                IconButton(onClick = { amountInput = "" }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear"
                    )
                }
            }
        },
        isError = isAmountError,
        singleLine = true
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Recent Amounts Chips
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        // Display the passed recentAmounts
        recentAmounts.forEach { amount ->
            SuggestionChip(
                onClick = { amountInput = amount },
                label = { Text("₹$amount") })
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = noteInput,
        onValueChange = { noteInput = it },
        label = { Text("Note (Optional)") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_note),
                contentDescription = "Note"
            )
        },
        trailingIcon = {
            if (noteInput.isNotEmpty()) {
                IconButton(onClick = { noteInput = "" }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear"
                    )
                }
            }
        },
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (isAmountValid) {
                // Use selectedUpiId if available, otherwise implicit one (handled by caller or just use empty if really broke)
                // Actually we ensure selectedUpiId is set from init.
                onGenerateQr(amountInput, noteInput, selectedUpiId)
            }
        }, enabled = isAmountValid && selectedUpiId.isNotEmpty(), modifier = Modifier.fillMaxWidth()
    ) { Text("Generate QR Code") }

    Spacer(modifier = Modifier.height(4.dp))
    OutlinedButton(
        onClick = { onResetUpi() }, modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) { Text("Reset") }
}
