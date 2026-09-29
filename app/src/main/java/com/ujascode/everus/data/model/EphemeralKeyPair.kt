package com.ujascode.everus.data.model

/**
 * Ephemeral key pair used for the pairing protocol.
 * Not stored long-term; kept in memory only.
 */
data class EphemeralKeyPair(
    val privateKey: ByteArray,
    val publicKey: ByteArray
)