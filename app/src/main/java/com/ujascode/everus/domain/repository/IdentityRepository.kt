package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.OwnDeviceIdentity
import com.ujascode.everus.data.keystore.KeystoreManager

/**
 * Repository for managing device identity.
 */
interface IdentityRepository {
    /**
     * Ensures that the device identity key pairs exist and returns the public keys.
     */
    suspend fun ensureIdentityExists(): Unit

    /**
     * Returns the device's own X25519 public key.
     */
    fun getX25519PublicKey(): ByteArray

    /**
     * Returns the device's own Ed25519 public key.
     */
    fun getEd25519PublicKey(): ByteArray

    /**
     * Signs data with the device's Ed25519 private key.
     */
    fun sign(data: ByteArray): ByteArray

    fun calculateAgreement(peerX25519PublicKey: ByteArray): ByteArray

    /**
     * Returns the device ID (stored in Room).
     */
    suspend fun getDeviceId(): String

    /**
     * Saves the device ID (generated during first run).
     */
    suspend fun saveDeviceId(deviceId: String)
}
