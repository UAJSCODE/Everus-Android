package com.ujascode.everus.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard
import com.ujascode.everus.ui.home.CallLogEntry
import com.ujascode.everus.ui.theme.EverusTheme

@Composable
fun CallsScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        // Should not happen, but fallback to home
        return
    }

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
                text = "CALLS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Quick call buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EverusButton(
                onClick = { /* TODO: Start Voice Call */ },
                text = "Voice Call",
                icon = { /* TODO: Add phone icon */ },
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp)
            )
            EverusButton(
                onClick = { /* TODO: Start Video Call */ },
                text = "Video Call",
                icon = { /* TODO: Add video icon */ },
                modifier = Modifier
                    .width(120.dp)
                    .height(48.dp)
            )
        }

        // Recent calls
        Text(
            text = "Recent",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // For now, show placeholder items
            items(3) { index ->
                CallLogEntry(
                    callerName = "Partner",
                    callType = when (index) {
                        0 -> "Missed"
                        1 -> "Outgoing"
                        2 -> "Incoming"
                        else -> "Unknown"
                    },
                    timestamp = when (index) {
                        0 -> "Yesterday, 2:30 PM"
                        1 -> "Today, 10:15 AM"
                        2 -> "Today, 9:45 AM"
                        else -> "Unknown"
                    },
                    duration = when (index) {
                        0 -> null // No duration for missed calls
                        1 -> "5:24"
                        2 -> "12:03"
                        else -> null
                    },
                    onClick = { /* TODO: Handle call log click */ }
                )
            }
        }

        // If no calls, show empty state
        /*
        if (true /* TODO: Check if call history is empty */) {
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp)
                ) {
                    Text(
                        text = "No call history",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }
        */
    }
}