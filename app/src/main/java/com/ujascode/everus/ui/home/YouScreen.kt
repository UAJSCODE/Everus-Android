package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun YouScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    var showSecurityDashboard by remember { mutableStateOf(true) }
    var showPrivacySecurityDetail by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showStorage by remember { mutableStateOf(false) }
    var showProfileEditor by remember { mutableStateOf(false) }
    var showPairingSettings by remember { mutableStateOf(false) }

    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOU",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            if (!showSecurityDashboard) {
                Spacer(modifier = Modifier.weight(1f))
                EverusButton(
                    onClick = {
                        showSecurityDashboard = true
                        showPrivacySecurityDetail = false
                        showNotifications = false
                        showStorage = false
                        showProfileEditor = false
                        showPairingSettings = false
                    },
                    text = "Back",
                    modifier = Modifier.size(60.dp, 36.dp)
                )
            }
        }

        if (showSecurityDashboard) {
            AccountSection(
                relationship = relationship,
                onProfileClick = { showProfileEditor = true; showSecurityDashboard = false },
                onPrivacyClick = { showPrivacySecurityDetail = true; showSecurityDashboard = false },
                onPairingClick = { showPairingSettings = true; showSecurityDashboard = false }
            )
            Spacer(modifier = Modifier.height(16.dp))
            PreferencesSection(
                onNotificationsClick = { showNotifications = true; showSecurityDashboard = false },
                onStorageClick = { showStorage = true; showSecurityDashboard = false }
            )
        } else if (showPrivacySecurityDetail) {
            PrivacySecurityDetailScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = { showPrivacySecurityDetail = false; showSecurityDashboard = true }
            )
        } else if (showNotifications) {
            NotificationsScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = { showNotifications = false; showSecurityDashboard = true }
            )
        } else if (showStorage) {
            StorageScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = { showStorage = false; showSecurityDashboard = true }
            )
        } else if (showProfileEditor) {
            ProfileEditorScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = { showProfileEditor = false; showSecurityDashboard = true }
            )
        } else if (showPairingSettings) {
            PairingSettingsScreen(
                relationship = relationship,
                viewModel = viewModel,
                onBack = { showPairingSettings = false; showSecurityDashboard = true }
            )
        }
    }
}

@Composable
private fun AccountSection(
    relationship: Relationship?,
    onProfileClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onPairingClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onBackground

    Text(
        text = "ACCOUNT",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    EverusCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.size(40.dp)) {
                    drawCircle(color = contentColor.copy(alpha = 0.1f))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Profile",
                        fontSize = 16.sp,
                        color = contentColor
                    )
                    Text(
                        text = relationship?.pairedDeviceId ?: "Local Device",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider(
                color = contentColor.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            EverusButton(
                onClick = onProfileClick,
                text = "Edit Profile",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            EverusButton(
                onClick = onPrivacyClick,
                text = "Privacy & Security",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            EverusButton(
                onClick = onPairingClick,
                text = "Pairing Settings",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PreferencesSection(
    onNotificationsClick: () -> Unit,
    onStorageClick: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onBackground

    Text(
        text = "PREFERENCES",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = contentColor,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    EverusCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            EverusButton(
                onClick = onNotificationsClick,
                text = "Notifications",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            EverusButton(
                onClick = onStorageClick,
                text = "Storage",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}