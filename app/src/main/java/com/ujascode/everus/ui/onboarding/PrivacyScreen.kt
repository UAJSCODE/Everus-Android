package com.ujascode.everus.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ujascode.everus.ui.components.EverusButton

@Composable
fun PrivacyScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLearnMore by remember { mutableStateOf(false) }
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
    ) {
        Text(
            text = "YOUR CONVERSATION IS YOURS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            PrivacyFeatureItem(text = "Private messaging")
            PrivacyFeatureItem(text = "Secure communication")
            PrivacyFeatureItem(text = "Protected identity")
            PrivacyFeatureItem(text = "Private media")
        }

        TextButton(
            onClick = { showLearnMore = true },
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Text(
                text = "Learn More",
                color = contentColor
            )
        }

        if (showLearnMore) {
            LearnMoreDialog(
                onDismiss = { showLearnMore = false }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        EverusButton(
            onClick = onNext,
            text = "Continue",
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    }
}

@Composable
private fun PrivacyFeatureItem(
    text: String
) {
    val contentColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawCircle(color = Color(0xFF34C759))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = contentColor
        )
    }
}