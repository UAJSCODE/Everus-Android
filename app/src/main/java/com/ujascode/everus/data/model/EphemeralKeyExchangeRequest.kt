package com.ujascode.everus.data.model

/**
 * Request to exchange ephemeral public keys with another device.
 */
data class EphemeralKeyExchangeRequest(
    val deviceId: String,
    val ephemeralPublicKey: ByteArray
)