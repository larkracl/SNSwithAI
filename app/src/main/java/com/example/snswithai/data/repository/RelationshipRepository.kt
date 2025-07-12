package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Relationship
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class RelationshipRepository(private val db: FirebaseDatabase) {

    private val relationshipsRef = db.getReference("relationships")

    suspend fun createRelationship(relationship: Relationship) {
        relationshipsRef.child(relationship.relationshipId).setValue(relationship).await()
    }

    suspend fun getRelationship(relationshipId: String): Relationship? {
        return relationshipsRef.child(relationshipId).get().await().getValue(Relationship::class.java)
    }

    suspend fun updateRelationship(relationship: Relationship) {
        relationshipsRef.child(relationship.relationshipId).setValue(relationship).await()
    }

    suspend fun deleteRelationship(relationshipId: String) {
        relationshipsRef.child(relationshipId).removeValue().await()
    }

    suspend fun getRelationshipsForUser(userId: String): List<Relationship> {
        return relationshipsRef.orderByChild("userId").equalTo(userId).get().await().children.mapNotNull { it.getValue(Relationship::class.java) }
    }

    suspend fun getRelationshipByUserAndCharacter(userId: String, characterId: String): Relationship? {
        return relationshipsRef
            .orderByChild("userId_characterId")
            .equalTo("${userId}_${characterId}")
            .get()
            .await()
            .children
            .firstOrNull()
            ?.getValue(Relationship::class.java)
    }
}
