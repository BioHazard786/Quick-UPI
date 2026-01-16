package com.biohazard786.quickupi.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.biohazard786.quickupi.utils.ShareUtils

@Composable
fun ShowQrScreen(
    amount: String,
    qrBitmap: Bitmap,
    upiId: String,
    payeeName: String,
    showUpiId: Boolean,
    showShareButton: Boolean = true,
    onQrShown: () -> Unit,
    onRestoreBrightness: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onQrShown()
        onDispose {
            onRestoreBrightness()
        }
    }

    Text("Show this code to receive payment")
    Spacer(modifier = Modifier.height(16.dp))

    Image(
        bitmap = qrBitmap.asImageBitmap(),
        contentDescription = "Payment QR Code",
        modifier = Modifier.size(250.dp),
        filterQuality = FilterQuality.None
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (amount.isNotBlank()) {
        Text(
            text = "₹${amount}",
            style = MaterialTheme.typography.displaySmall
        )
    } else {
        Text(
            text = "Scan to Pay", style = MaterialTheme.typography.headlineSmall
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (showUpiId) {
        Text(
            text = "UPI ID: $upiId",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (showShareButton) {
        Button(
            onClick = {
                ShareUtils.shareQrCode(
                    context,
                    qrBitmap,
                    payeeName,
                    upiId,
                    amount,
                    showUpiId
                )
            }, modifier = Modifier.fillMaxWidth()
        ) { Text("Share QR Code") }
    }

    Spacer(modifier = Modifier.height(4.dp))

    OutlinedButton(
        onClick = { onDismiss() }, modifier = Modifier.fillMaxWidth()
    ) { Text("Close") }
}
