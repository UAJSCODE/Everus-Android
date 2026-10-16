package com.ujascode.everus.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ujascode.everus.R

@Composable
fun EverusLogoLoader(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    contentDescription: String = "Loading Everus..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EverusLogoLoaderTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "EverusLogoRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        // Circular background
        Surface(
            shape = CircleShape,
            color = Color(0xFF08090B), // Near-black background
            modifier = Modifier
                .size(size)
                .border(1.dp, Color(0xFF2A2A2A), CircleShape) // Subtle circular border
        ) {
            // Logo with circular clipping
            Image(
                painter = painterResource(id = R.drawable.everus_app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .graphicsLayer {
                        rotationZ = angle
                    }
            )
        }
    }
}