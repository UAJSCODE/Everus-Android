package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.ChatRepository
import javax.inject.Inject

class DeleteAllMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(peerDeviceId: String): Int =
        chatRepository.deleteAllMessages(peerDeviceId)
}