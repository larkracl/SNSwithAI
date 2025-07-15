package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.snswithai.data.local.db.entity.UserDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserData(userDataEntity: UserDataEntity)

    @Query("SELECT * FROM user_data WHERE userId = :userId")
    fun getUserData(userId: String): Flow<UserDataEntity?>
}
