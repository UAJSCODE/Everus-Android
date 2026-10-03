package com.ujascode.everus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ujascode.everus.ui.theme.EverusTheme
import com.ujascode.everus.ui.pairing.PairingScreen
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import com.ujascode.everus.ui.home.HomeScreen
import androidx.compose.material3.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var localNetworkPermissionGranted by mutableStateOf(false)

    private val requestLocalNetworkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            localNetworkPermissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        localNetworkPermissionGranted = !requiresLocalNetworkPermission() ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) == PackageManager.PERMISSION_GRANTED

        setContent {
            EverusTheme {
                if (localNetworkPermissionGranted) {
                    PairingScreen()
                } else {
                    LocalNetworkPermissionContent {
                        requestLocalNetworkPermission.launch(
                            Manifest.permission.ACCESS_LOCAL_NETWORK
                        )
                    }
                }
            }
        }

        if (!localNetworkPermissionGranted && savedInstanceState == null) {
            requestLocalNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    private fun requiresLocalNetworkPermission(): Boolean =
        BuildConfig.DEBUG &&
            Build.VERSION.SDK_INT >= 37 &&
            BuildConfig.EVERUS_API_BASE_URL.startsWith("http://10.0.2.2:")
}

@Composable
private fun LocalNetworkPermissionContent(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Local network access is needed to connect to the Everus development server.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGrant) {
            Text("Allow local network access")
        }
    }
}
