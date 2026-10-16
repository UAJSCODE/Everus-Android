package com.ujascode.everus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel

@Composable
fun TestAppearanceScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    if (relationship == null) {
        return
    }

    var themeMode by remember { mutableStateOf(0) } // 0: System default, 1: Light, 2: Dark
    var accentColor by remember { mutableStateOf(0) } // 0: Default, 1: Blue, 2: Purple, 3: Pink, 4: Orange

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App bar
        EverusAppBar(
            title = "Appearance",
            showBackButton = true,
            onBackClicked = onBack
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .weight(1f)
        ) {
            Text(
                text = "Appearance",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LocalContentColor.current,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Customize how Everus looks and feels",
                fontSize = 14.sp,
                color = LocalContentColor.current.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Theme mode selection
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Theme mode",
                        fontSize = 16.sp,
                        color = LocalContentColor.current,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Choose between light, dark, or system default theme",
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == 0,
                        onClick = { themeMode = 0 },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = LocalContentColor.current.copy(alpha = 0.3f),
                            selectedColor = LocalContentColor.current
                        )
                    )
                    Text(
                        text = "System default",
                        fontSize = 16.sp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                HorizontalDivider(
                    color = LocalContentColor.current.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == 1,
                        onClick = { themeMode = 1 },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = LocalContentColor.current.copy(alpha = 0.3f),
                            selectedColor = LocalContentColor.current
                        )
                    )
                    Text(
                        text = "Light",
                        fontSize = 16.sp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                HorizontalDivider(
                    color = LocalContentColor.current.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = themeMode == 2,
                        onClick = { themeMode = 2 },
                        colors = RadioButtonDefaults.colors(
                            unselectedColor = LocalContentColor.current.copy(alpha = 0.3f),
                            selectedColor = LocalContentColor.current
                        )
                    )
                    Text(
                        text = "Dark",
                        fontSize = 16.sp,
                        color = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Accent color selection
            EverusCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Accent color",
                        fontSize = 16.sp,
                        color = LocalContentColor.current,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Choose your preferred accent color",
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accent color options
                (0..4).forEach { index ->
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val color = when (index) {
                            0 -> Color(0xFF6200EE) // Default purple
                            1 -> Color(0xFF2196F3) // Blue
                            2 -> Color(0xFF9C27B0) // Purple
                            3 -> Color(0xFFE91E63) // Pink
                            4 -> Color(0xFFFF9800) // Orange
                            else -> Color(0xFF6200EE)
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(color, shape = CircleShape)
                                .clickable { accentColor = index }
                                .border(
                                    width = if (accentColor == index) 2.dp else 0.dp,
                                    color = LocalContentColor.current,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (accentColor == index) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when (index) {
                                0 -> "Default"
                                1 -> "Blue"
                                2 -> "Purple"
                                3 -> "Pink"
                                4 -> "Orange"
                                else -> "Default"
                            },
                            fontSize = 12.sp,
                            color = LocalContentColor.current
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EverusButton(
                onClick = { /* TODO: Save appearance settings */ },
                text = "Save Changes",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}
