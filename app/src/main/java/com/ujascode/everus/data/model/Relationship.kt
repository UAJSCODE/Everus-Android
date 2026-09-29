package com.ujascode.everus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the pairing relationship with another device.
 */
@Entity(tableName = "relationship")
data class Relationship(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairedDeviceId: String,
    val pairedX25519PublicKey: ByteArray,
    val pairedEd25519PublicKey: ByteArray,
    val status: RelationshipStatus,
    val createdAt: Long,
    val updatedAt: Long
)
