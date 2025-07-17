package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.snswithai.data.local.db.entity.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<Character>)

    @Query("SELECT * FROM characters WHERE charId = :charId")
    fun getCharacter(charId: String): Flow<Character?>

    @Query("SELECT * FROM characters")
    fun getAllCharacters(): Flow<List<Character>>

}
