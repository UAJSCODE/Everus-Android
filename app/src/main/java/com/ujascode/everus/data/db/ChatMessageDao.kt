package com.ujascode.everus.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ujascode.everus.data.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_message WHERE peerDeviceId = :peerDeviceId ORDER BY sentAt")
    fun observeMessages(peerDeviceId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}
