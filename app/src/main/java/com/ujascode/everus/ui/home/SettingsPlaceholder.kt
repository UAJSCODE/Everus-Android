package com.ujascode.everus.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun SettingsPlaceholder(
    title: String,
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = title,
            showBackButton = true,
            onBackClicked = onBack
        )

        when (title) {
            "Notifications" -> NotificationsPlaceholder()
            "Appearance" -> AppearancePlaceholder()
            "Storage" -> StoragePlaceholder()
            "Devices" -> DevicesScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = onBack
            )
            else -> GenericSettingsPlaceholder(title = title)
        }
    }
}

@Composable
private fun NotificationsPlaceholder() {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Notification settings will be available in a future update.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AppearancePlaceholder() {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Appearance",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Appearance settings will be available in a future update.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun StoragePlaceholder() {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Storage",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Storage settings will be available in a future update.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Storage Usage",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Used",
                    fontSize = 16.sp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "2.1 GB",
                    fontSize = 16.sp,
                    color = contentColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        color = contentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(8.dp)
                        .background(
                            color = Color(0xFF00C7B4),
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }

            Text(
                text = "Free space: 5.8 GB",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        EverusButton(
            onClick = { },
            text = "Clear Cache",
            modifier = Modifier
                .width(200.dp)
                .height(40.dp)
        )
    }
}

@Composable
private fun GenericSettingsPlaceholder(title: String) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Settings for $title will be available in a future update.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f)
        )
    }
}