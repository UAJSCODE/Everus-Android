package com.ujascode.everus.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearnMoreDialog(
    onDismiss: () -> Unit
) {
    val contentColor = MaterialTheme.colorScheme.onBackground

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Everus Security") },
        text = {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Everus provides private, secure communication between two trusted people.",
                    fontSize = 16.sp,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All messages and calls are end-to-end encrypted using modern cryptographic protocols.",
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Encryption keys are generated and stored only on your devices, never on our servers.",
                    fontSize = 14.sp,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.width(80.dp)
            ) {
                Text(
                    text = "Got it",
                    color = contentColor
                )
            }
        }
    )
}