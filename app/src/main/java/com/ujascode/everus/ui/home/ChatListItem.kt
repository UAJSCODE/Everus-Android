package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.data.model.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
private val zoneId = ZoneId.systemDefault()
private val timestampFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@Composable
fun ChatListItem(
    message: ChatMessage?,
    onClick: () -> Unit
) {
    if (message == null) {
        return
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sender avatar (for incoming messages) or checkmark (for outgoing)
        if (!message.outgoing) {
            // Incoming message avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = contentColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = "P",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            // Outgoing message checkmark
            Canvas(
                modifier = Modifier.size(40.dp)
            ) {
                drawCircle(
                    color = contentColor.copy(alpha = 0.1f),
                    center = Offset(20.dp.toPx(), 20.dp.toPx()),
                    radius = 18.dp.toPx()
                )

                                // Draw checkmark using lines instead of Path for better performance
                val startPoint = Offset(12.dp.toPx(), 20.dp.toPx())
                val midPoint = Offset(16.dp.toPx(), 24.dp.toPx())
                val endPoint = Offset(22.dp.toPx(), 16.dp.toPx())
                drawLine(
                    start = startPoint,
                    end = midPoint,
                    color = Color(0xFF34C759),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    start = midPoint,
                    end = endPoint,
                    color = Color(0xFF34C759),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Message preview
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (message.outgoing) "You: " else "",
                    fontSize = 14.sp,
                    color = if (message.outgoing) contentColor.copy(alpha = 0.8f) else contentColor
                )
                Text(
                    text = message.body.take(30),
                    fontSize = 14.sp,
                    color = if (message.outgoing) contentColor.copy(alpha = 0.8f) else contentColor
                )
            }

            // Timestamp and status
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatTimestamp(message.sentAt),
                    fontSize = 12.sp,
                    color = contentColor.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (message.outgoing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Canvas(
                            modifier = Modifier.size(12.dp)
                        ) {
                            // Draw checkmark using lines instead of Path for better performance
                            val startPoint = Offset(3.dp.toPx(), 8.dp.toPx())
                            val midPoint = Offset(6.dp.toPx(), 12.dp.toPx())
                            val endPoint = Offset(10.dp.toPx(), 6.dp.toPx())
                            drawLine(
                                start = startPoint,
                                end = midPoint,
                                color = contentColor,
                                strokeWidth = 1.5f.dp.toPx()
                            )
                            drawLine(
                                start = midPoint,
                                end = endPoint,
                                color = contentColor,
                                strokeWidth = 1.5f.dp.toPx()
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val javaTime = Instant.ofEpochMilli(timestamp)
    val javaTimeZoned = javaTime.atZone(zoneId)
    val time = javaTimeZoned.toLocalTime()
    return timestampFormatter.format(time)
}
