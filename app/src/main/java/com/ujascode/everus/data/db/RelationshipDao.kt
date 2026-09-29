package com.ujascode.everus.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ujascode.everus.data.model.Relationship

@Dao
interface RelationshipDao {
    @Query("SELECT * FROM relationship LIMIT 1")
    suspend fun getRelationship(): Relationship?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: Relationship): Long

    @Query("DELETE FROM relationship")
    suspend fun clearRelationship()

    @Query("UPDATE relationship SET status = :status, updatedAt = :updatedAt WHERE rowid = (SELECT rowid FROM relationship LIMIT 1)")
    suspend fun updateRelationshipStatus(status: String, updatedAt: Long)
}