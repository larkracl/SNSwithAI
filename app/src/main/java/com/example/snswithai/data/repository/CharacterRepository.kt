package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Character
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CharacterRepository(private val db: FirebaseFirestore) {

    private val charactersCollection = db.collection("characters")

    suspend fun createCharacter(character: Character) {
        charactersCollection.document(character.characterId).set(character).await()
    }

    suspend fun getCharacter(characterId: String): Character? {
        return charactersCollection.document(characterId).get().await().toObject(Character::class.java)
    }

    suspend fun updateCharacter(character: Character) {
        charactersCollection.document(character.characterId).set(character).await()
    }

    suspend fun deleteCharacter(characterId: String) {
        charactersCollection.document(characterId).delete().await()
    }
}
