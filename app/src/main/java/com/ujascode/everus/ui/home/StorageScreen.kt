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
import kotlin.math.roundToInt

@Composable
fun StorageScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    var autoDownloadMedia by remember { mutableStateOf(true) }
    var maxStorageUsage by remember { mutableFloatStateOf(2.0f) }
    var cachedSize by remember { mutableDoubleStateOf(150.0) }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = "Storage",
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
                text = "Storage",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Manage Everus storage usage",
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
                        text = "Storage usage",
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${cachedSize.toInt()} MB used of $maxStorageUsage GB",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-download media",
                            fontSize = 16.sp,
                            color = contentColor,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Automatically download photos and videos when received",
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = autoDownloadMedia,
                        onCheckedChange = { autoDownloadMedia = it }
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
                        text = "Maximum storage usage",
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Set the maximum amount of storage Everus can use",
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$maxStorageUsage GB",
                            fontSize = 16.sp,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = maxStorageUsage,
                            onValueChange = { maxStorageUsage = it },
                            valueRange = 0.5f..10.0f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            EverusButton(
                onClick = { },
                text = "Clear Cache",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}