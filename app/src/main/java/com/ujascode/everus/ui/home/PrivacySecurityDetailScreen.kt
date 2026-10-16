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
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun PrivacySecurityDetailScreen(
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
            title = "Privacy & Security",
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
                text = "Your conversation is secure",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
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
                        text = "Encryption",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "All messages and calls are encrypted using the Signal Protocol, which provides end-to-end encryption.",
                        fontSize = 14.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Identity Verification",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Your identity is verified through cryptographic key exchange during the pairing process.",
                        fontSize = 14.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Device Security",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Encryption keys are stored securely on your device using Android's Keystore system.",
                        fontSize = 14.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}