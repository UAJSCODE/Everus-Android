package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.ChatRepository
import javax.inject.Inject

class ImportMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(peerDeviceId: String, jsonData: String): Int =
        chatRepository.importMessages(peerDeviceId, jsonData)
}