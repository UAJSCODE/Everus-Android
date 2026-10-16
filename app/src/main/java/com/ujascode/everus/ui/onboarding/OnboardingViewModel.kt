package com.ujascode.everus.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.domain.repository.PairingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val pairingRepository: PairingRepository
) : ViewModel() {

    enum class Step {
        WELCOME,
        PRIVACY,
        IDENTITY,
        PAIRING,
        PAIRING_SUCCESS
    }

    private val _currentStep = MutableStateFlow(Step.WELCOME)
    val currentStep: StateFlow<Step> = _currentStep.asStateFlow()

    private val _pairingEvent = MutableStateFlow<PairingRealtimeEvent?>(null)
    val pairingEvent: StateFlow<PairingRealtimeEvent?> = _pairingEvent.asStateFlow()

    init {
        viewModelScope.launch {
            pairingRepository.events.collect { event ->
                _pairingEvent.value = event
            }
        }
    }

    fun nextStep() {
        val next = when (_currentStep.value) {
            Step.WELCOME -> Step.PRIVACY
            Step.PRIVACY -> Step.IDENTITY
            Step.IDENTITY -> Step.PAIRING
            Step.PAIRING -> Step.PAIRING_SUCCESS
            Step.PAIRING_SUCCESS -> Step.PAIRING_SUCCESS
        }
        _currentStep.value = next
    }

    fun resetToWelcome() {
        _currentStep.value = Step.WELCOME
    }

    fun handlePairingEvent(event: PairingRealtimeEvent) {
        _pairingEvent.value = event
    }
}