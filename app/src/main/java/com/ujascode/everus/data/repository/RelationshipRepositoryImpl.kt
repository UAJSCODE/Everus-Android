package com.ujascode.everus.data.repository

import com.ujascode.everus.data.db.RelationshipDao
import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.data.model.RelationshipStatus
import com.ujascode.everus.domain.repository.RelationshipRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RelationshipRepositoryImpl @Inject constructor(
    private val relationshipDao: RelationshipDao
) : RelationshipRepository {

    override suspend fun saveRelationship(relationship: Relationship) {
        relationshipDao.insertRelationship(relationship)
    }

    override suspend fun getRelationship(): Relationship? {
        return relationshipDao.getRelationship()
    }

    override suspend fun updateRelationshipStatus(status: RelationshipStatus) {
        val current = relationshipDao.getRelationship()
        if (current != null) {
            val updated = current.copy(
                status = status,
                updatedAt = System.currentTimeMillis()
            )
            relationshipDao.insertRelationship(updated)
        }
    }

    override suspend fun clearRelationship() {
        relationshipDao.clearRelationship()
    }
}
