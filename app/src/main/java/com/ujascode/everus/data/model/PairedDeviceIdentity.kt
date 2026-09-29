package com.ujascode.everus.data.model

/**
 * Represents the identity of a paired device (only public keys are stored).
 */
data class PairedDeviceIdentity(
    val deviceId: String,
    val x25519PublicKey: ByteArray,
    val ed25519PublicKey: ByteArray
)