package com.ujascode.everus.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusLogoLoader

@Composable
fun PairingScreen(
    ownDeviceId: String?,
    pairingState: PairingViewModel.UiState,
    onStartPairing: (String) -> Unit,
    onRespondToPairing: (requestId: String, accept: Boolean) -> Unit,
    onCancelPairing: () -> Unit,
    onRetryConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var partnerDeviceId by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var copied by rememberSaveable(ownDeviceId) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val trimmedPartnerId = partnerDeviceId.text.trim()
    val isSelfPairing = ownDeviceId != null && trimmedPartnerId.isNotEmpty() && trimmedPartnerId == ownDeviceId

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EVERUS",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect your devices",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Share your device ID with your partner, then send a secure pairing request.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PairingSteps(pairingState = pairingState, hasIdentity = ownDeviceId != null)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your device ID",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (ownDeviceId == null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EverusLogoLoader(
                                size = 20.dp,
                                contentDescription = "Preparing identity..."
                            )
                            Text(
                                text = "Preparing your secure identity…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            text = ownDeviceId,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.merge(
                                TextStyle(fontFamily = FontFamily.Monospace)
                            )
                        )
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(ownDeviceId))
                                copied = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (copied) "Copied" else "Copy device ID")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Partner device ID",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val primaryColor = MaterialTheme.colorScheme.primary
                val selectionColors = remember(primaryColor) {
                    TextSelectionColors(
                        handleColor = primaryColor,
                        backgroundColor = primaryColor.copy(alpha = 0.35f)
                    )
                }
                CompositionLocalProvider(LocalTextSelectionColors provides selectionColors) {
                    OutlinedTextField(
                        value = partnerDeviceId,
                        onValueChange = { partnerDeviceId = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste or enter their device ID") },
                        singleLine = true,
                        enabled = canEditPartnerId(pairingState),
                        textStyle = MaterialTheme.typography.bodyLarge.merge(
                            TextStyle(fontFamily = FontFamily.Monospace)
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Ascii,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedBorderColor = if (isSelfPairing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isSelfPairing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                if (isSelfPairing) {
                    Text(
                        text = "You cannot pair with your own device ID.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            PairingStatus(pairingState = pairingState)

            PairingActions(
                pairingState = pairingState,
                hasIdentity = ownDeviceId != null,
                hasPartnerId = trimmedPartnerId.isNotEmpty(),
                isSelfPairing = isSelfPairing,
                onStartPairing = { onStartPairing(trimmedPartnerId) },
                onRespondToPairing = onRespondToPairing,
                onCancelPairing = onCancelPairing,
                onRetryConnection = onRetryConnection
            )
        }
    }
}

@Composable
private fun PairingSteps(
    pairingState: PairingViewModel.UiState,
    hasIdentity: Boolean
) {
    val requestComplete = pairingState is PairingViewModel.UiState.RequestSent ||
        pairingState is PairingViewModel.UiState.WaitingForApproval ||
        pairingState is PairingViewModel.UiState.IncomingRequest ||
        pairingState is PairingViewModel.UiState.Responding ||
        pairingState is PairingViewModel.UiState.Paired
    val approvalActive = pairingState is PairingViewModel.UiState.WaitingForApproval ||
        pairingState is PairingViewModel.UiState.IncomingRequest ||
        pairingState is PairingViewModel.UiState.Responding
    val connected = pairingState is PairingViewModel.UiState.Paired
    val steps = listOf(
        "Identity" to hasIdentity,
        "Request" to requestComplete,
        "Approval" to approvalActive,
        "Connected" to connected
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, (label, complete) ->
            val active = when (index) {
                0 -> hasIdentity
                1 -> requestComplete
                2 -> approvalActive
                else -> connected
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = CircleShape,
                    color = if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (complete) "✓" else "${index + 1}",
                            color = if (active) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = label,
                    color = if (active) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun PairingStatus(pairingState: PairingViewModel.UiState) {
    val (message, isError, isWorking) = when (pairingState) {
        PairingViewModel.UiState.Loading ->
            Triple("Preparing your secure identity…", false, true)
        PairingViewModel.UiState.Submitting ->
            Triple("Sending your secure pairing request…", false, true)
        PairingViewModel.UiState.Responding ->
            Triple("Saving the approved pairing…", false, true)
        is PairingViewModel.UiState.RequestSent ->
            Triple("Request sent to ${pairingState.targetDeviceId}.", false, false)
        is PairingViewModel.UiState.WaitingForApproval ->
            Triple("Waiting for your partner to approve the request.", false, true)
        is PairingViewModel.UiState.IncomingRequest ->
            Triple("A device is requesting to pair with you.", false, false)
        PairingViewModel.UiState.Rejected ->
            Triple("The pairing request was declined.", true, false)
        PairingViewModel.UiState.Cancelled ->
            Triple("The pairing request was cancelled.", false, false)
        PairingViewModel.UiState.Expired ->
            Triple("The pairing request expired. You can send another request.", true, false)
        is PairingViewModel.UiState.NetworkError ->
            Triple("Network error: ${pairingState.message}", true, false)
        is PairingViewModel.UiState.Failed ->
            Triple(pairingState.message, true, false)
        is PairingViewModel.UiState.Error ->
            Triple(pairingState.message, true, false)
        PairingViewModel.UiState.Idle ->
            Triple("Your identity is ready. Pairing starts after your partner approves.", false, false)
        is PairingViewModel.UiState.Paired ->
            Triple("Your devices are securely connected.", false, false)
    }

    val foreground = if (isError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWorking) {
                EverusLogoLoader(
                    size = 18.dp,
                    contentDescription = "Working..."
                )
            }
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = foreground,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PairingActions(
    pairingState: PairingViewModel.UiState,
    hasIdentity: Boolean,
    hasPartnerId: Boolean,
    isSelfPairing: Boolean,
    onStartPairing: () -> Unit,
    onRespondToPairing: (String, Boolean) -> Unit,
    onCancelPairing: () -> Unit,
    onRetryConnection: () -> Unit
) {
    when (pairingState) {
        is PairingViewModel.UiState.IncomingRequest -> {
            Text(
                text = "Device ${pairingState.request.fromDeviceId} wants to connect. Approve only if you recognize this device.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onRespondToPairing(pairingState.request.requestId, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Decline")
                }
                Button(
                    onClick = { onRespondToPairing(pairingState.request.requestId, true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Approve")
                }
            }
        }

        is PairingViewModel.UiState.RequestSent,
        is PairingViewModel.UiState.WaitingForApproval -> {
            OutlinedButton(
                onClick = onCancelPairing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Cancel request")
            }
        }

        PairingViewModel.UiState.Loading,
        PairingViewModel.UiState.Submitting,
        PairingViewModel.UiState.Responding,
        is PairingViewModel.UiState.Paired -> {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    when (pairingState) {
                        PairingViewModel.UiState.Loading -> "Preparing…"
                        PairingViewModel.UiState.Submitting -> "Sending request…"
                        PairingViewModel.UiState.Responding -> "Saving approval…"
                        else -> "Connected"
                    }
                )
            }
        }

        is PairingViewModel.UiState.Error,
        is PairingViewModel.UiState.NetworkError,
        is PairingViewModel.UiState.Failed -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val retry = pairingState is PairingViewModel.UiState.Failed ||
                    pairingState is PairingViewModel.UiState.NetworkError
                Button(
                    onClick = onStartPairing,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasIdentity && hasPartnerId && !isSelfPairing,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(if (retry) "Try pairing again" else "Send pairing request")
                }
                OutlinedButton(
                    onClick = onRetryConnection,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Retry connection")
                }
            }
        }

        else -> {
            val retry = pairingState is PairingViewModel.UiState.Expired
            Button(
                onClick = onStartPairing,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasIdentity && hasPartnerId && !isSelfPairing,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(if (retry) "Try pairing again" else "Send pairing request")
            }
        }
    }
}

private fun canEditPartnerId(state: PairingViewModel.UiState): Boolean =
    state !is PairingViewModel.UiState.Loading &&
        state !is PairingViewModel.UiState.Submitting &&
        state !is PairingViewModel.UiState.RequestSent &&
        state !is PairingViewModel.UiState.WaitingForApproval &&
        state !is PairingViewModel.UiState.Responding &&
        state !is PairingViewModel.UiState.Paired
