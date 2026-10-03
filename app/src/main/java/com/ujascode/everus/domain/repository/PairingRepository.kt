package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.EncryptedMessageBody
import com.ujascode.everus.data.model.EncryptedMessageResponse
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.data.model.PairingResponseResult
import com.ujascode.everus.data.model.PairingResult
import com.ujascode.everus.data.network.MediaTransferDownloadResponse
import com.ujascode.everus.data.network.MediaTransferInitResponse
import com.ujascode.everus.data.network.MediaTransferStatusResponse
import com.ujascode.everus.data.network.MediaTransferUploadResponse
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface PairingRepository {
    val events: SharedFlow<PairingRealtimeEvent>
    val connectionStatus: StateFlow<Boolean>

    suspend fun registerDeviceAndConnect()

    suspend fun createPairingRequest(targetDeviceId: String): PairingResult

    suspend fun respondToPairing(requestId: String, accept: Boolean): PairingResponseResult

    suspend fun cancelPairingRequest(requestId: String): PairingResponseResult

    suspend fun getActivePairingRequest(): PairingResult?

    suspend fun sendEncryptedMessage(message: EncryptedMessageBody): EncryptedMessageResponse

    suspend fun acknowledgeMessage(messageId: String)

    suspend fun deleteRelationship()

    // MEDIA TRANSFER FUNCTIONS
    suspend fun initMediaTransfer(
        senderDeviceId: String,
        receiverDeviceId: String,
        filename: String,
        mimeType: String,
        fileSize: Long
    ): MediaTransferInitResponse

    suspend fun uploadMediaChunk(
        transferId: String,
        chunkIndex: Int,
        totalChunks: Int,
        data: String,
        checksum: String? = null
    ): MediaTransferUploadResponse

    suspend fun getMediaDownloadUrl(
        transferId: String,
        authToken: String
    ): MediaTransferDownloadResponse

    suspend fun getMediaTransferStatus(
        transferId: String
    ): MediaTransferStatusResponse
}
