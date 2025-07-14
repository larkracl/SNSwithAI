package com.example.snswithai.data.repository

import android.util.Log
import com.example.snswithai.data.local.db.dao.CharacterDao
import com.example.snswithai.data.local.db.entity.Character
import com.example.snswithai.data.model.CharacterModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class CharacterRepository(
    private val characterDao: CharacterDao,
    private val database: FirebaseDatabase
) {

    // Firebase에서 모든 캐릭터 정보를 가져와 로컬 DB에 캐시
        suspend fun refreshCharacters() {
        try {
            val snapshot = database.getReference("characters").get().await()
            val characters = snapshot.children.mapNotNull { charSnapshot ->
                val model = charSnapshot.getValue(CharacterModel::class.java)
                if (model == null) {
                    null
                } else {
                    // charNo를 결정하는 로직
                    // 1. 스냅샷 내에 charNo 필드가 있는지 확인 (예: char101)
                    val charNoFromData = charSnapshot.child("charNo").getValue(Int::class.java)

                    // 2. 없다면, 키(예: charNo_2)에서 숫자 부분을 파싱
                    val finalCharNo = charNoFromData ?: charSnapshot.key?.filter { it.isDigit() }?.toIntOrNull() ?: -1

                    if (finalCharNo == -1) {
                        Log.w("CharacterRepository", "Could not determine charNo for key: ${charSnapshot.key}")
                        null
                    } else {
                        Character(
                            charId = charSnapshot.key!!,
                            charNo = finalCharNo,
                            name = model.name,
                            ageGroup = model.ageGroup,
                            description = model.description,
                            personality = model.personality,
                            hobby = model.hobby,
                            imageUrl = model.imageUrl,
                            voiceId = model.voiceId,

                        )
                    }
                }
            }
            characterDao.insertCharacters(characters)

        } catch (e: Exception) {
            Log.e("CharacterRepository", "Failed to refresh characters", e)
        }
    }

    // UI에서는 로컬 DB의 Flow를 관찰
    fun getCharacter(charId: String) = characterDao.getCharacter(charId)
    fun getAllCharacters() = characterDao.getAllCharacters()
}