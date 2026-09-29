package com.ujascode.everus.domain.usecase

import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.data.model.RelationshipStatus
import com.ujascode.everus.domain.repository.RelationshipRepository
import javax.inject.Inject

class PersistAcceptedPairingUseCase @Inject constructor(
    private val relationshipRepository: RelationshipRepository
) {
    suspend fun execute(peerDeviceId: String, x25519PublicKey: ByteArray, ed25519PublicKey: ByteArray) {
        val existing = relationshipRepository.getRelationship()
        if (existing?.pairedDeviceId == peerDeviceId) {
            relationshipRepository.updateRelationshipStatus(RelationshipStatus.PAIRED)
            return
        }
        if (existing != null) relationshipRepository.clearRelationship()
        val now = System.currentTimeMillis()
        relationshipRepository.saveRelationship(
            Relationship(
                pairedDeviceId = peerDeviceId,
                pairedX25519PublicKey = x25519PublicKey,
                pairedEd25519PublicKey = ed25519PublicKey,
                status = RelationshipStatus.PAIRED,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
