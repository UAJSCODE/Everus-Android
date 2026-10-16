package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun SecurityDashboard(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    EverusCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Privacy & Security",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(color = Color(0xFF34C759))
                    }
                    Text(
                        text = "Protected",
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(color = Color(0xFF34C759))
                    }
                    Text(
                        text = "Secure",
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        drawCircle(color = Color(0xFF34C759))
                    }
                    Text(
                        text = "Paired device",
                        fontSize = 16.sp,
                        color = contentColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            HorizontalDivider(
                color = contentColor.copy(alpha = 0.1f),
                thickness = 1.dp
            )

            Text(
                text = "Security Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Text(
                text = "Your connection is protected with end-to-end encryption.",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Messages and calls are encrypted using Signal Protocol.",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            EverusButton(
                onClick = { },
                text = "View Security Details",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(top = 16.dp)
            )
        }
    }
}