package com.ujascode.everus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawWithContent
import androidx.compose.ui.graphics.drawscope.InspectableValueHolder
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp

@Composable
fun TestGeometrySizeApproach() {
    // Create a geometry Size object (constructor expects Float pixels)
    val widthPx = 24.dp.roundToPx()
    val heightPx = 24.dp.roundToPx()
    val size = Size(widthPx, heightPx)
    
    Box(
        modifier = Modifier
            .width(size.width)
            .height(size.height)
    ) {
        drawWithContent {
            // Content would go here
        }
    }
}
