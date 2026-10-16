package com.ujascode.everus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EmptyButtonIcon: DrawScope.() -> Unit = {}

@Composable
fun EverusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "",
    icon: DrawScope.() -> Unit = EmptyButtonIcon,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonColors = if (enabled) {
        if (loading) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    }
    val contentColor = if (enabled && !loading) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    }
    val hasIcon = icon !== EmptyButtonIcon

    Button(
        onClick = { if (enabled && !loading) onClick() },
        modifier = modifier.height(48.dp),
        enabled = enabled && !loading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColors,
            contentColor = contentColor,
            disabledContainerColor = buttonColors,
            disabledContentColor = contentColor
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(min = 64.dp)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (hasIcon) {
                Arrangement.spacedBy(8.dp)
            } else {
                Arrangement.Center
            }
        ) {
            if (hasIcon) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = contentColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .drawWithContent {
                                drawContent()
                                icon()
                            }
                    )
                }
            }

            if (!loading && text.isNotEmpty()) {
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Outline button variant
@Composable
fun EverusOutlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: DrawScope.() -> Unit = EmptyButtonIcon
) {
    val interactionSource = remember { MutableInteractionSource() }
    val borderColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    }
    val hasIcon = icon !== EmptyButtonIcon

    Button(
        onClick = { if (enabled) onClick() },
        modifier = modifier.height(48.dp),
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = contentColor
        ),
        border = BorderStroke(1.dp, borderColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(min = 64.dp)
                .wrapContentWidth(align = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (hasIcon) {
                Arrangement.spacedBy(8.dp)
            } else {
                Arrangement.Center
            }
        ) {
            if (hasIcon) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .drawWithContent {
                            drawContent()
                            icon()
                        }
                )
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
