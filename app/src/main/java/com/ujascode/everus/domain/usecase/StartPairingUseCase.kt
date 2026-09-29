package com.ujascode.everus.domain.usecase

import com.ujascode.everus.domain.repository.PairingRepository
import javax.inject.Inject

class StartPairingUseCase @Inject constructor(
    private val pairingRepository: PairingRepository
) {
    suspend fun execute(targetDeviceId: String): String =
        pairingRepository.createPairingRequest(targetDeviceId)
}
