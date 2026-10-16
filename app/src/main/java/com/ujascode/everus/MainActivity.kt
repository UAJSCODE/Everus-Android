package com.ujascode.everus

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.ujascode.everus.ui.theme.EverusTheme
import com.ujascode.everus.ui.components.EverusLogoLoader
import com.ujascode.everus.ui.onboarding.OnboardingViewModel
import com.ujascode.everus.ui.onboarding.WelcomeScreen
import com.ujascode.everus.ui.onboarding.PrivacyScreen
import com.ujascode.everus.ui.onboarding.IdentityScreen
import com.ujascode.everus.ui.onboarding.PairingScreen
import com.ujascode.everus.ui.onboarding.PairingSuccessScreen
import com.ujascode.everus.ui.home.HomeScreen
import com.ujascode.everus.presentation.viewmodel.PairingViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var localNetworkPermissionGranted: Boolean by mutableStateOf(false)

    private val requestLocalNetworkPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            localNetworkPermissionGranted = granted
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        // Check if ACCESS_LOCAL_NETWORK permission is available (API 31+)
        val localNetworkPermissionExists = true // Assume it exists for now, handle gracefully if not
        val requiresLocalNetwork = requiresLocalNetworkPermission()
        localNetworkPermissionGranted = !requiresLocalNetwork ||
                (if (localNetworkPermissionExists) {
                    ContextCompat.checkSelfPermission(
                            this,
                            "android.permission.ACCESS_LOCAL_NETWORK"
                    ) == PackageManager.PERMISSION_GRANTED
                } else false)

        setContent {
            EverusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: PairingViewModel = hiltViewModel()
                    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()
                    if (!localNetworkPermissionGranted) {
                        LocalNetworkPermissionContent {
                            requestLocalNetworkPermission.launch(
                                    "android.permission.ACCESS_LOCAL_NETWORK"
                            )
                        }
                    } else {
                        // Observe the UI state and show appropriate screen
                        when (uiState) {
                            is PairingViewModel.UiState.Loading -> {
                                LoadingScreen()
                            }
                            is PairingViewModel.UiState.Paired -> {
                                val relationship = (uiState as PairingViewModel.UiState.Paired).relationship
                                HomeScreen(relationship)
                            }
                            else -> {
                                OnboardingFlow(
                                        onboardingViewModel = onboardingViewModel,
                                        pairingViewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!localNetworkPermissionGranted && savedInstanceState == null) {
            requestLocalNetworkPermission.launch("android.permission.ACCESS_LOCAL_NETWORK")
        }
    }

    private fun requiresLocalNetworkPermission(): Boolean =
            BuildConfig.DEBUG &&
                    Build.VERSION.SDK_INT >= 31 &&
                    BuildConfig.EVERUS_API_BASE_URL.startsWith("http://10.0.2.2:")
}

@Composable
fun LocalNetworkPermissionContent(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Local network access is needed to connect to the Everus development server.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGrant) {
            Text("Allow local network access")
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EverusLogoLoader(
            size = 96.dp,
            contentDescription = "Loading Everus..."
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Loading Everus...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun OnboardingFlow(
    onboardingViewModel: OnboardingViewModel,
    pairingViewModel: PairingViewModel
) {
    val currentStepState by onboardingViewModel.currentStep.collectAsState(initial = OnboardingViewModel.Step.WELCOME)
    val ownDeviceId by pairingViewModel.ownDeviceId.collectAsState(initial = null)
    val relationship by pairingViewModel.relationship.collectAsState(initial = null)
    val uiState by pairingViewModel.uiState.collectAsState(initial = PairingViewModel.UiState.Loading)

    if (relationship != null && relationship?.status?.name == "PAIRED") {
        HomeScreen(relationship)
        return
    }

    when (currentStepState) {
        OnboardingViewModel.Step.WELCOME -> {
            WelcomeScreen(
                onNext = { onboardingViewModel.nextStep() }
            )
        }
        OnboardingViewModel.Step.PRIVACY -> {
            PrivacyScreen(
                onNext = { onboardingViewModel.nextStep() }
            )
        }
        OnboardingViewModel.Step.IDENTITY -> {
            IdentityScreen(
                onNext = { onboardingViewModel.nextStep() }
            )
        }
        OnboardingViewModel.Step.PAIRING -> {
            PairingScreen(
                ownDeviceId = ownDeviceId,
                pairingState = uiState,
                onStartPairing = pairingViewModel::startPairing,
                onRespondToPairing = pairingViewModel::respondToPairing,
                onCancelPairing = pairingViewModel::cancelPairingRequest,
                onRetryConnection = pairingViewModel::resetError
            )
        }
        OnboardingViewModel.Step.PAIRING_SUCCESS -> {
            PairingSuccessScreen(
                partnerName = "Partner",
                onNext = {}
            )
        }
        else -> {
            WelcomeScreen(
                onNext = { onboardingViewModel.nextStep() }
            )
        }
    }
}
