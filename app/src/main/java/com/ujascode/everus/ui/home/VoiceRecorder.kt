package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.ui.components.EverusButton
import kotlinx.coroutines.delay

@Composable
fun VoiceRecorder(
    isRecording: Boolean,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    onPauseResume: () -> Unit
) {
    var recordingTimeSeconds by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (true) {
                delay(1000L)
                recordingTimeSeconds++
            }
        }
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier.size(20.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFFFF3B30),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = 8.dp.toPx()
                        )
                    }

                    Text(
                        text = if (isPaused) "Paused" else "Recording...",
                        fontSize = 16.sp,
                        color = contentColor
                    )

                    Text(
                        text = String.format("%02d:%02d", recordingTimeSeconds / 60, recordingTimeSeconds % 60),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            } else {
                Text(
                    text = "Touch to record a voice message",
                    fontSize = 16.sp,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EverusButton(
                onClick = {
                    recordingTimeSeconds = 0
                    isPaused = false
                    onCancel()
                },
                text = "Cancel",
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )

            if (isRecording) {
                EverusButton(
                    onClick = {
                        isPaused = !isPaused
                        onPauseResume()
                    },
                    text = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                )
            }

            EverusButton(
                onClick = {
                    recordingTimeSeconds = 0
                    isPaused = false
                    onSend()
                },
                text = "Send",
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )
        }
    }
}