package com.ujascode.everus.data.model

data class DeviceRegistrationBody(
    val deviceId: String,
    val ed25519PublicKey: String,
    val x25519PublicKey: String,
    val signature: String
)

data class DeviceRegistrationResponse(val deviceId: String)

data class PairingRequestBody(
    val requestId: String,
    val fromDeviceId: String,
    val targetDeviceId: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String
)

data class PairingRequestResponse(
    val requestId: String,
    val status: String,
    val delivered: Boolean,
    val expiresAt: Long  // Timestamp when request expires
)

data class PairingResponseBody(
    val requestId: String,
    val responderDeviceId: String,
    val action: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String
)

data class PairingResponseResult(
    val requestId: String,
    val relationshipId: String? = null,
    val status: String,
    val peerDeviceId: String? = null,
    val ed25519PublicKey: String? = null,
    val x25519PublicKey: String? = null
)

data class EncryptedMessageBody(
    val messageId: String,
    val fromDeviceId: String,
    val targetDeviceId: String,
    val timestamp: Long,
    val nonce: String,
    val iv: String,
    val ciphertext: String,
    val signature: String
)

data class EncryptedMessageResponse(val messageId: String, val delivered: Boolean)

data class MessageDeliveryAckBody(
    val messageId: String,
    val receiverDeviceId: String,
    val timestamp: Long,
    val nonce: String,
    val signature: String
)

sealed interface PairingRealtimeEvent {
    data class Request(
        val requestId: String,
        val fromDeviceId: String,
        val ed25519PublicKey: ByteArray,
        val x25519PublicKey: ByteArray
    ) : PairingRealtimeEvent

    data class Accepted(
        val peerDeviceId: String,
        val ed25519PublicKey: ByteArray,
        val x25519PublicKey: ByteArray
    ) : PairingRealtimeEvent

    data class Rejected(val requestId: String) : PairingRealtimeEvent

    data class Expired(val requestId: String) : PairingRealtimeEvent

    data class Message(
        val messageId: String,
        val fromDeviceId: String,
        val timestamp: Long,
        val nonce: String,
        val iv: ByteArray,
        val ciphertext: ByteArray,
        val signature: ByteArray
    ) : PairingRealtimeEvent
}

data class ChatMessage(
    val messageId: String,
    val peerDeviceId: String,
    val text: String,
    val sentAt: Long,
    val outgoing: Boolean,
    val mediaUrl: String? = null,
    val mediaType: String? = null
)
