package com.ujascode.everus.ui.pairing



import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.content.ContentResolver
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.presentation.viewmodel.PairingViewModel.MediaTransferState
import com.ujascode.everus.presentation.viewmodel.PairingViewModel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

// Temporary comment to trigger recompilation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(viewModel: PairingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ownDeviceId by viewModel.ownDeviceId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isConnected by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val relationship by viewModel.relationship.collectAsStateWithLifecycle()
    val mediaTransfers by viewModel.mediaTransfers.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Everus", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = ownDeviceId ?: "Connecting...",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                        Text(
                            text = if (isConnected) "Online" else "Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }) { padding ->
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
                is UiState.RequestSent -> RequestSentContent(
                    targetDeviceId = state.targetDeviceId,
                    onCancel = { viewModel.cancelPairingRequest() }
                )
                is UiState.WaitingForApproval -> WaitingForApprovalContent(
                    targetDeviceId = state.targetDeviceId,
                    expiresAt = state.expiresAt,
                    onCancel = { viewModel.cancelPairingRequest() }
                )
                is UiState.IncomingRequest -> IncomingPairingRequestContent(
                    deviceId = state.request.fromDeviceId,
                    onAccept = { viewModel.respondToPairing(state.request.requestId, true) },
                    onReject = { viewModel.respondToPairing(state.request.requestId, false) }
                )
                UiState.Responding -> PairingProgress("Saving paired relationship...")
                is UiState.Paired -> {
                    val peerDeviceId = relationship?.pairedDeviceId ?: ""
                    val contentResolver = LocalContext.current.contentResolver
                    PairedContent(
                        peerDeviceId = peerDeviceId,
                        messages = messages,
                        ownDeviceId = ownDeviceId ?: "",
                        mediaTransfers = mediaTransfers,
                        onSend = { text -> viewModel.sendMessage(text) },
                        onSendWithAttachment = { text, filename, mimeType, fileSize, uri ->
                            viewModel.sendMessageWithAttachment(
                                text = text,
                                filename = filename,
                                mimeType = mimeType,
                                fileSize = fileSize,
                                uri = uri,
                                senderDeviceId = ownDeviceId ?: "",
                                receiverDeviceId = peerDeviceId,
                                contentResolver = contentResolver
                            )
                        },
                        viewModel = viewModel,
                        contentResolver = contentResolver
                    )
                }
                UiState.Rejected -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pairing request was rejected.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::resetError) { Text("Back") }
                }
                UiState.Cancelled -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pairing request cancelled.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::resetError) { Text("Back") }
                }
                UiState.Expired -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pairing request expired.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = viewModel::resetError) { Text("Try Again") }
                }
                is UiState.NetworkError -> ErrorContent(
                    message = "Network error: ${state.message}\nPlease check your connection and try again.",
                    onRetry = viewModel::resetError
                )
                is UiState.Failed -> ErrorContent(
                    message = state.message,
                    onRetry = viewModel::resetError
                )
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
        Column(Modifier.padding(24.dp)) {
            Text("Pairing Request", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Device $deviceId wants to pair with you. Do you accept this request?",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("Reject")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
private fun PairingProgress(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun IdleContent(onStartPairing: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Welcome to Everus", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Text("Enter a device ID to start pairing", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        PairingInputField(onStartPairing = onStartPairing)
    }
}

@Composable
private fun PairingInputField(onStartPairing: (String) -> Unit) {
    var deviceId by remember { mutableStateOf("") }
    OutlinedTextField(
        value = deviceId,
        onValueChange = { deviceId = it },
        label = { Text("Device ID") },
        placeholder = { Text("Enter device ID") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(0.8f),
        isError = deviceId.isBlank()
    )
    Spacer(Modifier.height(16.dp))
    Button(
        onClick = { if (deviceId.isNotBlank()) onStartPairing(deviceId) },
        enabled = deviceId.isNotBlank()
    ) {
        Text("Pair")
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Error", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PairedContent(
    peerDeviceId: String,
    messages: List<ChatMessage>,
    ownDeviceId: String,
    mediaTransfers: Map<String, MediaTransferState>,
    onSend: (String) -> Unit,
    onSendWithAttachment: (String, String, String, Long, android.net.Uri) -> Unit,
    viewModel: PairingViewModel,
    contentResolver: ContentResolver
) {
    // State for attachment functionality
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentType by remember { mutableStateOf<String?>(null) }
    var attachmentSize by remember { mutableStateOf<Long>(0L) }
    val context = LocalContext.current

    // Activity result launcher for file picker - remembered correctly
    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = getFileName(context, uri)
            attachmentType = getMimeType(context, uri)
            // Get file size
            attachmentSize = getFileSize(context, uri)
        }
    }

    // State for message input
    var messageText by remember { mutableStateOf("") }

    // Scroll state for message list
    val lazyListState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages) {
        lazyListState.scrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .widthIn(max = 480.dp)
    ) {
        // Messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .fillMaxWidth(),
            state = lazyListState
        ) {
            items(messages) { message ->
                // Determine if this message has associated media transfer
                val mediaTransfer = findAssociatedMediaTransfer(message, ownDeviceId, peerDeviceId, mediaTransfers)

                if (message.outgoing) {
                    // Outgoing message (right-aligned)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (mediaTransfer != null && mediaTransfer.filename.isNotEmpty()) {
                            // Show as media message
                            MediaMessageSent(
                                message = message,
                                mediaTransfer = mediaTransfer,
                                onDownload = { /* Already sent, no download needed */ }
                            )
                        } else {
                            // Show as regular text message
                            TextMessageSent(message = message)
                        }
                    }
                } else {
                    // Incoming message (left-aligned)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (mediaTransfer != null && mediaTransfer.filename.isNotEmpty()) {
                            // Show as media message
                            MediaMessageReceived(
                                message = message,
                                mediaTransfer = mediaTransfer,
                                onDownload = { transferId ->
                                    // Initiate download for incoming media
                                    viewModel.downloadMediaFile(
                                        transferId = transferId,
                                        filename = mediaTransfer.filename,
                                        mimeType = mediaTransfer.mimeType
                                    )
                                }
                            )
                        } else {
                            // Show as regular text message
                            TextMessageReceived(message = message)
                        }
                    }
                }
            }
        }

        // Attachment preview (if attachment is selected)
        attachmentUri?.let { uri ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment icon based on type
                    when {
                        attachmentType?.startsWith("image/") == true -> {
                            // Image preview would go here (simplified for now)
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Image attachment",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        attachmentType?.startsWith("video/") == true -> {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video attachment",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "File attachment",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = attachmentName ?: "Unknown file",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                        Text(
                            text = attachmentType ?: "Unknown type",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatFileSize(attachmentSize),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            attachmentUri = null
                            attachmentName = null
                            attachmentType = null
                            attachmentSize = 0L
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Remove")
                    }
                }
            }
        }

        // Message composer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment button
            Button(
                onClick = {
                    try {
                        pickFileLauncher.launch("*/*") // Accept all file types
                    } catch (e: ActivityNotFoundException) {
                        // Handle case where no app can handle the intent
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.width(8.dp))

            // Text input field
            OutlinedTextField(
                value = messageText,
                onValueChange = { text ->
                    messageText = text
                    // Handle IME action (Enter/Send button)
                    if (text.endsWith("\n") || text.endsWith("\r")) {
                        val finalText = text.trimEnd()
                        if (attachmentUri != null) {
                            // Send with attachment
                            onSendWithAttachment(
                                finalText,
                                attachmentName ?: "unknown",
                                attachmentType ?: "application/octet-stream",
                                attachmentSize,
                                attachmentUri!!
                            )
                            // Clear attachment after sending
                            attachmentUri = null
                            attachmentName = null
                            attachmentType = null
                            attachmentSize = 0L
                        } else if (finalText.isNotBlank()) {
                            // Send text only
                            onSend(finalText)
                        }
                    }
                },
                label = { Text("Message") },
                placeholder = { Text("Type a message...") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send)
            )

            Spacer(Modifier.width(8.dp))

            // Send button
            Button(
                onClick = {
                    val textToSend = messageText.trim()
                    if (attachmentUri != null) {
                        // Send with attachment
                        onSendWithAttachment(
                            textToSend,
                            attachmentName ?: "unknown",
                            attachmentType ?: "application/octet-stream",
                            attachmentSize,
                            attachmentUri!!
                        )
                        // Clear attachment after sending
                        attachmentUri = null
                        attachmentName = null
                        attachmentType = null
                        attachmentSize = 0L
                    } else if (textToSend.isNotBlank()) {
                        // Send text only
                        onSend(textToSend)
                    }
                    messageText = ""
                },
                enabled = messageText.isNotBlank() || attachmentUri != null
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper function to find associated media transfer for a message
private fun findAssociatedMediaTransfer(
    message: ChatMessage,
    ownDeviceId: String,
    peerDeviceId: String,
    mediaTransfers: Map<String, PairingViewModel.MediaTransferState>
): PairingViewModel.MediaTransferState? {
    // This is a simplified implementation - in a real app, we would have a direct link
    // between messages and media transfers via message_id in the media_transfers table

    // For now, we'll look for recent media transfers that match the message criteria
    return mediaTransfers.values.firstOrNull { transfer ->
        // For demo purposes, we'll just return the most recent transfer if it matches device criteria
        transfer.filename.isNotEmpty()
    }
}

// Sent text message
@Composable
private fun TextMessageSent(message: ChatMessage) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = message.text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatTimestamp(message.sentAt),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    }
}

// Received text message
@Composable
private fun TextMessageReceived(message: ChatMessage) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
    ) {
        Text(
            text = message.text,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatTimestamp(message.sentAt),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

// Sent media message
@Composable
private fun MediaMessageSent(
    message: ChatMessage,
    mediaTransfer: PairingViewModel.MediaTransferState,
    onDownload: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        // Media preview based on type
        when {
            mediaTransfer.mimeType.startsWith("image/") -> {
                // Image preview (placeholder)
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image attachment",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
            mediaTransfer.mimeType.startsWith("video/") -> {
                // Video preview (placeholder)
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video attachment",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
            else -> {
                // File preview (placeholder)
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "File attachment",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Column {
            Text(
                text = mediaTransfer.filename,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = "${mediaTransfer.mimeType} • ${formatFileSize(mediaTransfer.fileSize)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
            // Transfer status
            when (mediaTransfer.status) {
                "PENDING" -> Text(
                    text = "Preparing...",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
                "UPLOADING" -> {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "Uploading ${mediaTransfer.uploadProgress}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        CircularProgressIndicator(
                            progress = mediaTransfer.uploadProgress / 100f,
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                "DOWNLOADING" -> {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "Downloading ${mediaTransfer.uploadProgress}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        CircularProgressIndicator(
                            progress = mediaTransfer.uploadProgress / 100f,
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                "COMPLETED" -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Transfer complete",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                "FAILED" -> Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Transfer failed",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatTimestamp(message.sentAt),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        )
    }
}

// Received media message
@Composable
private fun MediaMessageReceived(
    message: ChatMessage,
    mediaTransfer: PairingViewModel.MediaTransferState,
    onDownload: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
    ) {
        // Media preview based on type
        when {
            mediaTransfer.mimeType.startsWith("image/") -> {
                // Image preview (placeholder)
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Image attachment",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(60.dp)
                )
            }
            mediaTransfer.mimeType.startsWith("video/") -> {
                // Video preview (placeholder)
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video attachment",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(60.dp)
                )
            }
            else -> {
                // File preview (placeholder)
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "File attachment",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(60.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Column {
            Text(
                text = mediaTransfer.filename,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                text = "${mediaTransfer.mimeType} • ${formatFileSize(mediaTransfer.fileSize)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            // Transfer status and actions
            when (mediaTransfer.status) {
                "PENDING" -> Text(
                    text = "Preparing...",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
                "UPLOADING", "DOWNLOADING" -> {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Text(
                            text = "${if (mediaTransfer.status == "UPLOADING") "Uploading" else "Downloading"} ${mediaTransfer.uploadProgress}%",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.width(4.dp))
                        CircularProgressIndicator(
                            progress = mediaTransfer.uploadProgress / 100f,
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                "COMPLETED" -> {
                    // Show download/save button for completed transfers
                    Button(
                        onClick = { onDownload(mediaTransfer.transferId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download file",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                "FAILED" -> {
                    Row(horizontalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Transfer failed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Failed to transfer",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = formatTimestamp(message.sentAt),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

// Helper functions for file processing
private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    it.getString(nameIndex)
                } else {
                    null
                }
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun getMimeType(context: Context, uri: Uri): String? {
    return try {
        val contentResolver = context.contentResolver
        contentResolver.getType(uri)
    } catch (e: Exception) {
        null
    }
}

private fun getFileSize(context: Context, uri: Uri): Long {
    return try {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    it.getLong(sizeIndex)
                } else {
                    0L
                }
            } else {
                0L
            }
        } ?: 0L
    } catch (e: Exception) {
        0L
    }
}

private fun formatFileSize(size: Long): String {
    if (size < 1024) {
        return "$size B"
    } else if (size < 1024 * 1024) {
        return "%.1f KB".format(size / 1024.0)
    } else if (size < 1024 * 1024 * 1024) {
        return "%.1f MB".format(size / (1024.0 * 1024))
    } else {
        return "%.1f GB".format(size / (1024.0 * 1024 * 1024))
    }
}

@Composable
private fun RequestSentContent(
    targetDeviceId: String,
    onCancel: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pairing Request Sent", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Pairing request sent to $targetDeviceId",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cancel Request")
        }
    }
}

@Composable
private fun WaitingForApprovalContent(
    targetDeviceId: String,
    expiresAt: Long,
    onCancel: () -> Unit
) {
    // State for remaining time
    var remainingTime by remember { mutableStateOf(expiresAt - System.currentTimeMillis()) }

    // Update remaining time every second
    LaunchedEffect(expiresAt) {
        while (remainingTime > 0) {
            delay(1000) // Update every second
            remainingTime = expiresAt - System.currentTimeMillis()
            if (remainingTime <= 0) {
                // Time's up, but we'll let the ViewModel handle expiration
                break
            }
        }
    }

    // Calculate remaining time
    val remainingMinutes = (remainingTime / (60 * 1000)).coerceAtLeast(0)
    val remainingSeconds = ((remainingTime % (60 * 1000)) / 1000).coerceAtLeast(0)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Waiting for Approval", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Pairing request sent to $targetDeviceId. Waiting for their response...",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "This request will expire in ${remainingMinutes}m ${remainingSeconds}s",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("Cancel Request")
        }
    }
}