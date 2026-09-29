package com.ujascode.everus.data.model

/**
 * Request to exchange encrypted long-term public keys with another device.
 */
data class EncryptedKeyExchangeRequest(
    val deviceId: String,
    val encryptedPayload: ByteArray
)