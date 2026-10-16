package com.ujascode.everus.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.theme.EverusTheme

@Composable
fun AboutScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // App bar
        EverusAppBar(
            title = "About EVERUS",
            showBackButton = true,
            onBackClicked = onBack
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .weight(1f)
        ) {
            // App logo/icon
            Box(
                modifier = Modifier
                    .size(80.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = 40.dp

                    drawCircle(
                        color = Color.White,
                        center = Offset(centerX, centerY),
                        radius = radius.roundToPx().toFloat()
                    )
                }
                Text(
                    text = "EVERUS",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EVERUS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )

            Text(
                text = "Version 1.0.0",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Your private space for two.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            Text(
                text = "Designed for secure, private communication between two trusted people.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            // Features
            Text(
                text = "Features",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                FeatureItem(
                    icon = { /* TODO: Add lock icon */ },
                    text = "End-to-end encrypted messaging"
                )
                FeatureItem(
                    icon = { /* TODO: Add phone icon */ },
                    text = "Voice and video calls"
                )
                FeatureItem(
                    icon = { /* TODO: Add photo icon */ },
                    text = "Private media sharing"
                )
                FeatureItem(
                    icon = { /* TODO: Add heart icon */ },
                    text = "Shared memories"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Links
            Text(
                text = "Legal & Safety",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Privacy Policy",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
                Text(
                    text = "Terms of Service",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // Icon
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}