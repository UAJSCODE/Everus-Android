package com.ujascode.everus.ui.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.presentation.viewmodel.PairingViewModel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(viewModel: PairingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ownDeviceId by viewModel.ownDeviceId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Everus Pairing") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .widthIn(max = 480.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ownDeviceId?.let {
                OwnDeviceIdContent(it)
                Spacer(Modifier.height(24.dp))
            }
            when (val state = uiState) {
                UiState.Loading -> PairingProgress("Preparing secure identity...")
                UiState.Idle -> IdleContent(viewModel::startPairing)
                is UiState.Pairing -> PairingProgress("Sending pairing request...")
                is UiState.WaitingForApproval -> Text(
                    "Pairing request sent to ${state.targetDeviceId}. Waiting for approval.",
                    style = MaterialTheme.typography.titleMedium
                )
                is UiState.IncomingRequest -> IncomingPairingRequestContent(
                    deviceId = state.request.fromDeviceId,
                    onAccept = { viewModel.respondToPairing(state.request.requestId, true) },
                    onReject = { viewModel.respondToPairing(state.request.requestId, false) }
                )
                UiState.Responding -> PairingProgress("Saving paired relationship...")
                is UiState.Paired -> PairedContent(
                    peerDeviceId = state.relationship.pairedDeviceId,
                    messages = messages,
                    onSend = viewModel::sendMessage
                )
                UiState.Rejected -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pairing request was rejected.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::resetError) { Text("Back") }
                }
                UiState.Expired -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pairing request expired.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::resetError) { Text("Back") }
                }
                is UiState.Error -> ErrorContent(state.message, viewModel::resetError)
            }
        }
    }
}

@Composable
private fun OwnDeviceIdContent(deviceId: String) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember(deviceId) { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Your Device ID", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(deviceId, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    clipboardManager.setText(AnnotatedString(deviceId))
                    copied = true
                }) {
                    Text(if (copied) "Copied" else "Copy")
                }
            }
        }
    }
}

@Composable
private fun IncomingPairingRequestContent(
    deviceId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Pairing Request", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text("Device $deviceId wants to pair with you.", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
            ) {
                OutlinedButton(onClick = onReject) { Text("Reject") }
                Button(onClick = onAccept) { Text("Accept") }
            }
        }
    }
}

@Composable
private fun PairedContent(
    peerDeviceId: String,
    messages: List<ChatMessage>,
    onSend: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    Column(Modifier.fillMaxWidth()) {
        Text("PAIRED", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text("Secure conversation with $peerDeviceId", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        if (messages.isEmpty()) {
            Text("No messages yet.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.messageId }) { message ->
                    Surface(
                        color = if (message.outgoing) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(message.text, style = MaterialTheme.typography.bodyLarge)
                            Text(if (message.outgoing) "You" else "Peer", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        val focusManager = LocalFocusManager.current
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text("Encrypted message") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (draft.isNotBlank()) {
                    onSend(draft)
                    draft = ""
                    focusManager.clearFocus()
                }
            })
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                onSend(draft)
                draft = ""
                focusManager.clearFocus()
            },
            enabled = draft.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Send")
        }
    }
}

@Composable
private fun IdleContent(onStartPairing: (String) -> Unit) {
    var targetDeviceId by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Enter Target Device ID", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = targetDeviceId,
            onValueChange = { targetDeviceId = it },
            label = { Text("Target Device ID") },
            placeholder = { Text("Enter device ID") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (targetDeviceId.isNotBlank()) onStartPairing(targetDeviceId)
            }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onStartPairing(targetDeviceId) },
            enabled = targetDeviceId.isNotBlank()
        ) {
            Text("Pair")
        }
    }
}

@Composable
private fun PairingProgress(message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(modifier = Modifier.size(50.dp))
        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("!", color = Color.Red, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.titleLarge, color = Color.Red)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
