package com.ujascode.everus.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun PairingSettingsScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = "Pairing Settings",
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
                text = "Your Pairing Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Manage your pairing connections and settings.",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Connection",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Connected to Partner",
                        fontSize = 14.sp,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Last updated: Just now",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EverusButton(
                onClick = { },
                text = "Repair Connection",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            EverusButton(
                onClick = { },
                text = "Unpair Device",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}