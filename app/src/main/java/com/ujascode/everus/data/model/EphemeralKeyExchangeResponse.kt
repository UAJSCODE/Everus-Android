package com.ujascode.everus.data.model

/**
 * Response containing the ephemeral public key of the paired device.
 */
data class EphemeralKeyExchangeResponse(
    val ephemeralPublicKey: ByteArray
)