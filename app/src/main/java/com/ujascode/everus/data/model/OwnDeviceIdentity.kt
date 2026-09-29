package com.ujascode.everus.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the device's own identity (only device ID is stored here; keys are in Keystore).
 */
@Entity(tableName = "own_device_identity")
data class OwnDeviceIdentity(
    @PrimaryKey val deviceId: String
)
