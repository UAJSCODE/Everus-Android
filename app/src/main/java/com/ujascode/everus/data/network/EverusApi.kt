package com.ujascode.everus.data.network

import com.ujascode.everus.data.model.DeviceRegistrationBody
import com.ujascode.everus.data.model.DeviceRegistrationResponse
import com.ujascode.everus.data.model.EncryptedMessageBody
import com.ujascode.everus.data.model.EncryptedMessageResponse
import com.ujascode.everus.data.model.PairingRequestBody
import com.ujascode.everus.data.model.PairingRequestResponse
import com.ujascode.everus.data.model.PairingResponseBody
import com.ujascode.everus.data.model.PairingResponseResult
import com.ujascode.everus.data.model.MessageDeliveryAckBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EverusApi {

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationBody): Response<DeviceRegistrationResponse>

    @POST("pairing/request")
    suspend fun createPairingRequest(@Body request: PairingRequestBody): Response<PairingRequestResponse>

    @POST("pairing/respond")
    suspend fun respondToPairing(@Body request: PairingResponseBody): Response<PairingResponseResult>

    @POST("pairing/cancel/{requestId}")
    suspend fun cancelPairingRequest(
        @Path("requestId") requestId: String
    ): Response<PairingResponseResult>

    @GET("pairing/active")
    suspend fun getActivePairingRequest(): Response<PairingRequestResponse>

    @POST("messages")
    suspend fun sendEncryptedMessage(@Body request: EncryptedMessageBody): Response<EncryptedMessageResponse>

    @POST("messages/ack")
    suspend fun acknowledgeMessage(@Body request: MessageDeliveryAckBody): Response<EncryptedMessageResponse>

    // MEDIA TRANSFER ENDPOINTS
    @POST("media/init")
    suspend fun initMediaTransfer(@Body request: MediaTransferInitRequest): Response<MediaTransferInitResponse>

    @POST("media/{transferId}/upload")
    suspend fun uploadMediaChunk(
        @Path("transferId") transferId: String,
        @Body request: MediaTransferUploadRequest
    ): Response<MediaTransferUploadResponse>

    @GET("media/{transferId}/download")
    suspend fun getMediaDownloadUrl(
        @Path("transferId") transferId: String,
        @Header("Authorization") authToken: String
    ): Response<MediaTransferDownloadResponse>

    @GET("media/{transferId}")
    suspend fun getMediaTransferStatus(
        @Path("transferId") transferId: String
    ): Response<MediaTransferStatusResponse>
}

// DATA CLASSES FOR MEDIA TRANSFER
data class MediaTransferInitRequest(
    val senderDeviceId: String,
    val receiverDeviceId: String,
    val filename: String,
    val mimeType: String,
    val fileSize: Long
)

data class MediaTransferInitResponse(
    val transferId: String,
    val uploadUrl: String,
    val expiresAt: String, // ISO 8601 timestamp
    val maxChunkSize: Int
)

data class MediaTransferUploadRequest(
    val chunkIndex: Int,
    val totalChunks: Int,
    val data: String, // Base64 encoded chunk data
    val checksum: String? = null
)

data class MediaTransferUploadResponse(
    val success: Boolean,
    val uploadProgress: Int,
    val status: String // PENDING, UPLOADING, COMPLETED
)

data class MediaTransferDownloadResponse(
    val downloadUrl: String,
    val expiresAt: String, // ISO 8601 timestamp
    val filename: String,
    val mimeType: String,
    val fileSize: Long,
    val checksum: String? = null
)

data class MediaTransferStatusResponse(
    val transferId: String,
    val status: String, // PENDING, UPLOADING, COMPLETED, FAILED, EXPIRED
    val uploadProgress: Int,
    val filename: String,
    val mimeType: String,
    val fileSize: Long,
    val objectKey: String,
    val bucket: String,
    val checksum: String? = null,
    val createdAt: String, // ISO 8601 timestamp
    val expiresAt: String // ISO 8601 timestamp
)