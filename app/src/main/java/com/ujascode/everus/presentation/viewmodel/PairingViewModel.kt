package com.ujascode.everus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.domain.repository.PairingRepository
import com.ujascode.everus.domain.usecase.GenerateIdentityUseCase
import com.ujascode.everus.domain.usecase.GetOwnDeviceIdUseCase
import com.ujascode.everus.domain.usecase.LoadRelationshipUseCase
import com.ujascode.everus.domain.usecase.ObserveChatMessagesUseCase
import com.ujascode.everus.domain.usecase.PersistAcceptedPairingUseCase
import com.ujascode.everus.domain.usecase.ReceiveEncryptedMessageUseCase
import com.ujascode.everus.domain.usecase.RespondToPairingUseCase
import com.ujascode.everus.domain.usecase.SendEncryptedMessageUseCase
import com.ujascode.everus.domain.usecase.StartPairingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PairingViewModel @Inject constructor(
    private val generateIdentityUseCase: GenerateIdentityUseCase,
    private val getOwnDeviceIdUseCase: GetOwnDeviceIdUseCase,
    private val pairingRepository: PairingRepository,
    private val startPairingUseCase: StartPairingUseCase,
    private val respondToPairingUseCase: RespondToPairingUseCase,
    private val persistAcceptedPairingUseCase: PersistAcceptedPairingUseCase,
    private val loadRelationshipUseCase: LoadRelationshipUseCase,
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val sendEncryptedMessageUseCase: SendEncryptedMessageUseCase,
    private val receiveEncryptedMessageUseCase: ReceiveEncryptedMessageUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _relationship = MutableStateFlow<Relationship?>(null)
    val relationship: StateFlow<Relationship?> = _relationship.asStateFlow()

    private val _ownDeviceId = MutableStateFlow<String?>(null)
    val ownDeviceId: StateFlow<String?> = _ownDeviceId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private var messagesJob: Job? = null
    private var initializationJob: Job? = null

    init {
        // Subscribe before connecting; replay also protects events delivered during startup.
        viewModelScope.launch {
            pairingRepository.events.collect(::handleRealtimeEvent)
        }
        initialize()
    }

    private fun initialize() {
        if (initializationJob?.isActive == true) return
        _uiState.value = UiState.Loading
        initializationJob = viewModelScope.launch {
            try {
                generateIdentityUseCase.execute()
                _ownDeviceId.value = getOwnDeviceIdUseCase.execute()
                pairingRepository.registerDeviceAndConnect()
                val existing = loadRelationshipUseCase.execute()
                _relationship.value = existing
                if (existing?.status?.name == "PAIRED") {
                    _uiState.value = UiState.Paired(existing)
                    observeMessages(existing.pairedDeviceId)
                } else {
                    _uiState.value = UiState.Idle
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Unable to connect to Everus")
            }
        }
    }

    fun startPairing(targetDeviceId: String) {
        val target = targetDeviceId.trim()
        if (target.isEmpty() || _uiState.value is UiState.Pairing) return
        viewModelScope.launch {
            _uiState.value = UiState.Pairing(target)
            try {
                val requestId = startPairingUseCase.execute(target)
                if (_uiState.value is UiState.Pairing) {
                    _uiState.value = UiState.WaitingForApproval(requestId, target)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Pairing request failed")
            }
        }
    }

    fun respondToPairing(requestId: String, accept: Boolean) {
        val request = (_uiState.value as? UiState.IncomingRequest)?.request ?: return
        if (request.requestId != requestId) return
        viewModelScope.launch {
            _uiState.value = UiState.Responding
            try {
                val result = respondToPairingUseCase.execute(requestId, accept)
                if (accept && result.status == "COMPLETED") {
                    val relationship = loadRelationshipUseCase.execute()
                        ?: throw IllegalStateException("Paired relationship was not persisted")
                    _relationship.value = relationship
                    _uiState.value = UiState.Paired(relationship)
                    observeMessages(relationship.pairedDeviceId)
                } else {
                    _uiState.value = UiState.Idle
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Unable to respond to pairing request")
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value !is UiState.Paired) return
        viewModelScope.launch {
            try {
                sendEncryptedMessageUseCase.execute(text.trim())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.value = UiState.Error(error.localizedMessage ?: "Message could not be sent")
            }
        }
    }

    fun resetError() {
        when (_uiState.value) {
            is UiState.Error -> initialize()
            UiState.Rejected, UiState.Expired ->
                _uiState.value = _relationship.value?.let(UiState::Paired) ?: UiState.Idle
            else -> Unit
        }
    }

    private suspend fun handleRealtimeEvent(event: PairingRealtimeEvent) {
        try {
            when (event) {
                is PairingRealtimeEvent.Request -> {
                    if (_relationship.value == null) _uiState.value = UiState.IncomingRequest(event)
                }
                is PairingRealtimeEvent.Accepted -> {
                    persistAcceptedPairingUseCase.execute(
                        event.peerDeviceId,
                        event.x25519PublicKey,
                        event.ed25519PublicKey
                    )
                    val relationship = loadRelationshipUseCase.execute()
                        ?: throw IllegalStateException("Accepted relationship was not persisted")
                    _relationship.value = relationship
                    _uiState.value = UiState.Paired(relationship)
                    observeMessages(relationship.pairedDeviceId)
                }
                is PairingRealtimeEvent.Rejected -> {
                    _uiState.value = UiState.Rejected
                }
                is PairingRealtimeEvent.Expired -> {
                    _uiState.value = UiState.Expired
                }
                is PairingRealtimeEvent.Message -> {
                    receiveEncryptedMessageUseCase.execute(event)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _uiState.value = UiState.Error("A pairing or encrypted-message event could not be verified")
        }
    }

    private fun observeMessages(peerDeviceId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            observeChatMessagesUseCase.execute(peerDeviceId).collect { _messages.value = it }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data object Idle : UiState()
        data class Pairing(val targetDeviceId: String) : UiState()
        data class WaitingForApproval(val requestId: String, val targetDeviceId: String) : UiState()
        data class IncomingRequest(val request: PairingRealtimeEvent.Request) : UiState()
        data object Responding : UiState()
        data class Paired(val relationship: Relationship) : UiState()
        data object Rejected : UiState()
        data object Expired : UiState()
        data class Error(val message: String) : UiState()
    }
}
