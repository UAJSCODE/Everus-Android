package com.ujascode.everus.domain.usecase

import com.ujascode.everus.data.model.ChatMessage
import com.ujascode.everus.domain.repository.ChatRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    fun execute(peerDeviceId: String): Flow<List<ChatMessage>> =
        chatRepository.observeMessages(peerDeviceId)
}
