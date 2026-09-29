package com.ujascode.everus.domain.usecase

import android.util.Base64
import com.ujascode.everus.data.model.PairingResponseResult
import com.ujascode.everus.domain.repository.PairingRepository
import javax.inject.Inject

class RespondToPairingUseCase @Inject constructor(
    private val pairingRepository: PairingRepository,
    private val persistAcceptedPairingUseCase: PersistAcceptedPairingUseCase
) {
    suspend fun execute(requestId: String, accept: Boolean): PairingResponseResult {
        val result = pairingRepository.respondToPairing(requestId, accept)
        if (accept && result.status == "COMPLETED") {
            val peerId = requireNotNull(result.peerDeviceId)
            val x25519 = Base64.decode(requireNotNull(result.x25519PublicKey), Base64.NO_WRAP)
            val ed25519 = Base64.decode(requireNotNull(result.ed25519PublicKey), Base64.NO_WRAP)
            persistAcceptedPairingUseCase.execute(peerId, x25519, ed25519)
        }
        return result
    }
}
