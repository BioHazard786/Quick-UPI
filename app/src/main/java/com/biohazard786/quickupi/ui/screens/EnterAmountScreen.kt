package com.biohazard786.quickupi.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.R

@Composable
fun EnterAmountScreen(
    recentAmounts: List<String>,
    onGenerateQr: (String, String) -> Unit,
    onResetUpi: () -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset UPI ID?") },
            text = { Text("Are you sure you want to reset your saved UPI ID? You will need to set it up again.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetUpi()
                    }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            })
    }

    Text("Receiving Amount", style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(8.dp))

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
                onGenerateQr(amountInput, noteInput)
            }
        }, enabled = isAmountValid, modifier = Modifier.fillMaxWidth()
    ) { Text("Generate QR Code") }

    Spacer(modifier = Modifier.height(4.dp))
    OutlinedButton(
        onClick = { showResetDialog = true }, modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) { Text("Reset UPI ID") }
}
