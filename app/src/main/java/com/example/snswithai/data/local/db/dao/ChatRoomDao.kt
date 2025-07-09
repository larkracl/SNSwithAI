package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.ChatRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDao {
    @Query("SELECT * FROM chat_rooms")
    fun getAllChatRooms(): Flow<List<ChatRoom>>

    @Query("SELECT * FROM chat_rooms WHERE id = :roomId")
    suspend fun getChatRoomById(roomId: String): ChatRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(room: ChatRoom)

    @Update
    suspend fun updateChatRoom(room: ChatRoom)

    @Delete
    suspend fun deleteChatRoom(room: ChatRoom)
}
