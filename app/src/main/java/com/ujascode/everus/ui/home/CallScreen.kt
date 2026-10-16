package com.ujascode.everus.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.theme.EverusTheme

@Composable
fun CallScreen(
    relationship: Relationship?,
    isVideoCall: Boolean = false,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        // Should not happen, but fallback to home
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Status bar (optional, for full screen calls)
        /*
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Text(
                text = "2:30 PM",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            // Battery, signal, etc.
        }
        */

        // Caller info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                // Partner initial
                val partnerId = relationship.pairedDeviceId
                val initial = partnerId.take(1).uppercase()
                if (initial.isEmpty()) "?" else initial

                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            Text(
                text = relationship.pairedDeviceId,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )

            Text(
                text = if (isVideoCall) "Video Call" else "Voice Call",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Connection status
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Connected",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Call duration placeholder
            Text(
                text = "00:00",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Call controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Mute button placeholder
            EverusButton(
                onClick = { /* TODO: Toggle mute */ },
                icon = { /* TODO: Add mute icon */ },
                modifier = Modifier
                    .size(48.dp)
            )

            // Speaker button placeholder
            EverusButton(
                onClick = { /* TODO: Toggle speaker */ },
                icon = { /* TODO: Add speaker icon */ },
                modifier = Modifier
                    .size(48.dp)
            )

            // Video toggle (only for video calls)
            if (isVideoCall) {
                EverusButton(
                    onClick = { /* TODO: Toggle video */ },
                    icon = { /* TODO: Add video camera icon */ },
                    modifier = Modifier
                        .size(48.dp)
                )
            }

            // End call button
            EverusButton(
                onClick = { /* TODO: End call */ },
                text = "End Call",
                modifier = Modifier
                    .width(80.dp)
                    .height(48.dp)
            )
        }
    }
}