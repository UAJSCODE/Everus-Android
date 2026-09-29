package com.ujascode.everus.data.repository

import com.ujascode.everus.data.db.ChatMessageDao
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.ChatMessageEntity
import com.ujascode.everus.domain.repository.ChatRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {
    override fun observeMessages(peerDeviceId: String): Flow<List<ChatMessage>> =
        chatMessageDao.observeMessages(peerDeviceId).map { messages ->
            messages.map {
                ChatMessage(it.messageId, it.peerDeviceId, it.body, it.sentAt, it.outgoing)
            }
        }

    override suspend fun saveMessage(message: ChatMessage): Boolean =
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                messageId = message.messageId,
                peerDeviceId = message.peerDeviceId,
                body = message.text,
                sentAt = message.sentAt,
                outgoing = message.outgoing
            )
        ) != -1L
}
