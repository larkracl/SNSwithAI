package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.Call
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls")
    fun getAllCalls(): Flow<List<Call>>

    @Query("SELECT * FROM calls WHERE id = :callId")
    suspend fun getCallById(callId: Long): Call?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: Call): Long

    @Update
    suspend fun updateCall(call: Call)

    @Delete
    suspend fun deleteCall(call: Call)
}
