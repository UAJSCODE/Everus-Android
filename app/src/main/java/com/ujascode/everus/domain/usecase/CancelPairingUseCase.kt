package com.ujascode.everus.domain.usecase

import com.ujascode.everus.data.model.PairingResponseResult
import com.ujascode.everus.domain.repository.PairingRepository
import javax.inject.Inject

class CancelPairingUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    suspend fun execute(requestId: String): PairingResponseResult =
        pairingRepository.cancelPairingRequest(requestId)
}