package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.CallUtterance
import kotlinx.coroutines.flow.Flow

@Dao
interface CallUtteranceDao {
    @Query("SELECT * FROM call_utterances")
    fun getAllCallUtterances(): Flow<List<CallUtterance>>

    @Query("SELECT * FROM call_utterances WHERE id = :utteranceId")
    suspend fun getCallUtteranceById(utteranceId: Long): CallUtterance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallUtterance(utterance: CallUtterance): Long

    @Update
    suspend fun updateCallUtterance(utterance: CallUtterance)

    @Delete
    suspend fun deleteCallUtterance(utterance: CallUtterance)

    @Query("SELECT * FROM call_utterances WHERE call_id = :callId ORDER BY sequence ASC")
    fun getUtterancesForCall(callId: Long): Flow<List<CallUtterance>>
}
