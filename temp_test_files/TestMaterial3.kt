package com.ujascode.everus.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TestMaterial3() {
    // Test if basic material3 components work
    Text(text = "Test")
    Button(onClick = {}) {
        Text(text = "Button")
    }

    // NavigationBar in Material 3
    NavigationBar(
        modifier = Modifier
    ) {
        // NavigationBar content
    }
}