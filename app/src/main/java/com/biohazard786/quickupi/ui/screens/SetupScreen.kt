package com.biohazard786.quickupi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.R

@Composable
fun SetupScreen(
    showUpiId: Boolean,
    onSaveUpi: (String, String) -> Unit,
    onToggleShowUpiId: (Boolean) -> Unit
) {
    var newUpiInput by remember { mutableStateOf("") }
    var newPayeeNameInput by remember { mutableStateOf("") }

    Text("Setup your UPI ID", style = MaterialTheme.typography.bodyLarge)
    Spacer(modifier = Modifier.height(8.dp))

    val isUpiValid = newUpiInput.matches(Regex("^[a-zA-Z0-9.\\-_]+@[a-zA-Z]+$"))
    val isUpiError = !isUpiValid && newUpiInput.isNotEmpty()

    OutlinedTextField(
        value = newUpiInput,
        onValueChange = { newUpiInput = it },
        label = { Text("e.g. name@bank") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_upi_pay),
                contentDescription = "UPI ID"
            )
        },
        trailingIcon = {
            if (newUpiInput.isNotEmpty()) {
                IconButton(onClick = { newUpiInput = "" }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Clear"
                    )
                }
            }
        },
        isError = isUpiError,
        singleLine = true
    )
    if (isUpiError) {
        Text(
            "Invalid UPI ID Format (name@bank)",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = newPayeeNameInput,
        onValueChange = { newPayeeNameInput = it },
        label = { Text("Name (Optional)") },
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = "Payee Name"
            )
        },
        trailingIcon = {
            if (newPayeeNameInput.isNotEmpty()) {
                IconButton(onClick = { newPayeeNameInput = "" }) {
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleShowUpiId(!showUpiId) }
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Show UPI ID in QR Screen",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = showUpiId,
            onCheckedChange = { onToggleShowUpiId(it) },
            thumbContent = if (showUpiId) {
                {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            } else {
                null
            }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = { onSaveUpi(newUpiInput, newPayeeNameInput) },
        enabled = isUpiValid,
        modifier = Modifier.fillMaxWidth()
    ) { Text("Save & Continue") }

}
