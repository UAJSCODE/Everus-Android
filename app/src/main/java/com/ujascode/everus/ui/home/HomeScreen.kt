package com.ujascode.everus.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun Spacer(modifier: Modifier) = androidx.compose.foundation.layout.Spacer(modifier = modifier)

@Composable
fun showDisconfirmDialog(viewModel: PairingViewModel, onRequestClose: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onRequestClose() },
        title = { Text("Disconnect Device") },
        text = { Text("Are you sure you want to disconnect from the current device?") },
        confirmButton = {
            TextButton(onClick = {
                viewModel.clearRelationship()
                onRequestClose()
            }) {
                Text("Disconnect")
            }
        },
        dismissButton = {
            TextButton(onClick = { onRequestClose() }) {
                Text("Cancel")
            }
        }
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun EverusAppBar() {
    TopAppBar(title = { Text("Everus") })
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun HomeScreen(
    relationship: Relationship?,
    viewModel: PairingViewModel = hiltViewModel()
) {
    if (relationship == null) {
        // Should not happen, but fallback to pairing screen
        return
    }
    val showDialogState = remember { mutableStateOf(false) }

    Scaffold(
        topBar = { EverusAppBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .widthIn(max = 480.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = "Everus",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connected to: ${relationship.pairedDeviceId}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status: Online",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Implement messages */ },
                    enabled = false
                ) {
                    Text("Messages")
                }
                OutlinedButton(
                    onClick = { /* TODO: Implement call */ },
                    enabled = false
                ) {
                    Text("Call")
                }
                OutlinedButton(
                    onClick = { /* TODO: Implement video call */ },
                    enabled = false
                ) {
                    Text("Video Call")
                }
                OutlinedButton(
                    onClick = { /* TODO: Implement media */ },
                    enabled = false
                ) {
                    Text("Media")
                }
                OutlinedButton(
                    onClick = { showDialogState.value = true }
                ) {
                    Text("Settings")
                }
            }
        }
    }
    if (showDialogState.value) {
        showDisconfirmDialog(viewModel) { showDialogState.value = false }
    }
}