package com.test.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TestPackage(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Check",
        modifier = modifier
    )
}
