package com.biohazard786.quickupi.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.R

@Composable
fun SettingsScreen(
    showUpiId: Boolean, onToggleShowUpiId: (Boolean) -> Unit, onResetUpi: () -> Unit
) {
    val context = LocalContext.current
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleShowUpiId(!showUpiId) }
            .padding(vertical = 8.dp)) {
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
            })
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/BioHazard786/quick-upi") // Example URL
            )
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth()
    ) {
        Text("Github Repo")
    }

    Spacer(modifier = Modifier.height(4.dp))

    OutlinedButton(
        onClick = {
            val upiUri = Uri.Builder().scheme("upi").authority("pay")
                .appendQueryParameter("pa", context.getString(R.string.upi_id))
                .appendQueryParameter("pn", context.getString(R.string.upi_name))
                .appendQueryParameter("tn", context.getString(R.string.upi_description))
                .appendQueryParameter("cu", "INR")
                .build()

            val intent = Intent(Intent.ACTION_VIEW, upiUri)
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth()
    ) {
        Text("Support Development")
    }

    Spacer(modifier = Modifier.height(4.dp))

    OutlinedButton(
        onClick = { showResetDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) { Text("Reset UPI ID") }
}
