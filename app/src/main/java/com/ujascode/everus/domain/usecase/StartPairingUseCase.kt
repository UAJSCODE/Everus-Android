package com.ujascode.everus.domain.usecase

import com.ujascode.everus.data.model.PairingResult
import com.ujascode.everus.domain.repository.PairingRepository
import javax.inject.Inject

class StartPairingUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    suspend fun execute(targetDeviceId: String): PairingResult =
        pairingRepository.createPairingRequest(targetDeviceId)
}
