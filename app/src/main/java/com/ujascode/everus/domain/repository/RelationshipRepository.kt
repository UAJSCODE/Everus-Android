package com.ujascode.everus.domain.repository

import com.ujascode.everus.data.model.Relationship
import com.ujascode.everus.data.model.RelationshipStatus

/**
 * Repository for managing the pairing relationship.
 */
interface RelationshipRepository {
    /**
     * Saves the relationship with the paired device.
     */
    suspend fun saveRelationship(relationship: Relationship)

    /**
     * Retrieves the current relationship, if any.
     */
    suspend fun getRelationship(): Relationship?

    /**
     * Updates the status of the relationship.
     */
    suspend fun updateRelationshipStatus(status: RelationshipStatus)

    /**
     * Clears the relationship (e.g., on revocation).
     */
    suspend fun clearRelationship()
}