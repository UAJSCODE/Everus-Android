package com.ujascode.everus.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.components.EverusButton
import com.ujascode.everus.ui.components.EverusCard

@Composable
fun MemoriesScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        return
    }

    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MEMORIES",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MemoryFilterChip(text = "All", isSelected = true)
            MemoryFilterChip(text = "Photos", isSelected = false)
            MemoryFilterChip(text = "Videos", isSelected = false)
            MemoryFilterChip(text = "Files", isSelected = false)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(6) { index ->
                EverusCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Canvas(modifier = Modifier.size(32.dp)) {
                                drawCircle(color = contentColor.copy(alpha = 0.3f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Memory $index",
                                fontSize = 12.sp,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryFilterChip(
    text: String,
    isSelected: Boolean
) {
    EverusButton(
        onClick = { },
        text = text,
        modifier = Modifier
            .height(32.dp)
            .padding(horizontal = 4.dp),
        enabled = true
    )
}