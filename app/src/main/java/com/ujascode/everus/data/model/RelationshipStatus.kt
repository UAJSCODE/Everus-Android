package com.ujascode.everus.data.model

/**
 * Status of the pairing relationship.
 */
enum class RelationshipStatus {
    PENDING,      // Pairing initiated, waiting for verification
    PAIRED,       // Peer accepted and signed the pairing request
    VERIFIED,     // Out-of-band verification completed, relationship trusted
    REVOKED       // Relationship revoked (device unpaired)
}
