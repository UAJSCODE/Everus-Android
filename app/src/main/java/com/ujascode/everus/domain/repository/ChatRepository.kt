package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(peerDeviceId: String): Flow<List<ChatMessage>>
    suspend fun saveMessage(message: ChatMessage): Boolean
}
