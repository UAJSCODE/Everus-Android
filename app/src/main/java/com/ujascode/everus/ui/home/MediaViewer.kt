package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun MediaViewer(
    mediaUrl: String,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EverusButton(
                onClick = onClose,
                text = "Close",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Media",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.weight(1f))
            EverusButton(
                onClick = onShare,
                text = "Share",
                modifier = Modifier.size(40.dp)
            )
        }

        HorizontalDivider(
            color = contentColor.copy(alpha = 0.2f),
            thickness = 1.dp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color = contentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            when (mediaUrl.lowercase()) {
                in listOf("jpg", "jpeg", "png", "gif") -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            drawRect(
                                color = contentColor.copy(alpha = 0.1f),
                                topLeft = Offset.Zero,
                                size = size
                            )
                        }
                        Text(
                            text = "Image Preview",
                            fontSize = 16.sp,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                in listOf("mp4", "mov", "avi") -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            drawRect(
                                color = contentColor.copy(alpha = 0.1f),
                                topLeft = Offset.Zero,
                                size = size
                            )
                            val playPath = Path().apply {
                                moveTo(size.width / 2 - 12.dp.toPx(), size.height / 2 - 12.dp.toPx())
                                lineTo(size.width / 2 - 12.dp.toPx(), size.height / 2 + 12.dp.toPx())
                                lineTo(size.width / 2 + 12.dp.toPx(), size.height / 2)
                                close()
                            }
                            drawPath(playPath, color = contentColor)
                        }
                        Text(
                            text = "Video Preview",
                            fontSize = 16.sp,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            drawRect(
                                color = contentColor.copy(alpha = 0.1f),
                                topLeft = Offset.Zero,
                                size = size
                            )
                        }
                        Text(
                            text = "File Preview",
                            fontSize = 16.sp,
                            color = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EverusButton(
                onClick = { },
                text = "Download",
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            EverusButton(
                onClick = { },
                text = "Share",
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp)
            )
        }
    }
}