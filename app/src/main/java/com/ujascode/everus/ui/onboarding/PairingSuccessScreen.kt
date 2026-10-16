package com.ujascode.everus.ui.onboarding

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
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun PairingSuccessScreen(
    partnerName: String,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(
            modifier = Modifier.size(64.dp)
        ) {
            drawCircle(color = Color(0xFF34C759))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "YOU'RE CONNECTED",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = partnerName,
            fontSize = 18.sp,
            color = contentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Your private space is ready.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        EverusButton(
            onClick = onNext,
            text = "Continue",
            modifier = Modifier
                .width(200.dp)
                .height(48.dp)
        )
    }
}