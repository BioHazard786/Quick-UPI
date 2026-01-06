package com.biohazard786.quickupi.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.biohazard786.quickupi.R
import com.biohazard786.quickupi.data.UserStore
import com.biohazard786.quickupi.utils.QRCodeGenerator
import kotlinx.coroutines.launch

sealed interface QuickUpiUiState {
    data object Setup : QuickUpiUiState
    data class EnterAmount(val upiId: String) : QuickUpiUiState
    data class ShowQr(val amount: String, val qrBitmap: Bitmap) : QuickUpiUiState
}

@Composable
fun QuickUpiApp(
    userStore: UserStore,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // 1. STATE (useState)
    // Collecting flow is like subscribing to a store
    val savedUpiId by userStore.upiId.collectAsState(initial = null)
    val savedPayeeName by userStore.payeeName.collectAsState(initial = null)
    val recentAmounts by userStore.recentAmounts.collectAsState(initial = emptyList())

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
        onSaveUpi = { upi, name ->
            scope.launch {
                userStore.saveUpiId(upi)
                userStore.savePayeeName(name)
            }
        },
        onGenerateQr = { amount, note ->
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

            uiState = QuickUpiUiState.ShowQr(amount, bitmap)
        },
        onResetUpi = {
            scope.launch {
                userStore.saveUpiId("")
                userStore.savePayeeName("")
            }
        },
        onDone = { uiState = QuickUpiUiState.EnterAmount(savedUpiId!!) },
        onQrShown = onQrShown,
        onRestoreBrightness = onRestoreBrightness,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickUpiContent(
    uiState: QuickUpiUiState,
    recentAmounts: List<String> = emptyList(),

    // Actions (events)
    onSaveUpi: (String, String) -> Unit,
    onGenerateQr: (String, String) -> Unit,
    onResetUpi: () -> Unit,
    onDone: () -> Unit,
    onQrShown: () -> Unit = {},
    onRestoreBrightness: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // Local UI state
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var newUpiInput by remember { mutableStateOf("") }
    var newPayeeNameInput by remember { mutableStateOf("") }

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

                // Text Header
                Text(
                    text = "Quick UPI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 2. LOGIC
                when (uiState) {

                    // VIEW 1: Setup Screen (No UPI ID saved)
                    QuickUpiUiState.Setup -> {
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

                        Button(
                            onClick = { onSaveUpi(newUpiInput, newPayeeNameInput) },
                            enabled = isUpiValid
                        ) { Text("Save & Continue") }
                    }

                    // VIEW 2: Enter Amount
                    is QuickUpiUiState.EnterAmount -> {
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
                            label = { Text("Amount") },
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
                            }, enabled = isAmountValid
                        ) { Text("Generate QR Code") }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { onResetUpi() }) { Text("Reset UPI ID") }
                    }

                    // VIEW 3: QR Code Display
                    is QuickUpiUiState.ShowQr -> {
                        DisposableEffect(Unit) {
                            onQrShown()
                            onDispose {
                                onRestoreBrightness()
                            }
                        }

                        Text("Show this code to receive payment")
                        Spacer(modifier = Modifier.height(16.dp))

                        Image(
                            bitmap = uiState.qrBitmap.asImageBitmap(),
                            contentDescription = "Payment QR Code",
                            modifier = Modifier.size(250.dp),
                            filterQuality = FilterQuality.None
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.amount.isNotBlank()) {
                            Text(
                                text = "₹${uiState.amount}",
                                style = MaterialTheme.typography.displaySmall
                            )
                        } else {
                            Text(
                                text = "Scan to Pay", style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDismiss() }, modifier = Modifier.weight(1f)
                            ) { Text("Close") }

                            Button(onClick = { onDone() }, modifier = Modifier.weight(1f)) {
                                Text("New Pay")
                            }
                        }
                    }
                }
            }
        }
    }
}
