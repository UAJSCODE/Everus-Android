package com.ujascode.everus.data.repository

import com.ujascode.everus.data.db.ChatMessageDao
import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.data.model.ChatMessageEntity
import com.ujascode.everus.domain.repository.ChatRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {
    override fun observeMessages(peerDeviceId: String): Flow<List<ChatMessage>> =
        chatMessageDao.observeMessages(peerDeviceId).map { messages ->
            messages.map {
                ChatMessage(it.messageId, it.peerDeviceId, it.body, it.sentAt, it.outgoing, it.mediaUrl, it.mediaType)
            }
        }

    override suspend fun saveMessage(message: ChatMessage): Boolean =
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                messageId = message.messageId,
                peerDeviceId = message.peerDeviceId,
                body = message.body,
                sentAt = message.sentAt,
                outgoing = message.outgoing,
                mediaUrl = message.mediaUrl,
                mediaType = message.mediaType
            )
        ) != -1L

    override suspend fun deleteMessage(messageId: String): Int =
        chatMessageDao.deleteMessage(messageId)

    override suspend fun deleteAllMessages(peerDeviceId: String): Int =
        chatMessageDao.deleteAllMessages(peerDeviceId)

    override suspend fun exportMessages(peerDeviceId: String): String {
        val messages = chatMessageDao.observeMessages(peerDeviceId)
                .first() // Get the current list of messages

        // Convert to JSON for export
        val messageList = messages.map { msg ->
            mapOf(
                "messageId" to msg.messageId,
                "peerDeviceId" to msg.peerDeviceId,
                "body" to msg.body,
                "sentAt" to msg.sentAt,
                "outgoing" to msg.outgoing,
                "mediaUrl" to msg.mediaUrl,
                "mediaType" to msg.mediaType
            )
        }

        return Gson().toJson(messageList)
    }

    override suspend fun importMessages(peerDeviceId: String, jsonData: String): Int {
        try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val messageList: List<Map<String, Any>> = Gson().fromJson(jsonData, type)

            var count = 0
            for (msgMap in messageList) {
                val message = ChatMessage(
                    messageId = msgMap["messageId"] as String,
                    peerDeviceId = msgMap["peerDeviceId"] as String,
                    body = msgMap["body"] as String,
                    sentAt = msgMap["sentAt"] as Long,
                    outgoing = msgMap["outgoing"] as Boolean,
                    mediaUrl = msgMap["mediaUrl"] as String?,
                    mediaType = msgMap["mediaType"] as String?
                )

                chatMessageDao.insertMessage(
                    ChatMessageEntity(
                        messageId = message.messageId,
                        peerDeviceId = message.peerDeviceId,
                        body = message.body,
                        sentAt = message.sentAt,
                        outgoing = message.outgoing,
                        mediaUrl = message.mediaUrl,
                        mediaType = message.mediaType
                    )
                )
                count++
            }

            return count
        } catch (e: Exception) {
            throw IllegalStateException("Failed to import messages: ${e.message}")
        }
    }
}
