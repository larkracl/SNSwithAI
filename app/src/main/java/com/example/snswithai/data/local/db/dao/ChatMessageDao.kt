package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.ChatMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getChatMessageById(messageId: Long): ChatMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage): Long

    @Update
    suspend fun updateChatMessage(message: ChatMessage)

    @Delete
    suspend fun deleteChatMessage(message: ChatMessage)

    @Query("SELECT * FROM chat_messages WHERE room_id = :roomId ORDER BY sent_at ASC")
    fun getMessagesForChatRoom(roomId: String): Flow<List<ChatMessage>>
}
