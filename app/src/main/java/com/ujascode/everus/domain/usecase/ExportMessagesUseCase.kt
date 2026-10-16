package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.ChatRepository
import javax.inject.Inject

class ExportMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend fun execute(peerDeviceId: String): String =
        chatRepository.exportMessages(peerDeviceId)
}