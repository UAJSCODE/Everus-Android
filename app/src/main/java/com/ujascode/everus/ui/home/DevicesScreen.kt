package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusAppBar
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard
import com.ujascode.everus.ui.components.EverusInputField

@Composable
fun DevicesScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    var showRenameThisDeviceDialog by remember { mutableStateOf(false) }
    var showRenamePartnerDeviceDialog by remember { mutableStateOf(false) }
    var thisDeviceName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("This Device"))
    }
    var partnerDeviceName by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue("Partner's Device"))
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        EverusAppBar(
            title = "Devices",
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
                text = "Your Devices",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Currently connected devices",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Current device
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
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(
                            modifier = Modifier.size(24.dp)
                        ) {
                            drawCircle(color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = thisDeviceName.text,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active • Last seen just now",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        EverusButton(
                            onClick = { showRenameThisDeviceDialog = true },
                            text = "Rename",
                            modifier = Modifier
                                .width(80.dp)
                                .height(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        EverusButton(
                            onClick = { },
                            text = "Remove",
                            modifier = Modifier
                                .width(80.dp)
                                .height(32.dp)
                        )
                    }
                }
            }

            // Paired device
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
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Canvas(
                            modifier = Modifier.size(24.dp)
                        ) {
                            drawCircle(color = Color.LightGray)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = partnerDeviceName.text,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Active • Last seen 2 minutes ago",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        EverusButton(
                            onClick = { showRenamePartnerDeviceDialog = true },
                            text = "Rename",
                            modifier = Modifier
                                .width(80.dp)
                                .height(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        EverusButton(
                            onClick = { },
                            text = "Remove",
                            modifier = Modifier
                                .width(80.dp)
                                .height(32.dp)
                        )
                    }
                }
            }
        }

        if (showRenameThisDeviceDialog) {
            AlertDialog(
                onDismissRequest = { showRenameThisDeviceDialog = false },
                title = { Text("Rename this device") },
                text = {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(240.dp)
                    ) {
                        Text(
                            text = "Enter a name for this device",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EverusInputField(
                            value = thisDeviceName,
                            onValueChange = { thisDeviceName = it },
                            label = "",
                            placeholder = "Device name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showRenameThisDeviceDialog = false },
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = "Rename",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }

        if (showRenamePartnerDeviceDialog) {
            AlertDialog(
                onDismissRequest = { showRenamePartnerDeviceDialog = false },
                title = { Text("Rename partner's device") },
                text = {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .width(240.dp)
                    ) {
                        Text(
                            text = "Enter a name for your partner's device",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        EverusInputField(
                            value = partnerDeviceName,
                            onValueChange = { partnerDeviceName = it },
                            label = "",
                            placeholder = "Device name",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showRenamePartnerDeviceDialog = false },
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            text = "Rename",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    }
}
