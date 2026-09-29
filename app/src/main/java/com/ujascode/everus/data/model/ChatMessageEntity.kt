package com.ujascode.everus.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_message",
    indices = [Index(value = ["peerDeviceId", "sentAt"])]
)
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val peerDeviceId: String,
    val body: String,
    val sentAt: Long,
    val outgoing: Boolean
)
