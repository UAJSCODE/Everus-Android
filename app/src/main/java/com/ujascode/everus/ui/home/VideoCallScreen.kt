package com.ujascode.everus.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun VideoCallScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    var isMuted by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color = contentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Video Call",
                fontSize = 18.sp,
                color = contentColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EverusButton(
                onClick = { isMuted = !isMuted },
                text = if (isMuted) "Unmute" else "Mute",
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            EverusButton(
                onClick = { isFrontCamera = !isFrontCamera },
                text = "Flip",
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            EverusButton(
                onClick = { },
                text = "End Call",
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )
        }
    }
}