package com.ujascode.everus.data.model

/**
 * Represents the long-term identity key pair of a device.
 * The private key is stored securely in the Android Keystore.
 */
data class DeviceIdentity(
    val deviceId: String,
    val x25519PublicKey: ByteArray,
    val ed25519PublicKey: ByteArray
)