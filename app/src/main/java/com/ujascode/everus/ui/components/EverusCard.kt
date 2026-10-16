package com.ujascode.everus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun EverusCard(
    modifier: Modifier = Modifier,
    elevation: Int = 1,
    content: @Composable () -> Unit
) {
    val shadowElevation = when (elevation) {
        0, 1 -> 1.dp
        2 -> 3.dp
        else -> 6.dp
    }
    
    Card(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .shadow(shadowElevation)
            .padding(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// Outline card variant
@Composable
fun EverusOutlineCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}
