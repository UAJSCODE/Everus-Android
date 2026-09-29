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
import retrofit2.http.POST

interface EverusApi {

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegistrationBody): Response<DeviceRegistrationResponse>

    @POST("pairing/request")
    suspend fun createPairingRequest(@Body request: PairingRequestBody): Response<PairingRequestResponse>

    @POST("pairing/respond")
    suspend fun respondToPairing(@Body request: PairingResponseBody): Response<PairingResponseResult>

    @POST("messages")
    suspend fun sendEncryptedMessage(@Body request: EncryptedMessageBody): Response<EncryptedMessageResponse>

    @POST("messages/ack")
    suspend fun acknowledgeMessage(@Body request: MessageDeliveryAckBody): Response<EncryptedMessageResponse>
}
