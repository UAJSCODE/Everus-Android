package com.ujascode.everus.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard
import com.ujascode.everus.ui.components.EverusInputField
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ConversationScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var messageValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var isVoiceRecording by remember { mutableStateOf(false) }
    var isVoiceRecordingPaused by remember { mutableStateOf(false) }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var attachmentType by remember { mutableStateOf<String?>(null) }
    var attachmentSize by remember { mutableStateOf<Long>(0L) }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            attachmentUri = uri
            attachmentName = getFileName(context, uri)
            attachmentType = getMimeType(context, uri)
            attachmentSize = getFileSize(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        EverusAppBar(
            title = "Conversation",
            showBackButton = true,
            onBackClicked = { }
        )

        val messagesList by viewModel.messages.collectAsState()

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            items(messagesList) { message ->
                if (message.outgoing) {
                    OutgoingMessage(message = message)
                } else {
                    IncomingMessage(message = message)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EverusButton(
                onClick = {
                    try {
                        pickFileLauncher.launch("*/*")
                    } catch (e: ActivityNotFoundException) {
                        // Handle error
                    }
                },
                icon = {
                    val lineColor = Color.White
                    val path = Path().apply {
                        moveTo(4.dp.toPx(), 4.dp.toPx())
                        lineTo(20.dp.toPx(), 20.dp.toPx())
                    }
                    drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
                },
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isVoiceRecording) {
                VoiceRecorder(
                    isRecording = true,
                    onCancel = {
                        isVoiceRecording = false
                        isVoiceRecordingPaused = false
                    },
                    onSend = {
                        isVoiceRecording = false
                        isVoiceRecordingPaused = false
                    },
                    onPauseResume = { isVoiceRecordingPaused = !isVoiceRecordingPaused }
                )
            } else {
                EverusInputField(
                    value = messageValue,
                    onValueChange = { messageValue = it },
                    label = "",
                    placeholder = "Message",
                    leadingIcon = {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            drawCircle(
                                color = Color.White,
                                radius = 8.dp.toPx(),
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    },
                    trailingIcon = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            EverusButton(
                                onClick = { isVoiceRecording = true },
                                icon = {
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            )

                            EverusButton(
                                onClick = {
                                    if (attachmentUri != null) {
                                        viewModel.sendMessageWithAttachment(
                                            text = messageValue.text,
                                            filename = attachmentName ?: "unknown",
                                            mimeType = attachmentType ?: "application/octet-stream",
                                            fileSize = attachmentSize,
                                            uri = attachmentUri!!,
                                            senderDeviceId = "",
                                            receiverDeviceId = relationship.pairedDeviceId,
                                            contentResolver = context.contentResolver
                                        )
                                        attachmentUri = null
                                        attachmentName = null
                                        attachmentType = null
                                        attachmentSize = 0L
                                    } else if (messageValue.text.isNotBlank()) {
                                        viewModel.sendMessage(messageValue.text)
                                    }
                                    messageValue = TextFieldValue()
                                },
                                icon = {
                                    val sendPath = Path().apply {
                                        moveTo(4.dp.toPx(), 12.dp.toPx())
                                        lineTo(20.dp.toPx(), 12.dp.toPx())
                                        lineTo(16.dp.toPx(), 8.dp.toPx())
                                        moveTo(20.dp.toPx(), 12.dp.toPx())
                                        lineTo(16.dp.toPx(), 16.dp.toPx())
                                    }
                                    drawPath(sendPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))
                                },
                                modifier = Modifier.size(32.dp),
                                enabled = messageValue.text.isNotBlank() || attachmentUri != null
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OutgoingMessage(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        EverusCard(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.body,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = formatTimestamp(message.sentAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun IncomingMessage(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        EverusCard(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = message.body,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = formatTimestamp(message.sentAt),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val javaTime = Instant.ofEpochMilli(timestamp)
    val javaTimeZoned = javaTime.atZone(ZoneId.systemDefault())
    val time = javaTimeZoned.toLocalTime()
    return DateTimeFormatter.ofPattern("hh:mm a").format(time)
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "file"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}

private fun getMimeType(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri) ?: "application/octet-stream"
}

private fun getFileSize(context: Context, uri: Uri): Long {
    var size = 0L
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                size = it.getLong(sizeIndex)
            }
        }
    }
    return size
}
