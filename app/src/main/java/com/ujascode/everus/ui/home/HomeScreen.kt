package com.ujascode.everus.ui.home

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusBottomNavigation
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun HomeScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    var currentScreen by remember { mutableStateOf("Chats") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Screen content
        when (currentScreen) {
            "Chats" -> ChatsContent(relationship, viewModel)
            "Calls" -> CallsScreen(relationship, viewModel)
            "Memories" -> MemoriesScreen(relationship, viewModel)
            "You" -> YouScreen(relationship, viewModel)
        }

        // Bottom navigation
        EverusBottomNavigation(
            selectedItem = currentScreen,
            onItemSelected = { currentScreen = it }
        )
    }
}

@Composable
private fun ChatsContent(
    relationship: Relationship?,
    viewModel: PairingViewModel
) {
    if (relationship == null) {
        return
    }

    val messagesList by viewModel.messages.collectAsState()
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVERUS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Your private space for two",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.7f)
            )
        }

        // Connected person prominently displayed
        EverusCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Canvas(
                    modifier = Modifier.size(80.dp)
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = 40.dp.toPx()

                    drawCircle(
                        color = contentColor.copy(alpha = 0.1f),
                        center = Offset(centerX, centerY),
                        radius = radius
                    )

                    val partnerId = relationship.pairedDeviceId
                    val initial = partnerId.firstOrNull()?.uppercase() ?: "?"
                    drawIntoCanvas { canvas ->
                        val paint = Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        canvas.nativeCanvas.drawText(initial, centerX, centerY + 8, paint)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Connected",
                    fontSize = 16.sp,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = relationship.pairedDeviceId,
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Status indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier.size(8.dp)
                    ) {
                        drawCircle(
                            color = Color(0xFF34C759),
                            center = Offset(4.dp.toPx(), 4.dp.toPx()),
                            radius = 4.dp.toPx()
                        )
                    }

                    Text(
                        text = "Online",
                        fontSize = 14.sp,
                        color = contentColor
                    )
                }
            }
        }

        // Quick action buttons
        Text(
            text = "Quick Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        EverusButton(
            onClick = { },
            text = "Messages",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EverusButton(
                onClick = { },
                text = "Voice Call",
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )
            EverusButton(
                onClick = { },
                text = "Video Call",
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )
            EverusButton(
                onClick = { },
                text = "Memories",
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(
            color = contentColor.copy(alpha = 0.2f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chat History",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(messagesList.size) { index ->
                val message = messagesList.getOrNull(index) ?: return@items
                ChatListItem(
                    message = message,
                    onClick = { }
                )
            }

            if (messagesList.isEmpty()) {
                item {
                    EverusCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No messages yet",
                                fontSize = 16.sp,
                                color = contentColor.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Start a conversation with your partner",
                                fontSize = 14.sp,
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}