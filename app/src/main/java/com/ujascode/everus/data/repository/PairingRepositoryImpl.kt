package com.ujascode.everus.data.repository

import android.util.Base64
import android.util.Log
import com.ujascode.everus.BuildConfig
import com.ujascode.everus.data.model.DeviceRegistrationBody
import com.ujascode.everus.data.model.EncryptedMessageBody
import com.ujascode.everus.data.model.EncryptedMessageResponse
import com.ujascode.everus.data.model.PairingRealtimeEvent
import com.ujascode.everus.data.model.PairingRequestBody
import com.ujascode.everus.data.model.PairingResponseBody
import com.ujascode.everus.data.model.PairingResponseResult
import com.ujascode.everus.data.model.MessageDeliveryAckBody
import com.ujascode.everus.data.network.EverusApi
import com.ujascode.everus.data.network.RealtimeConnectionManager
import com.ujascode.everus.domain.repository.IdentityRepository
import com.ujascode.everus.domain.repository.PairingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PairingRepositoryImpl @Inject constructor(
    private val everusApi: EverusApi,
    private val identityRepository: IdentityRepository,
    private val realtimeConnectionManager: RealtimeConnectionManager
) : PairingRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableEvents = MutableSharedFlow<PairingRealtimeEvent>(replay = 32, extraBufferCapacity = 64)
    override val events = mutableEvents.asSharedFlow()

    init {
        scope.launch {
            realtimeConnectionManager.incoming.collect { json ->
                runCatching { parseEvent(json) }
                    .onSuccess { event ->
                        if (event != null) mutableEvents.emit(event)
                    }
                    .onFailure { Log.w(TAG, "WEBSOCKET_INVALID_EVENT") }
            }
        }
    }

    override suspend fun registerDeviceAndConnect() = withContext(Dispatchers.IO) {
        identityRepository.ensureIdentityExists()
        val deviceId = identityRepository.getDeviceId()
        val x25519 = encode(identityRepository.getX25519PublicKey())
        val ed25519 = encode(identityRepository.getEd25519PublicKey())
        val signature = encode(identityRepository.sign("register\n$deviceId\n$x25519\n$ed25519".toByteArray()))
        val response = try {
            everusApi.registerDevice(
                DeviceRegistrationBody(
                    deviceId = deviceId,
                    ed25519PublicKey = ed25519,
                    x25519PublicKey = x25519,
                    signature = signature
                )
            )
        } catch (error: Exception) {
            if (BuildConfig.DEBUG) logNetworkExceptionChain(error)
            throw error
        }
        if (!response.isSuccessful) {
            throw IllegalStateException("Device registration failed (${response.code()})")
        }
        val registeredId = response.body()?.deviceId
            ?: throw IllegalStateException("Registration response was empty")
        if (registeredId != deviceId) identityRepository.saveDeviceId(registeredId)
        realtimeConnectionManager.connect(registeredId)
        Log.i(TAG, "DEVICE_REGISTERED")
        Unit
    }

    override suspend fun createPairingRequest(targetDeviceId: String): String = withContext(Dispatchers.IO) {
        val fromDeviceId = identityRepository.getDeviceId()
        require(targetDeviceId.isNotBlank() && targetDeviceId != fromDeviceId) {
            "Enter a different registered Device ID"
        }
        val requestId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val nonce = newNonce()
        val signature = sign(
            "pair-request",
            requestId,
            fromDeviceId,
            targetDeviceId,
            timestamp.toString(),
            nonce
        )
        val response = everusApi.createPairingRequest(
            PairingRequestBody(requestId, fromDeviceId, targetDeviceId, timestamp, nonce, signature)
        )
        if (!response.isSuccessful) {
            val message = response.errorBody()?.string()?.let(::extractError)
            throw IllegalStateException(message ?: "Pairing request failed (${response.code()})")
        }
        Log.i(TAG, "PAIRING_REQUEST_SENT")
        requestId
    }

    override suspend fun respondToPairing(
        requestId: String,
        accept: Boolean
    ): PairingResponseResult = withContext(Dispatchers.IO) {
        val deviceId = identityRepository.getDeviceId()
        val action = if (accept) "ACCEPT" else "REJECT"
        val timestamp = System.currentTimeMillis()
        val nonce = newNonce()
        val signature = sign(
            "pair-response",
            requestId,
            deviceId,
            action,
            timestamp.toString(),
            nonce
        )
        val response = everusApi.respondToPairing(
            PairingResponseBody(requestId, deviceId, action, timestamp, nonce, signature)
        )
        if (!response.isSuccessful) {
            throw IllegalStateException("Pairing response failed (${response.code()})")
        }
        response.body() ?: throw IllegalStateException("Pairing response was empty")
    }

    override suspend fun sendEncryptedMessage(message: EncryptedMessageBody): EncryptedMessageResponse =
        withContext(Dispatchers.IO) {
            val response = everusApi.sendEncryptedMessage(message)
            if (!response.isSuccessful) {
                throw IllegalStateException("Message send failed (${response.code()})")
            }
            response.body() ?: throw IllegalStateException("Message response was empty")
        }

    override suspend fun acknowledgeMessage(messageId: String) = withContext(Dispatchers.IO) {
        val deviceId = identityRepository.getDeviceId()
        val timestamp = System.currentTimeMillis()
        val nonce = newNonce()
        val signature = sign("message-ack", messageId, deviceId, timestamp.toString(), nonce)
        val response = everusApi.acknowledgeMessage(
            MessageDeliveryAckBody(messageId, deviceId, timestamp, nonce, signature)
        )
        if (!response.isSuccessful) {
            throw IllegalStateException("Message delivery acknowledgement failed (${response.code()})")
        }
    }

    private fun parseEvent(json: String): PairingRealtimeEvent? {
        val value = JSONObject(json)
        return when (value.optString("type")) {
            "pairing.request" -> PairingRealtimeEvent.Request(
                requestId = value.getString("requestId").also {
                    Log.i(TAG, "PAIRING_REQUEST_RECEIVED")
                },
                fromDeviceId = value.getString("fromDeviceId"),
                ed25519PublicKey = decode(value.getString("ed25519PublicKey")),
                x25519PublicKey = decode(value.getString("x25519PublicKey"))
            )
            "pairing.accepted" -> PairingRealtimeEvent.Accepted(
                peerDeviceId = value.getString("peerDeviceId").also {
                    Log.i(TAG, "PAIRING_ACCEPTED")
                },
                ed25519PublicKey = decode(value.getString("ed25519PublicKey")),
                x25519PublicKey = decode(value.getString("x25519PublicKey"))
            )
            "pairing.rejected" -> PairingRealtimeEvent.Rejected(
                requestId = value.getString("requestId").also {
                    Log.i(TAG, "PAIRING_REJECTED")
                }
            )
            "pairing.expired" -> PairingRealtimeEvent.Expired(value.getString("requestId"))
            "message" -> PairingRealtimeEvent.Message(
                messageId = value.getString("messageId"),
                fromDeviceId = value.getString("fromDeviceId"),
                timestamp = value.getLong("timestamp"),
                nonce = value.getString("nonce"),
                iv = decode(value.getString("iv")),
                ciphertext = decode(value.getString("ciphertext")),
                signature = decode(value.getString("signature"))
            )
            else -> null
        }
    }

    private fun sign(vararg fields: String): String =
        encode(identityRepository.sign(fields.joinToString("\n").toByteArray()))

    private fun newNonce(): String =
        UUID.randomUUID().toString().replace("-", "") +
            UUID.randomUUID().toString().replace("-", "")

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)

    private fun extractError(body: String): String? =
        runCatching { JSONObject(body).optString("error").takeIf(String::isNotBlank) }.getOrNull()

    private fun logNetworkExceptionChain(error: Throwable) {
        val visited = java.util.Collections.newSetFromMap(
            java.util.IdentityHashMap<Throwable, Boolean>()
        )
        var current: Throwable? = error
        var depth = 0
        while (current != null && visited.add(current)) {
            Log.e(
                TAG,
                "NETWORK_EXCEPTION[$depth] ${current.javaClass.name}: ${current.message ?: "<no message>"}"
            )
            current = current.cause
            depth++
        }
    }

    companion object {
        private const val TAG = "EverusPairing"
    }
}
