package com.ujascode.everus.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun NotificationsScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    var messageNotifications by remember { mutableStateOf(true) }
    var callNotifications by remember { mutableStateOf(true) }
    var mediaNotifications by remember { mutableStateOf(true) }
    var notificationSound by remember { mutableStateOf(true) }
    var vibration by remember { mutableStateOf(true) }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = "Notifications",
            showBackButton = true,
            onBackClicked = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .weight(1f)
        ) {
            Text(
                text = "Notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Control how you get notified about activity in Everus",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Message notifications
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Message notifications",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Receive notifications when you receive new messages",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = messageNotifications,
                        onCheckedChange = { messageNotifications = it }
                    )
                }
            }

            // Call notifications
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Call notifications",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Receive notifications when you receive calls",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = callNotifications,
                        onCheckedChange = { callNotifications = it }
                    )
                }
            }

            // Media notifications
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Media notifications",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Receive notifications when you receive media",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = mediaNotifications,
                        onCheckedChange = { mediaNotifications = it }
                    )
                }
            }

            // Notification sound
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification sound",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Play sound for notifications",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = notificationSound,
                        onCheckedChange = { notificationSound = it }
                    )
                }
            }

            // Vibration
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vibration",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Vibrate for notifications",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = vibration,
                        onCheckedChange = { vibration = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EverusButton(
                onClick = { },
                text = "Save Changes",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}