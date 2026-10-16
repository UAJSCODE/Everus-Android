package com.ujascode.everus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.*

private val DefaultActionIcon: DrawScope.() -> Unit = {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EverusAppBar(
    title: String = "Everus",
    showBackButton: Boolean = false,
    onBackClicked: () -> Unit = {},
    actionIcon: DrawScope.() -> Unit = DefaultActionIcon,
    onActionClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            if (actionIcon != DefaultActionIcon) {
                IconButton(
                    onClick = onActionClicked,
                    modifier = Modifier.size(48.dp)
                ) {
                    // Use geometry Size to specify dimensions
                    val density = LocalDensity.current
                    val size = with(density) {
                        Size(
                            width = 24.dp.toPx(),
                            height = 24.dp.toPx()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(with(density) { size.width.toDp() })
                            .height(with(density) { size.height.toDp() })
                            .drawWithContent {
                                actionIcon()
                            }
                    )
                }
            }
        }
    )
}