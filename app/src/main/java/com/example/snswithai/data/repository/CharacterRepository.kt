package com.example.snswithai.data.repository

import com.example.snswithai.data.local.db.entity.Character
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CharacterRepository(private val db: FirebaseDatabase) {

    private val charactersRef = db.getReference("characters")

    suspend fun createCharacter(character: Character) {
        charactersRef.child(character.characterId).setValue(character).await()
    }

    suspend fun getCharacter(characterId: String): Character? {
        return charactersRef.child(characterId).get().await().getValue(Character::class.java)
    }

    suspend fun updateCharacter(character: Character) {
        charactersRef.child(character.characterId).setValue(character).await()
    }

    suspend fun deleteCharacter(characterId: String) {
        charactersRef.child(characterId).removeValue().await()
    }

    suspend fun getCharactersForUser(userId: String): List<Character> {
        return charactersRef.orderByChild("userId").equalTo(userId).get().await().children.mapNotNull { it.getValue(Character::class.java) }
    }
}
