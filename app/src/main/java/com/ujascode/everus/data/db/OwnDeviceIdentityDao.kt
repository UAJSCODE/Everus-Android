package com.ujascode.everus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ujascode.everus.data.model.OwnDeviceIdentity

@Dao
interface OwnDeviceIdentityDao {
    @Query("SELECT * FROM own_device_identity LIMIT 1")
    suspend fun getOwnDeviceIdentity(): OwnDeviceIdentity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnDeviceIdentity(identity: OwnDeviceIdentity): Long

    @Query("DELETE FROM own_device_identity")
    suspend fun clearOwnDeviceIdentity()
}