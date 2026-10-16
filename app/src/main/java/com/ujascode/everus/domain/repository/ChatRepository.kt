package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(peerDeviceId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage): Boolean
    suspend fun deleteMessage(messageId: String): Int
    suspend fun deleteAllMessages(peerDeviceId: String): Int
    suspend fun exportMessages(peerDeviceId: String): String
    suspend fun importMessages(peerDeviceId: String, jsonData: String): Int
}
