package com.example.snswithai.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.snswithai.data.local.db.entity.ChatRoomMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomMemberDao {
    @Query("SELECT * FROM chat_room_members")
    fun getAllChatRoomMembers(): Flow<List<ChatRoomMember>>

    @Query("SELECT * FROM chat_room_members WHERE room_id = :roomId AND member_id = :memberId")
    suspend fun getChatRoomMember(roomId: String, memberId: String): ChatRoomMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoomMember(member: ChatRoomMember)

    @Update
    suspend fun updateChatRoomMember(member: ChatRoomMember)

    @Delete
    suspend fun deleteChatRoomMember(member: ChatRoomMember)

    @Query("SELECT * FROM chat_room_members WHERE room_id = :roomId")
    fun getMembersForChatRoom(roomId: String): Flow<List<ChatRoomMember>>
}
