package com.biohazard786.quickupi.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.biohazard786.quickupi.MainActivity
import com.biohazard786.quickupi.QuickUpiTheme
import com.biohazard786.quickupi.data.UserStore
import com.biohazard786.quickupi.ui.screens.ShowQrScreen
import com.biohazard786.quickupi.utils.QRCodeGenerator

class WidgetQrActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userStore = UserStore(this)

        setContent {
            QuickUpiTheme {
                WidgetQrContent(
                    userStore = userStore,
                    onDismiss = { finish() },
                    onOpenApp = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onSetBrightness = { maxBrightness() },
                    onRestoreBrightness = { restoreBrightness() }
                )
            }
        }
    }

    private fun maxBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = 1.0f
        window.attributes = layoutParams
    }

    private fun restoreBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetQrContent(
    userStore: UserStore,
    onDismiss: () -> Unit,
    onOpenApp: () -> Unit,
    onSetBrightness: () -> Unit,
    onRestoreBrightness: () -> Unit
) {
    val upiId by userStore.upiId.collectAsState(initial = null)
    val payeeName by userStore.payeeName.collectAsState(initial = null)
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Quick UPI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (upiId.isNullOrBlank()) {
                    Text("Setup Required", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Please set up your UPI ID in the main app to use this widget.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onOpenApp, modifier = Modifier.fillMaxWidth()) {
                        Text("Open App")
                    }
                } else {
                    val uri = Uri.Builder()
                        .scheme("upi")
                        .authority("pay")
                        .appendQueryParameter("pa", upiId)
                        .appendQueryParameter("cu", "INR")
                        .appendQueryParameter("tr", "TXN_${System.currentTimeMillis()}")

                    if (!payeeName.isNullOrBlank()) {
                        uri.appendQueryParameter("pn", payeeName)
                    }
                    val bitmap = QRCodeGenerator.generateQRCode(uri.build().toString(), 512, 512)

                    ShowQrScreen(
                        amount = "",
                        qrBitmap = bitmap,
                        upiId = upiId!!,
                        payeeName = payeeName ?: "",
                        showUpiId = showUpiId,
                        showShareButton = false,
                        onQrShown = onSetBrightness,
                        onRestoreBrightness = onRestoreBrightness,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}
