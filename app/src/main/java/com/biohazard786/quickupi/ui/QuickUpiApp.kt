package com.biohazard786.quickupi.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.biohazard786.quickupi.R
import com.biohazard786.quickupi.data.UserStore
import com.biohazard786.quickupi.ui.screens.EnterAmountScreen
import com.biohazard786.quickupi.ui.screens.SettingsScreen
import com.biohazard786.quickupi.ui.screens.SetupScreen
import com.biohazard786.quickupi.ui.screens.ShowQrScreen
import com.biohazard786.quickupi.utils.QRCodeGenerator
import kotlinx.coroutines.launch

sealed interface QuickUpiUiState {
    data object Setup : QuickUpiUiState
    data class EnterAmount(val upiId: String) : QuickUpiUiState
    data class ShowQr(
        val amount: String, val qrBitmap: Bitmap, val upiId: String, val payeeName: String
    ) : QuickUpiUiState
    data object Settings : QuickUpiUiState
}

@Composable
fun QuickUpiApp(
    userStore: UserStore,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // 1. STATE (useState)
    val savedUpiId by userStore.upiId.collectAsState(initial = null)
    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())
    val showUpiId by userStore.showUpiId.collectAsState(initial = true)

    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<QuickUpiUiState>(QuickUpiUiState.Setup) }

    // Sync UI state with store
    LaunchedEffect(savedUpiId) {
        uiState = if (savedUpiId.isNullOrBlank()) {
            QuickUpiUiState.Setup
        } else {
            QuickUpiUiState.EnterAmount(savedUpiId!!)
        }
    }

    QuickUpiContent(
        uiState = uiState,
        recentAmounts = recentAmounts,
        showUpiId = showUpiId,
        onSaveUpi = { upi, name ->
            scope.launch {
                userStore.saveUpiId(upi)
                userStore.savePayeeName(name)
            }
        }, onGenerateQr = { amount, note ->
            if (amount.isNotBlank()) {
                scope.launch {
                    userStore.saveRecentAmount(amount)
                }
            }

            val uriBuilder =
                Uri.Builder().scheme("upi").authority("pay").appendQueryParameter("pa", savedUpiId)
                    // Only append amount if it's not empty
                    .apply {
                        if (amount.isNotBlank()) {
                            appendQueryParameter("am", amount)
                        }
                    }.appendQueryParameter("cu", "INR")
                    .appendQueryParameter("tr", "TXN_${System.currentTimeMillis()}")

            // Optional: Payee Name
            if (!savedPayeeName.isNullOrBlank()) {
                uriBuilder.appendQueryParameter("pn", savedPayeeName)
            }

            // Optional: Transaction Note
            if (note.isNotBlank()) {
                uriBuilder.appendQueryParameter("tn", note)
            }

            val payeeURL = uriBuilder.build()

            val bitmap = QRCodeGenerator.generateQRCode(
                payeeURL.toString(), 1024, 1024
            )

            uiState = QuickUpiUiState.ShowQr(
                amount, bitmap, savedUpiId ?: "", savedPayeeName ?: ""
            )
        }, onResetUpi = {
            scope.launch {
                userStore.saveUpiId("")
                userStore.savePayeeName("")
            }
        }, onToggleShowUpiId = { show ->
            scope.launch {
                userStore.saveShowUpiId(show)
            }
        }, onSettingsClick = {
            uiState = QuickUpiUiState.Settings
        }, onBackToHome = {
            uiState = if (!savedUpiId.isNullOrBlank()) {
                QuickUpiUiState.EnterAmount(savedUpiId!!)
            } else {
                QuickUpiUiState.Setup
            }
        }, onQrShown = onQrShown, onRestoreBrightness = onRestoreBrightness, onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickUpiContent(
    uiState: QuickUpiUiState,
    recentAmounts: List<String> = emptyList(),
    showUpiId: Boolean = true,

    // Actions (events)
    onSaveUpi: (String, String) -> Unit,
    onGenerateQr: (String, String) -> Unit,
    onResetUpi: () -> Unit,
    onToggleShowUpiId: (Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onBackToHome: () -> Unit,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // Material 3 Surface (Container)
    BasicAlertDialog(
        onDismissRequest = { onDismiss() }, properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Text Header - Dynamic
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState is QuickUpiUiState.Settings) {
                        IconButton(
                            onClick = onBackToHome,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    }

                    Text(
                        text = if (uiState is QuickUpiUiState.Settings) "Settings" else "Quick UPI",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    if (uiState is QuickUpiUiState.EnterAmount) {
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_settings),
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. LOGIC
                when (uiState) {
                    QuickUpiUiState.Setup -> {
                        SetupScreen(
                            showUpiId = showUpiId,
                            onSaveUpi = onSaveUpi,
                            onToggleShowUpiId = onToggleShowUpiId
                        )
                    }
                    is QuickUpiUiState.EnterAmount -> {
                        EnterAmountScreen(
                            recentAmounts = recentAmounts,
                            onGenerateQr = onGenerateQr,
                            onResetUpi = onResetUpi
                        )
                    }
                    is QuickUpiUiState.ShowQr -> {
                        ShowQrScreen(
                            amount = uiState.amount,
                            qrBitmap = uiState.qrBitmap,
                            upiId = uiState.upiId,
                            payeeName = uiState.payeeName,
                            showUpiId = showUpiId,
                            onQrShown = onQrShown,
                            onRestoreBrightness = onRestoreBrightness,
                            onDismiss = onDismiss
                        )
                    }
                    QuickUpiUiState.Settings -> {
                        SettingsScreen(
                            showUpiId = showUpiId,
                            onToggleShowUpiId = onToggleShowUpiId,
                            onResetUpi = onResetUpi
                        )
                    }
                }
            }
        }
    }
}
