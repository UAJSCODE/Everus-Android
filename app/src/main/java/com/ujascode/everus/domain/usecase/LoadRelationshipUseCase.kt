package com.ujascode.everus.domain.usecase

import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.domain.repository.RelationshipRepository
import javax.inject.Inject

class LoadRelationshipUseCase @Inject constructor(
    private val relationshipRepository: RelationshipRepository
) {
    suspend fun execute(): Relationship? = relationshipRepository.getRelationship()
}
