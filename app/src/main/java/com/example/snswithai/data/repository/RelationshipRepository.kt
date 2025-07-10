package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Relationship
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RelationshipRepository(private val db: FirebaseFirestore) {

    private val relationshipsCollection = db.collection("relationships")

    suspend fun createRelationship(relationship: Relationship) {
        relationshipsCollection.document(relationship.relationshipId).set(relationship).await()
    }

    suspend fun getRelationship(relationshipId: String): Relationship? {
        return relationshipsCollection.document(relationshipId).get().await().toObject(Relationship::class.java)
    }

    suspend fun updateRelationship(relationship: Relationship) {
        relationshipsCollection.document(relationship.relationshipId).set(relationship).await()
    }

    suspend fun deleteRelationship(relationshipId: String) {
        relationshipsCollection.document(relationshipId).delete().await()
    }

    suspend fun getRelationshipsForUser(userId: String): List<Relationship> {
        return relationshipsCollection.whereEqualTo("userId", userId).get().await().toObjects(Relationship::class.java)
    }

    suspend fun getRelationshipByUserAndCharacter(userId: String, characterId: String): Relationship? {
        return relationshipsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("characterId", characterId)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toObject(Relationship::class.java)
    }
}
