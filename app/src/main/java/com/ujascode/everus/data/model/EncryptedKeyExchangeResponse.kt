package com.ujascode.everus.data.model

/**
 * Response containing the encrypted payload from the paired device.
 */
data class EncryptedKeyExchangeResponse(
    val encryptedPayload: ByteArray
)