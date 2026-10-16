package com.ujascode.everus.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun WelcomeScreen(
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
        Text(
            text = "EVERUS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Your private space for two.",
            fontSize = 18.sp,
            color = contentColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Messages. Calls. Memories.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Designed privately.",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        EverusButton(
            onClick = onNext,
            text = "Get Started",
            modifier = Modifier
                .width(200.dp)
                .height(48.dp)
        )

        Column(
            modifier = Modifier.padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Private by design",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.6f)
            )
            Text(
                text = "End-to-end encrypted",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}