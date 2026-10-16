package com.ujascode.everus.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun EverusBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
    ) {
        NavigationBarItem(
            selected = selectedItem == "Chats",
            onClick = { onItemSelected("Chats") },
            icon = { Text(text = "C", style = TextStyle(fontSize = 24.sp)) },
            label = { Text("Chats") }
        )
        NavigationBarItem(
            selected = selectedItem == "Calls",
            onClick = { onItemSelected("Calls") },
            icon = { Text(text = "C", style = TextStyle(fontSize = 24.sp)) },
            label = { Text("Calls") }
        )
        NavigationBarItem(
            selected = selectedItem == "Memories",
            onClick = { onItemSelected("Memories") },
            icon = { Text(text = "M", style = TextStyle(fontSize = 24.sp)) },
            label = { Text("Memories") }
        )
        NavigationBarItem(
            selected = selectedItem == "You",
            onClick = { onItemSelected("You") },
            icon = { Text(text = "Y", style = TextStyle(fontSize = 24.sp)) },
            label = { Text("You") }
        )
    }
}