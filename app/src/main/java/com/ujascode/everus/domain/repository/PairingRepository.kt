package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.EncryptedMessageBody
import com.ujascode.everus.data.model.EncryptedMessageResponse
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.data.model.PairingResponseResult
import kotlinx.coroutines.flow.SharedFlow

interface PairingRepository {
    val events: SharedFlow<PairingRealtimeEvent>

    suspend fun registerDeviceAndConnect()

    suspend fun createPairingRequest(targetDeviceId: String): String

    suspend fun respondToPairing(requestId: String, accept: Boolean): PairingResponseResult

    suspend fun sendEncryptedMessage(message: EncryptedMessageBody): EncryptedMessageResponse

    suspend fun acknowledgeMessage(messageId: String)
}
